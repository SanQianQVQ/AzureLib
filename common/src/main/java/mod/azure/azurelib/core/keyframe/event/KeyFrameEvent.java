/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
/*
 * Copyright (c) 2020. Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe.event;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.keyframe.Keyframe;
import mod.azure.azurelib.core.keyframe.event.data.KeyFrameData;

/**
 * The base class for {@link Keyframe} events.<br>
 * These will be passed to one of the controllers in {@link AnimationController} when encountered during animation.
 *
 * @see CustomInstructionKeyframeEvent
 * @see ParticleKeyframeEvent
 * @see SoundKeyframeEvent
 */
public abstract class KeyFrameEvent<T extends GeoAnimatable, E extends KeyFrameData> {

    private final T animatable;

    private final double animationTick;

    private final AnimationController<T> controller;

    private final E eventKeyFrame;

    protected KeyFrameEvent(T animatable, double animationTick, AnimationController<T> controller, E eventKeyFrame) {
        this.animatable = animatable;
        this.animationTick = animationTick;
        this.controller = controller;
        this.eventKeyFrame = eventKeyFrame;
    }

    /**
     * Gets the amount of ticks that have passed in either the current transition or animation, depending on the
     * controller's AnimationState.
     */
    public double getAnimationTick() {
        return animationTick;
    }

    /**
     * Gets the {@link GeoAnimatable} object being rendered
     */
    public T getAnimatable() {
        return animatable;
    }

    /**
     * Gets the {@link AnimationController} responsible for the currently playing animation
     */
    public AnimationController<T> getController() {
        return controller;
    }

    /**
     * Returns the {@link KeyFrameData} relevant to the encountered {@link Keyframe}
     */
    public E getKeyframeData() {
        return this.eventKeyFrame;
    }
}
