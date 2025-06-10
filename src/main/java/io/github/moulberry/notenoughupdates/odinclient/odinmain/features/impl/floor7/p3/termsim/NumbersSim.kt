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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termsim

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.TerminalEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalTypes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.postAndCatch
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object NumbersSim : TermSimGUI(
    TerminalTypes.NUMBERS.windowName, TerminalTypes.NUMBERS.windowSize
) {
    override fun create() {
        val used = (1..14).shuffled().toMutableList()
        createNewGui {
            if (floor(it.slotIndex / 9.0) in 1.0..2.0 && it.slotIndex % 9 in 1..7) ItemStack(pane, used.removeFirst(), 14).apply { setStackDisplayName("") }
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (guiInventorySlots.minByOrNull { if (it.stack?.metadata == 14) it.stack?.stackSize ?: 999 else 1000 } != slot) return
        createNewGui {
            if (it == slot) ItemStack(pane, slot.stack.stackSize, 5).apply { setStackDisplayName("") }
            else it.stack
        }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.metadata == 14 })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
