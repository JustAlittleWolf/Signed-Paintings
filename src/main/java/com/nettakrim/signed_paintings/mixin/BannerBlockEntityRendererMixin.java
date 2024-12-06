package com.nettakrim.signed_paintings.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nettakrim.signed_paintings.SignedPaintingsClient;
import com.nettakrim.signed_paintings.access.OverlayInfoAccessor;
import com.nettakrim.signed_paintings.rendering.OverlayInfo;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.model.BannerFlagBlockModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerBlockEntityRenderer.class)
public class BannerBlockEntityRendererMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BannerBlockEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIFLnet/minecraft/client/render/block/entity/model/BannerBlockModel;Lnet/minecraft/client/render/block/entity/model/BannerFlagBlockModel;FLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V"), method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
    private void onRender(BannerBlockEntity bannerBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci, @Local BannerFlagBlockModel flagBlockModel) {
        if (!SignedPaintingsClient.renderBanners) return;
        OverlayInfoAccessor accessor = (OverlayInfoAccessor)bannerBlockEntity;
        accessor.signedPaintings$reloadIfNeeded();
        OverlayInfo overlayInfo = accessor.signedPaintings$getOverlayInfo();
        if (overlayInfo.isReady()) {
            SignedPaintingsClient.paintingRenderer.renderImageOverlay(matrixStack, vertexConsumerProvider, overlayInfo, flagBlockModel.getRootPart(), i);
        }
    }
}
