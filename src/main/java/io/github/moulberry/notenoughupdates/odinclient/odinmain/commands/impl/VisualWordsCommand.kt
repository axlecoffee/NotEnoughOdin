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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.VisualWords
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage

val visualWordsCommand = Commodore("visualwords") {

    literal("add").runs { command: GreedyString ->
        if (!command.string.contains("replace")) return@runs modMessage("You are missing the 'replace' keyword")
        val actualText = command.string.replace("&", "§").substringBefore("replace").trim()
        val replaceText = command.string.replace("&", "§").substringAfter("replace").trim()
        val replaceTextForChat = "\"${actualText.substring(0, actualText.length / 2)}⛏${actualText.substring(actualText.length / 2)}\""
        VisualWords.wordsMap[actualText] = replaceText
        modMessage("Replacing $replaceTextForChat with \"$replaceText\"")
        Config.save()
    }

    literal("remove").runs { command: GreedyString ->
        val actualText = command.string.replace("&", "§").substringBefore("replace").trim()
        if (!VisualWords.wordsMap.containsKey(actualText)) return@runs modMessage("This element is not in the list")
        VisualWords.wordsMap.remove(actualText)
        modMessage("Removed \"$actualText\" from the list")
        Config.save()
    }

    literal("clear").runs {
        VisualWords.wordsMap.clear()
        modMessage("Visual Word list cleared")
        Config.save()
    }

    literal("list").runs {
        if (VisualWords.wordsMap.isEmpty()) return@runs modMessage("Visual Word list is empty")
        for (actualText in VisualWords.wordsMap.keys) {
            val replaceText = VisualWords.wordsMap[actualText]
            val actualTextForChat = "\"${actualText.substring(0, actualText.length / 2)}⛏${actualText.substring(actualText.length / 2)}\""
            modMessage("$actualTextForChat -> \"$replaceText\"")
        }
    }

}
