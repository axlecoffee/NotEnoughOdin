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

package io.github.moulberry.notenoughupdates.odinclient.mixin.mixins.entity;

import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.render.NoDebuff;
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.Animations;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityLivingBase.class, priority = 9999)
public abstract class MixinEntityLivingBase {

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Inject(method = {"getArmSwingAnimationEnd()I"}, at = @At("HEAD"), cancellable = true)
    public void adjustSwingLength(CallbackInfoReturnable<Integer> cir) {
        if (!Animations.INSTANCE.getEnabled()) return;
        int length = Animations.INSTANCE.getIgnoreHaste() ? 6 : this.isPotionActive(Potion.digSpeed) ?
                6 - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) :
                (this.isPotionActive(Potion.digSlowdown) ?
               6 + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
        cir.setReturnValue(Math.max((int)(length* Math.exp(-Animations.INSTANCE.getSpeed())), 1));
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void isPotionActive(Potion potion, CallbackInfoReturnable<Boolean> cir) {
        if (NoDebuff.getShouldIgnoreNausea() && potion == Potion.confusion) cir.setReturnValue(false);
    }
}
