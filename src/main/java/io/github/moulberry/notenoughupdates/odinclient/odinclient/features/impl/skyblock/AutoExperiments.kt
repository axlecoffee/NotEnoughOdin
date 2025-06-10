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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.name
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.Island
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoExperiments : Module(
    name = "Auto Experiments",
    desc = "Automatically click on the Chronomatron and Ultrasequencer experiments."
){
    private val delay by NumberSetting("Click Delay", 200, 0, 1000, 10, unit = "ms", desc = "Time in ms between automatic test clicks.")
    private val autoClose by BooleanSetting("Auto Close", true, desc = "Automatically close the GUI after completing the experiment.")
    private val serumCount by NumberSetting("Serum Count", 0, 0, 3, 1, desc = "Consumed Metaphysical Serum count.")

    private var ultrasequencerOrder = HashMap<Int, Int>()
    private val chronomatronOrder = ArrayList<Int>(28)
    private var lastClickTime = 0L
    private var hasAdded = false
    private var lastAdded = 0
    private var clicks = 0

    private fun reset() {
        ultrasequencerOrder.clear()
        chronomatronOrder.clear()
        hasAdded = false
        lastAdded = 0
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui == null) reset()
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/EnchantingExperiments.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onGuiDraw(event: GuiEvent.DrawGuiBackground) {
        if (!LocationUtils.currentArea.isArea(Island.PrivateIsland)) return
        val gui = ((event.gui as? GuiChest)?.inventorySlots as? ContainerChest) ?: return

        when {
            gui.name.startsWith("Chronomatron (") -> solveChronomatron(gui.inventorySlots)
            gui.name.startsWith("Ultrasequencer (") -> solveUltraSequencer(gui.inventorySlots)
            else -> return
        }
    }

    private fun solveChronomatron(invSlots: List<Slot>) {
        if (invSlots[49].stack?.item == Item.getItemFromBlock(Blocks.glowstone) && invSlots[lastAdded].stack?.isItemEnchanted == false) {
            if (autoClose && chronomatronOrder.size > 11 - serumCount) Minecraft.getMinecraft().thePlayer?.closeScreen()
            hasAdded = false
        }
        if (!hasAdded && invSlots[49].stack?.item == Items.clock) {
            invSlots.find { it.slotNumber in 10..43 && it.stack?.isItemEnchanted == true }?.let {
                chronomatronOrder.add(it.slotNumber)
                lastAdded = it.slotNumber
                hasAdded = true
                clicks = 0
            }
        }
        if (hasAdded && invSlots[49].stack?.item == Items.clock && chronomatronOrder.size > clicks && System.currentTimeMillis() - lastClickTime > delay) {
            windowClick(chronomatronOrder[clicks], ClickType.Middle)
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }

    private fun solveUltraSequencer(invSlots: List<Slot>) {
        if (invSlots[49].stack?.item == Items.clock) hasAdded = false

        if (!hasAdded && invSlots[49].stack?.item == Item.getItemFromBlock(Blocks.glowstone)) {
            if (!invSlots[44].hasStack) return
            ultrasequencerOrder.clear()
            invSlots.forEach {
                if (it.slotNumber in 9..44 && it.stack?.item == Items.dye) ultrasequencerOrder[it.stack.stackSize - 1] = it.slotNumber
            }
            hasAdded = true
            clicks = 0
            if (ultrasequencerOrder.size > 9 - serumCount && autoClose) Minecraft.getMinecraft().thePlayer?.closeScreen()
        }
        if (invSlots[49].stack?.item == Items.clock && ultrasequencerOrder.contains(clicks) && System.currentTimeMillis() - lastClickTime > delay) {
            ultrasequencerOrder[clicks]?.let { windowClick(it, ClickType.Middle) }
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }
}
