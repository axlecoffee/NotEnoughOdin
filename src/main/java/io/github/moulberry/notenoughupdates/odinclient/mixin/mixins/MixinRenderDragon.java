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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.PersonalDragon;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.EntityDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(RenderDragon.class)
public class MixinRenderDragon {

    @Inject(method = "doRender(Lnet/minecraft/entity/boss/EntityDragon;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderLiving;doRender(Lnet/minecraft/entity/EntityLiving;DDDFF)V"))
    private void onDoRender(EntityDragon entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!PersonalDragon.INSTANCE.getEnabled()) return;
        if (entity.getEntityId() == Objects.requireNonNull(PersonalDragon.INSTANCE.getDragon()).getEntityId()) {
            BossStatus.bossName = null;
            BossStatus.statusBarTime = 0;
        }
    }
}
