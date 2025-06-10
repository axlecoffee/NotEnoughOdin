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

object PanesSim : TermSimGUI(
    TerminalTypes.PANES.windowName, TerminalTypes.PANES.windowSize
) {
    private val greenPane get() = ItemStack(pane, 1, 5 ).apply { setStackDisplayName("") }
    private val redPane   get() = ItemStack(pane, 1, 14).apply { setStackDisplayName("") }

    override fun create() {
        createNewGui {
            if (floor(it.slotIndex / 9.0) in 1.0..3.0 && it.slotIndex % 9 in 2..6) if (Math.random() > 0.75) greenPane else redPane else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        createNewGui { if (it == slot) { if (slot.stack?.metadata == 14) greenPane else redPane } else it.stack }

        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.metadata == 14 })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
