/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.animation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.model.CoreBakedGeoModel;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animatable.model.CoreGeoModel;
import mod.azure.azurelib.core.keyframe.AnimationPoint;
import mod.azure.azurelib.core.keyframe.BoneAnimationQueue;
import mod.azure.azurelib.core.state.BoneSnapshot;
import mod.azure.azurelib.core.utils.Interpolations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AnimationProcessor<T extends GeoAnimatable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimationProcessor.class);

    private final Map<String, CoreGeoBone> bonesByName = new Object2ObjectOpenHashMap<>();

    private final CoreGeoModel<T> model;

    public boolean reloadAnimations = false;

    public AnimationProcessor(CoreGeoModel<T> model) {
        this.model = model;
    }

    /**
     * Build an animation queue for the given {@link RawAnimation}
     *
     * @param animatable   The animatable object being rendered
     * @param rawAnimation The raw animation to be compiled
     * @return A queue of animations and loop types to play
     */
    public Queue<QueuedAnimation> buildAnimationQueue(T animatable, RawAnimation rawAnimation) {
        LinkedList<QueuedAnimation> animations = new LinkedList<>();

        for (RawAnimation.Stage stage : rawAnimation.getAnimationStages()) {
            Animation animation;

            if (Objects.equals(stage.animationName(), RawAnimation.Stage.WAIT)) {
                animation = Animation.generateWaitAnimation(stage.additionalTicks());
            } else {
                animation = this.model.getAnimation(animatable, stage.animationName());
            }

            if (animation == null) {
                LOGGER.warn(
                        "Unable to find animation: {} for {}",
                        stage.animationName(),
                        animatable.getClass().getSimpleName()
                );
                return null;
            } else {
                animations.add(new QueuedAnimation(animation, stage.loopType()));
            }
        }

        return animations;
    }

    /**
     * Tick and apply transformations to the model based on the current state of the {@link AnimationController}
     *
     * @param animatable            The animatable object relevant to the animation being played
     * @param model                 The model currently being processed
     * @param animatableManager     The AnimatableManager instance being used for this animation processor
     * @param animTime              The internal tick counter kept by the {@link AnimatableManager} for this animatable
     * @param event                 An {@link AnimationState} instance applied to this render frame
     * @param crashWhenCantFindBone Whether to crash if unable to find a required bone, or to continue with the
     *                              remaining bones
     */
    public void tickAnimation(
            T animatable,
            CoreGeoModel<T> model,
            AnimatableManager<T> animatableManager,
            double animTime,
            AnimationState<T> event,
            boolean crashWhenCantFindBone
    ) {
        Map<String, BoneSnapshot> boneSnapshots = updateBoneSnapshots(animatableManager.getBoneSnapshotCollection());

        for (AnimationController<T> controller : animatableManager.getAnimationControllers().values()) {
            if (this.reloadAnimations) {
                controller.forceAnimationReset();
                controller.getBoneAnimationQueues().clear();
            }

            controller.isJustStarting = animatableManager.isFirstTick();

            event.withController(controller);
            controller.process(model, event, this.bonesByName, boneSnapshots, animTime, crashWhenCantFindBone);

            for (BoneAnimationQueue boneAnimation : controller.getBoneAnimationQueues().values()) {
                CoreGeoBone bone = boneAnimation.bone();
                BoneSnapshot snapshot = boneSnapshots.get(bone.getName());
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();

                AnimationPoint rotXPoint = boneAnimation.rotationXQueue().poll();
                AnimationPoint rotYPoint = boneAnimation.rotationYQueue().poll();
                AnimationPoint rotZPoint = boneAnimation.rotationZQueue().poll();
                AnimationPoint posXPoint = boneAnimation.positionXQueue().poll();
                AnimationPoint posYPoint = boneAnimation.positionYQueue().poll();
                AnimationPoint posZPoint = boneAnimation.positionZQueue().poll();
                AnimationPoint scaleXPoint = boneAnimation.scaleXQueue().poll();
                AnimationPoint scaleYPoint = boneAnimation.scaleYQueue().poll();
                AnimationPoint scaleZPoint = boneAnimation.scaleZQueue().poll();
                EasingType easingType = controller.overrideEasingTypeFunction.apply(animatable);

                if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
                    bone.setRotX(
                            (float) EasingType.lerpWithOverride(rotXPoint, easingType) + initialSnapshot.getRotX()
                    );
                    bone.setRotY(
                            (float) EasingType.lerpWithOverride(rotYPoint, easingType) + initialSnapshot.getRotY()
                    );
                    bone.setRotZ(
                            (float) EasingType.lerpWithOverride(rotZPoint, easingType) + initialSnapshot.getRotZ()
                    );
                    snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                    snapshot.startRotAnim();
                    bone.markRotationAsChanged();
                }

                if (posXPoint != null && posYPoint != null && posZPoint != null) {
                    bone.setPosX((float) EasingType.lerpWithOverride(posXPoint, easingType));
                    bone.setPosY((float) EasingType.lerpWithOverride(posYPoint, easingType));
                    bone.setPosZ((float) EasingType.lerpWithOverride(posZPoint, easingType));
                    snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
                    snapshot.startPosAnim();
                    bone.markPositionAsChanged();
                }

                if (scaleXPoint != null && scaleYPoint != null && scaleZPoint != null) {
                    bone.setScaleX((float) EasingType.lerpWithOverride(scaleXPoint, easingType));
                    bone.setScaleY((float) EasingType.lerpWithOverride(scaleYPoint, easingType));
                    bone.setScaleZ((float) EasingType.lerpWithOverride(scaleZPoint, easingType));
                    snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
                    snapshot.startScaleAnim();
                    bone.markScaleAsChanged();
                }
            }
        }

        this.reloadAnimations = false;
        double resetTickLength = animatable.getBoneResetTime();

        for (CoreGeoBone bone : getRegisteredBones()) {
            if (!bone.hasRotationChanged()) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

                if (saveSnapshot.isRotAnimInProgress())
                    saveSnapshot.stopRotAnim(animTime);

                double percentageReset = Math.min(
                        (animTime - saveSnapshot.getLastResetRotationTick()) / resetTickLength,
                        1
                );

                bone.setRotX(
                        (float) Interpolations.lerp(saveSnapshot.getRotX(), initialSnapshot.getRotX(), percentageReset)
                );
                bone.setRotY(
                        (float) Interpolations.lerp(saveSnapshot.getRotY(), initialSnapshot.getRotY(), percentageReset)
                );
                bone.setRotZ(
                        (float) Interpolations.lerp(saveSnapshot.getRotZ(), initialSnapshot.getRotZ(), percentageReset)
                );

                if (percentageReset >= 1)
                    saveSnapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            }

            if (!bone.hasPositionChanged()) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

                if (saveSnapshot.isPosAnimInProgress())
                    saveSnapshot.stopPosAnim(animTime);

                double percentageReset = Math.min(
                        (animTime - saveSnapshot.getLastResetPositionTick()) / resetTickLength,
                        1
                );

                bone.setPosX(
                        (float) Interpolations.lerp(
                                saveSnapshot.getOffsetX(),
                                initialSnapshot.getOffsetX(),
                                percentageReset
                        )
                );
                bone.setPosY(
                        (float) Interpolations.lerp(
                                saveSnapshot.getOffsetY(),
                                initialSnapshot.getOffsetY(),
                                percentageReset
                        )
                );
                bone.setPosZ(
                        (float) Interpolations.lerp(
                                saveSnapshot.getOffsetZ(),
                                initialSnapshot.getOffsetZ(),
                                percentageReset
                        )
                );

                if (percentageReset >= 1)
                    saveSnapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
            }

            if (!bone.hasScaleChanged()) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

                if (saveSnapshot.isScaleAnimInProgress())
                    saveSnapshot.stopScaleAnim(animTime);

                double percentageReset = Math.min(
                        (animTime - saveSnapshot.getLastResetScaleTick()) / resetTickLength,
                        1
                );

                bone.setScaleX(
                        (float) Interpolations.lerp(saveSnapshot.getScaleX(), initialSnapshot.getScaleX(),
                                percentageReset)
                );
                bone.setScaleY(
                        (float) Interpolations.lerp(saveSnapshot.getScaleY(), initialSnapshot.getScaleY(),
                                percentageReset)
                );
                bone.setScaleZ(
                        (float) Interpolations.lerp(saveSnapshot.getScaleZ(), initialSnapshot.getScaleZ(),
                                percentageReset)
                );

                if (percentageReset >= 1)
                    saveSnapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            }
        }

        resetBoneTransformationMarkers();
        animatableManager.finishFirstTick();
    }

    /**
     * Reset the transformation markers applied to each {@link CoreGeoBone} ready for the next render frame
     */
    private void resetBoneTransformationMarkers() {
        getRegisteredBones().forEach(CoreGeoBone::resetStateChanges);
    }

    /**
     * Create new bone {@link BoneSnapshot} based on the bone's initial snapshot for the currently registered
     * {@link CoreGeoBone GeoBones}, filtered by the bones already present in the master snapshots map
     *
     * @param snapshots The master bone snapshots map from the related {@link AnimatableManager}
     * @return The input snapshots map, for easy assignment
     */
    private Map<String, BoneSnapshot> updateBoneSnapshots(Map<String, BoneSnapshot> snapshots) {
        for (CoreGeoBone bone : getRegisteredBones()) {
            if (!snapshots.containsKey(bone.getName()))
                snapshots.put(bone.getName(), BoneSnapshot.copy(bone.getInitialSnapshot()));
        }

        return snapshots;
    }

    /**
     * Gets a bone by name.
     *
     * @param boneName The bone name
     * @return the bone
     */
    public CoreGeoBone getBone(String boneName) {
        return this.bonesByName.get(boneName);
    }

    /**
     * Adds the given bone to the bones list for this processor.<br>
     * This is normally handled automatically by AzureLib.<br>
     * Failure to properly register a bone will break things.
     */
    public void registerGeoBone(CoreGeoBone bone) {
        bone.saveInitialSnapshot();
        this.bonesByName.put(bone.getName(), bone);
        bone.getChildBones().forEach(this::registerGeoBone);
    }

    /**
     * Clear the {@link CoreGeoBone GeoBones} currently registered to the processor, then prepares the processor for a
     * new model.<br>
     * Should be called whenever switching models to render/animate
     */
    public void setActiveModel(CoreBakedGeoModel model) {
        this.bonesByName.clear();
        model.getBones().forEach(this::registerGeoBone);
    }

    /**
     * Get an iterable collection of the {@link CoreGeoBone GeoBones} currently registered to the processor
     */
    public Collection<CoreGeoBone> getRegisteredBones() {
        return this.bonesByName.values();
    }

    /**
     * Apply transformations and settings prior to acting on any animation-related functionality
     */
    public void preAnimationSetup(T animatable, double animTime) {
        this.model.applyMolangQueries(animatable, animTime);
    }

    /**
     * {@link Animation} and {@link Animation.LoopType} override pair, used to define a playable animation stage for a
     * {@link GeoAnimatable}
     */
    public record QueuedAnimation(
            Animation animation,
            Animation.LoopType loopType
    ) {
    }
}
