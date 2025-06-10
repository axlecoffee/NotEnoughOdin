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

object RubixSim : TermSimGUI(
    TerminalTypes.RUBIX.windowName, TerminalTypes.RUBIX.windowSize
) {
    private val indices = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
    private val order = listOf(1, 4, 13, 11, 14)

    override fun create() {
        createNewGui {
            if (floor(it.slotIndex / 9.0) in 1.0..3.0 && it.slotIndex % 9 in 3..5) getPane()
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        val current = order.find { it == slot.stack?.metadata } ?: return
        createNewGui {
            if (it == slot) {
                if (button == 1) genStack(order.getOrElse(order.indexOf(current) - 1) { order.last() })
                else genStack(order[(order.indexOf(current) + 1) % order.size])
            } else it.stack ?: blackPane
        }

        playTermSimSound()
        if (indices.all { guiInventorySlots[it]?.stack?.metadata == guiInventorySlots[12]?.stack?.metadata })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getPane(): ItemStack {
        return when (Math.random()) {
            in 0.0..0.2 -> genStack(order[0])
            in 0.2..0.4 -> genStack(order[1])
            in 0.4..0.6 -> genStack(order[2])
            in 0.6..0.8 -> genStack(order[3])
            else -> genStack(order[4])
        }
    }

    private fun genStack(meta: Int) = ItemStack(pane, 1, meta).apply { setStackDisplayName("") } // This makes unique itemstacks, so terminalsolver works.
}
