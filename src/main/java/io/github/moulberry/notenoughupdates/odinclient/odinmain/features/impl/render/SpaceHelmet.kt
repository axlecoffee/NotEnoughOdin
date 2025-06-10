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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.setLore
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
object SpaceHelmet : Module(
    name = "Space Helmet",
    desc = "Equips you with a space helmet."
) {
    private val speed by NumberSetting("Speed", 250L, 100, 1000, 10, desc = "The speed at which the color changes.", unit = "ms")
    private var edition = 0
    private val values = listOf(14, 1, 4, 5, 13, 9, 11, 10, 6)
    private var currentIndex = 0

    init {
        execute({ speed }) {
            if (Minecraft.getMinecraft().currentScreen !== null) return@execute

            currentIndex = (currentIndex + 1) % values.size
            val item = ItemStack(Item.getItemFromBlock(Blocks.stained_glass), 1, values[currentIndex]).apply { setStackDisplayName("§c§lSpace Helmet") }
                .setLore(listOf("§7A rare space helmet forged", "§7from shards of moon glass", "", "§7To: ${Minecraft.getMinecraft().thePlayer?.displayName?.siblings?.firstOrNull()?.formattedText}", "§7From: §6Odin", "", "§8Edition #${edition}", "§8${Month.entries[LocalDateTime.now().monthValue - 1].getDisplayName(TextStyle.FULL, Locale.getDefault())} 2024", "", "§8This item can be reforged!", "§c§lSPECIAL HELMET"))
            edition++
            Minecraft.getMinecraft().thePlayer?.inventory?.armorInventory?.set(3, item)
        }
    }
}
