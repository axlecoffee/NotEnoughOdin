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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameData
import kotlin.math.floor

class StartsWithSim(private val letter: String = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T").random()) : TermSimGUI(
    "What starts with: \'$letter\'?",
    TerminalTypes.STARTS_WITH.windowSize
) {
    override fun create() {
        createNewGui {
            when {
                floor(it.slotIndex / 9.0) !in 1.0..3.0 || it.slotIndex % 9 !in 1..7 -> blackPane
                it.slotIndex == (10..16).random() -> getLetterItemStack()
                Math.random() > .7 -> getLetterItemStack()
                else -> getLetterItemStack(true)
            }
        }
    }

    override fun slotClick(slot: Slot, button: Int) = with(slot.stack) {
        if (displayName?.startsWith(letter, true) == false || isItemEnchanted) {
            Minecraft.getMinecraft().thePlayer?.closeScreen()
            return modMessage("§cThat item does not start with: \'$letter\'!")
        }

        createNewGui { if (it == slot) ItemStack(item, stackSize, metadata).apply { addEnchantment(Enchantment.infinity, 1) } else it.stack }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.displayName?.startsWith(letter, true) == true && !it.stack.isItemEnchanted })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getLetterItemStack(filterNot: Boolean = false): ItemStack =
        ItemStack(GameData.getItemRegistry().filter { it.registryName.replace("minecraft:", "").startsWith(letter, true) != filterNot }.random())
}
