/*
 * Copyright (C) 2025 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.odinclient.mixin.mixins;

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.Animations;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 9999)
public abstract class MixinItemRenderer {

    @Shadow protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Inject(method = "transformFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void onTransformFirstPersonItem(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (Animations.itemTransferHook(equipProgress, swingProgress)) ci.cancel();
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 0))
    private void renderItemHook(ItemRenderer instance, float equipProgress, float swingProgress) {
        this.transformFirstPersonItem(Animations.getShouldNoEquipReset() ? 0.0f : equipProgress, swingProgress);
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1))
    private void renderItemHook1(ItemRenderer instance, float equipProgress, float swingProgress) {
        this.transformFirstPersonItem(Animations.getShouldNoEquipReset() ? 0.0f : equipProgress, swingProgress);
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 2))
    private void renderItemHook2(ItemRenderer instance, float equipProgress, float swingProgress) {
        this.transformFirstPersonItem(Animations.getShouldNoEquipReset() ? 0.0f : equipProgress, swingProgress);
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 3))
    private void renderItemHook3(ItemRenderer instance, float equipProgress, float swingProgress) {
        this.transformFirstPersonItem(Animations.getShouldNoEquipReset() ? 0.0f : equipProgress, swingProgress);
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 4))
    private void renderItemHook4(ItemRenderer instance, float equipProgress, float swingProgress) {
        this.transformFirstPersonItem(Animations.getShouldNoEquipReset() ? 0.0f : equipProgress, swingProgress);
    }

    @Inject(method = "doItemUsedTransformations", at = @At("HEAD"), cancellable = true)
    public void useTransform(float swingProgress, CallbackInfo ci) {
        if (Animations.getShouldStopSwing() || Animations.scaledSwing(swingProgress)) ci.cancel();
    }
}
