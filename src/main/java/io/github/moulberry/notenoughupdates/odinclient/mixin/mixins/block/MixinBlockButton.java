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
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockButton.class)
public class MixinBlockButton extends Block {

    public MixinBlockButton(Material materialIn)
    {
        super(materialIn);
    }

    @Inject(method = "updateBlockBounds", at = @At("HEAD"), cancellable = true)
    private void onUpdateBlockBounds(IBlockState state, CallbackInfo ci) {
        if (SecretHitboxes.INSTANCE.getEnabled() && SecretHitboxes.INSTANCE.getButton()) {
            EnumFacing enumfacing = state.getValue(BlockButton.FACING);
            boolean flag = state.getValue(BlockButton.POWERED);
            float f2 = (flag ? 1 : 2) / 16.0f;

            switch (enumfacing) {
                case EAST:
                    this.setBlockBounds(0.0f, 0.0f, 0.0f, f2, 1.0f, 1.0f);
                    break;

                case WEST:
                    this.setBlockBounds(1.0f - f2, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;

                case SOUTH:
                    this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, f2);
                    break;

                case NORTH:
                    this.setBlockBounds(0.0f, 0.0f, 1.0f - f2, 1.0f, 1.0f, 1.0f);
                    break;

                case UP:
                    this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.0f + f2, 1.0f);
                    break;

                case DOWN:
                    this.setBlockBounds(0.0f, 1.0f - f2, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
            }
            ci.cancel();
        }
    }
}
