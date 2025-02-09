/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.api.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.cache.object.GeoBone;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * {@link GeoRenderLayer} for auto-applying some form of modification to bones of a model prior to rendering.<br>
 * This can be useful for enabling or disabling bone rendering based on arbitrary conditions.<br>
 * <br>
 * NOTE: Despite this layer existing, it is much more efficient to use {@link FastBoneFilterGeoLayer} instead
 */
public class BoneFilterGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    protected final TriConsumer<GeoBone, T, Float> checkAndApply;

    public BoneFilterGeoLayer(GeoRenderer<T> renderer) {
        this(renderer, (bone, animatable, partialTick) -> {
        });
    }

    public BoneFilterGeoLayer(GeoRenderer<T> renderer, TriConsumer<GeoBone, T, Float> checkAndApply) {
        super(renderer);

        this.checkAndApply = checkAndApply;
    }

    /**
     * This method is called for each bone in the model.<br>
     * Check whether the bone should be affected and apply the modification as needed.
     */
    protected void checkAndApply(GeoBone bone, T animatable, float partialTick) {
        this.checkAndApply.accept(bone, animatable, partialTick);
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            T animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        for (GeoBone bone : bakedModel.getTopLevelBones()) {
            checkChildBones(bone, animatable, partialTick);
        }
    }

    private void checkChildBones(GeoBone parentBone, T animatable, float partialTick) {
        checkAndApply(parentBone, animatable, partialTick);

        for (GeoBone bone : parentBone.getChildBones()) {
            checkChildBones(bone, animatable, partialTick);
        }
    }
}
