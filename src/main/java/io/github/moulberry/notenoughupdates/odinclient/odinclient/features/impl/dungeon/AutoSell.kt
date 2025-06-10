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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ActionSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ListSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.containsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.name
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.windowClick
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.inventory.ContainerChest

object AutoSell : Module(
    name = "Auto Sell",
    desc = "Automatically sell items in trades and cookie menus. (/autosell)"
) {
    val sellList by ListSetting("Sell list", mutableSetOf<String>())
    private val delay by NumberSetting("Delay", 100L, 75L, 300L, 5L, desc = "The delay between each sell action.", unit = "ms")
    private val clickType by SelectorSetting("Click Type", "Shift", arrayListOf("Shift", "Middle", "Left"), desc = "The type of click to use when selling items.")
    private val addDefaults by ActionSetting("Add defaults", desc = "Add default dungeon items to the auto sell list.") {
        sellList.addAll(defaultItems)
        modMessage("§aAdded default items to auto sell list")
        Config.save()
    }

    init {
        execute(delay = { delay }) {
            if (!enabled || sellList.isEmpty()) return@execute
            val container = Minecraft.getMinecraft().thePlayer?.openContainer as? ContainerChest ?: return@execute

            if (!container.name.equalsOneOf("Trades", "Booster Cookie", "Farm Merchant", "Ophelia")) return@execute
            val index = container.inventorySlots?.subList(54, 90)?.firstOrNull { it.stack?.displayName?.containsOneOf(sellList, true) == true }?.slotNumber ?: return@execute
            when (clickType) {
                0 -> windowClick(index, ClickType.Shift)
                1 -> windowClick(index, ClickType.Middle)
                2 -> windowClick(index, ClickType.Left)
            }
        }
    }

    private val defaultItems = arrayOf(
        "enchanted ice", "health potion", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
        "skeleton lord", "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "zombie lord",
        "skeletor", "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
        "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight", "health potion viii",
        "health potion 8", "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
        "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever", "diamond atom",
        "health potion viii splash potion", "healing potion 8 splash potion", "healing potion viii splash potion",
        "healing viii splash potion", "healing 8 splash potion"
    )
}
