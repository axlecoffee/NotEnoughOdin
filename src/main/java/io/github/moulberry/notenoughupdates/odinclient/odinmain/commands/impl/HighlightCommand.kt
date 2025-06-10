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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.CustomHighlight.currentEntities
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.CustomHighlight.highlightMap
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import kotlin.text.toHexString


@OptIn(ExperimentalStdlibApi::class)
val highlightCommand = Commodore("highlight") {
    val colorRegex = Regex("^(.*?)(?:\\s+#?([0-9a-fA-F]{6}|[0-9a-fA-F]{8}))?$")

    literal("add").runs { input: GreedyString ->
        val inputString = input.string.trim()
        val matchResult = colorRegex.matchEntire(inputString) ?: return@runs modMessage("Invalid format. Use: /highlight add <mob name> [#hexcolor]")

        val (mobName, colorCode) = matchResult.destructured
        val mobNameTrimmed = mobName.trim()
        val lowercase = mobNameTrimmed.lowercase()

        if (mobNameTrimmed.isEmpty()) return@runs modMessage("Mob name cannot be empty.")

        if (highlightMap.any { it.key == lowercase }) return@runs modMessage("$mobNameTrimmed is already in the highlight list.")

        if (colorCode.isNotEmpty() && !Regex("^[0-9a-fA-F]{6}|[0-9a-fA-F]{8}$").matches(colorCode)) return@runs modMessage("Invalid color format. Use #RRGGBB or #RRGGBBAA.")

        val color = if (colorCode.isNotEmpty()) {
            try {
                Color(colorCode.padEnd(8, 'f'))
            } catch (e: Exception) {
                modMessage("Invalid color format. Use #RRGGBB or #RRGGBBAA.")
                null
            }
        } else null

        highlightMap[lowercase] = color
        modMessage("Added $mobNameTrimmed to the highlight list${if (colorCode.isNotEmpty()) " with color #$colorCode" else ""}.")
        Config.save()
    }

    literal("remove").runs { mob: GreedyString ->
        val lowercase = mob.string.lowercase()
        if (highlightMap.none { it.key == lowercase }) return@runs modMessage("$mob isn't in the highlight list.")

        modMessage("Removed $mob from the highlight list.")
        highlightMap.remove(lowercase)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Highlight list cleared.")
        highlightMap.clear()
        currentEntities.clear()
        Config.save()
    }

    literal("list").runs {
        if (highlightMap.isEmpty()) return@runs modMessage("Highlight list is empty")
        modMessage("Highlight list:\n${highlightMap.entries.joinToString("\n") {
            "${it.key} - ${it.value?.rgba?.toHexString() ?: "default color"}"
        }}")
    }
}
