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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.MapSetting

object VisualWords : Module(
    name = "Visual Words",
    desc = "Replaces words in the world with other words. (/visualwords)"
) {
    val wordsMap by MapSetting("wordsMap", mutableMapOf<String, String>())

    @JvmStatic
    fun replaceText(text: String?): String? {
        if (text == null) return text
        var replacedText = RandomPlayers.replaceText(text)
        if (!enabled) return replacedText
        for (actualText in wordsMap.keys) {
            replacedText = wordsMap[actualText]?.let { replacedText?.replace(actualText, it) }
        }
        return replacedText
    }
}
