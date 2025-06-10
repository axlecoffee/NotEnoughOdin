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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon.AutoSell.sellList

import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.Minecraft

val autoSellCommand = Commodore("autosell") {

    literal("add").runs { item: GreedyString? ->
        val lowercase = item?.string?.lowercase() ?: Minecraft.getMinecraft().thePlayer?.heldItem?.unformattedName?.lowercase() ?: return@runs modMessage("Either hold an item or write an item name to be added to autosell.")
        if (lowercase in sellList) return@runs modMessage("$lowercase is already in the Auto sell list.")

        modMessage("Added $lowercase to the Auto sell list.")
        sellList.add(lowercase)
        Config.save()
    }

    literal("remove").runs { item: GreedyString ->
        val lowercase = item.string.lowercase()
        if (lowercase !in sellList) return@runs modMessage("$item isn't in the Auto sell list.")

        modMessage("Removed $item from the Auto sell list.")
        sellList.remove(lowercase)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Auto sell list cleared.")
        sellList.clear()
        Config.save()
    }

    literal("list").runs {
        if (sellList.isEmpty()) return@runs modMessage("Auto sell list is empty")
        val chunkedList = sellList.chunked(10)
        modMessage("Auto sell list:\n${chunkedList.joinToString("\n")}")
    }
}
