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

package io.github.moulberry.notenoughupdates.odinclient.mixin.mixins.block;

import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon.SecretHitboxes;
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.p3.LightsDevice;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockLever.class)
public class MixinBlockLever extends Block {

    public MixinBlockLever(Material materialIn)
    {
        super(materialIn);
    }

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void onSetBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos, CallbackInfo ci) {
        if (LightsDevice.INSTANCE.getEnabled() && LightsDevice.INSTANCE.getBigLevers() && LightsDevice.INSTANCE.getLevers().contains(pos)) {
            this.setBlockBounds(0, 0, 0, 1, 1, 1);
            ci.cancel();
            return;
        }
        if (SecretHitboxes.INSTANCE.getLever() && SecretHitboxes.INSTANCE.getEnabled()) {
            if (pos.getX() >= 58 && pos.getX() <= 62 && pos.getY() >= 133 && pos.getY() <= 136 && pos.getZ() == 142) return;

            this.setBlockBounds(0, 0, 0, 1, 1, 1);
            ci.cancel();
        }
    }
}
