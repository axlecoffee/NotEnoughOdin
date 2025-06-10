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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.terminalhandler

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalTypes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean {
        if (packet.func_149173_d() != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solvePanes(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: ClickType) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solvePanes(items: Array<ItemStack?>): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.metadata == 14) index else null }
}
