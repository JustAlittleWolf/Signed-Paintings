package com.nettakrim.signed_paintings.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SliderWidget.class)
public class SliderWidgetMixin {
    @WrapOperation(
            method = "renderButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawNineSlicedTexture(Lnet/minecraft/util/Identifier;IIIIIIIIII)V",
                    ordinal = 1
            )
    )
    private void fixSliderHeight(DrawContext instance, Identifier texture, int x, int y, int width, int height, int outerSliceWidth, int outerSliceHeight, int centerSliceWidth, int centerSliceHeight, int u, int v, Operation<Void> original) {
        original.call(instance, texture, x, y, width, ((SliderWidget)(Object)this).getHeight(), outerSliceHeight, outerSliceHeight, centerSliceWidth, centerSliceHeight, u, v);
    }
}
