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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl

import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.fml.common.eventhandler.Event

open class RenderChestEvent(var chest: TileEntityChest, var x: Double, var y: Double, var z: Double, var partialTicks: Float) : Event() {

    class Pre(tileEntity: TileEntityChest, x: Double, y: Double, z: Double, partialTicks: Float) : RenderChestEvent(tileEntity, x, y, z, partialTicks)

    class Post(tileEntity: TileEntityChest, x: Double, y: Double, z: Double, partialTicks: Float) : RenderChestEvent(tileEntity, x, y, z, partialTicks)
}
