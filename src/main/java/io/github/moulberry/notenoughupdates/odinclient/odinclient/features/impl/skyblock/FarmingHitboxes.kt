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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.mixin.accessors.IBlockAccessor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import net.minecraft.block.Block

object FarmingHitboxes : Module(
    name = "Farming Hitboxes",
    desc = "Expands the hitbox of some crops to a full block."
) {
    fun setFullBlock(block: Block) {
        val accessor = (block as IBlockAccessor)
        accessor.setMinX(0.0)
        accessor.setMinY(0.0)
        accessor.setMinZ(0.0)
        accessor.setMaxX(1.0)
        accessor.setMaxY(1.0)
        accessor.setMaxZ(1.0)
    }
}
