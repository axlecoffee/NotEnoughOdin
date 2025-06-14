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
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object MelodySim : TermSimGUI(
    TerminalTypes.MELODY.windowName, TerminalTypes.MELODY.windowSize
) {
    private val magentaPane get() = ItemStack(pane, 1, 2 ).apply { setStackDisplayName("") }
    private val greenPane   get() = ItemStack(pane, 1, 5 ).apply { setStackDisplayName("") }
    private val redPane     get() = ItemStack(pane, 1, 14).apply { setStackDisplayName("") }
    private val whitePane   get() = ItemStack(pane, 1, 0 ).apply { setStackDisplayName("") }
    private val redClay     get() = ItemStack(Item.getItemById(159), 1, 14).apply { setStackDisplayName("") }
    private val greenClay   get() = ItemStack(Item.getItemById(159), 1, 5 ).apply { setStackDisplayName("") }

    private var magentaColumn = 1
    private var limeColumn = 2
    private var currentRow = 1
    private var limeDirection = 1

    override fun create() {
        currentRow = 1
        magentaColumn = (1..5).random()
        limeColumn = 1
        limeDirection = 1
        createNewGui { it.generateItemStack() }
    }

    private var counter = 0

    override fun updateScreen() {
        if (counter++ % 10 != 0) return
        limeColumn += limeDirection
        if (limeColumn == 1 || limeColumn == 5) limeDirection *= -1
        updateGui()
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (slot.slotIndex % 9 != 7 || limeColumn != magentaColumn || slot.slotIndex / 9 != currentRow) return

        magentaColumn = (1 until 5).random()
        currentRow++
        updateGui()

        playTermSimSound()
        if (currentRow >= 5) TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun updateGui() {
        guiInventorySlots.forEachIndexed { index, currentStack ->
            currentStack?.setSlot(guiInventorySlots.map { it.generateItemStack() }.getOrNull(index)?.takeIf { it != currentStack.stack } ?: return@forEachIndexed)
        }
    }

    private fun Slot.generateItemStack(): ItemStack {
        return when {
            slotIndex % 9 == magentaColumn && slotIndex / 9 !in 1..4 -> magentaPane
            slotIndex % 9 == limeColumn && slotIndex / 9 == currentRow -> greenPane
            slotIndex % 9 in 1..5 && slotIndex / 9 == currentRow -> redPane
            slotIndex % 9 == 7 && slotIndex / 9 == currentRow -> greenClay
            slotIndex % 9 == 7 && slotIndex / 9 in 1..4 -> redClay
            slotIndex % 9 in 1..5 && slotIndex / 9 in 1..4 -> whitePane
            else -> blackPane
        }
    }
}


