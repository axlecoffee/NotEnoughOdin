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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.SplitsManager.currentSplits
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.SplitsManager.getAndUpdateSplitsTimes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.formatTime
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextHeight
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors

object Splits : Module(
    name = "Splits",
    desc = "Provides visual timers for Kuudra and Dungeons."
) {
    private val hud by HudSetting("Splits Display HUD", 10f, 10f, 1f, true) { example ->
        if (example) {
            repeat(5) { i ->
                RenderUtils.drawText("Split $i:", 1f, 9f + i * getMCTextHeight(), 1f, Colors.WHITE, shadow = true, center = false)
            }
            return@HudSetting getMCTextWidth("Split 0: 0h 00m 00s") + 2f to 80f
        }

        val (times, current) = getAndUpdateSplitsTimes(currentSplits)
        if (currentSplits.splits.isEmpty()) return@HudSetting 0f to 0f
        val x = getMCTextWidth("Professor: 0m 00s")
        currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
            val time = formatTime(if (index >= times.size) 0 else times[index], numbersAfterDecimal)
            RenderUtils.drawText(split.name, 1f, 9f + index * getMCTextHeight(), 1f, Colors.WHITE, shadow = true, center = false)
            RenderUtils.drawText(time, x.toFloat(), 9f + index * getMCTextHeight(), 1f, Colors.WHITE, shadow = true, center = false)
        }
        if (bossEntrySplit && currentSplits.splits.size > 3) {
            RenderUtils.drawText("§9Boss Entry", 1f, (currentSplits.splits.size) * getMCTextHeight().toFloat(), 1f, Colors.WHITE, shadow = true, center = false)
            RenderUtils.drawText(formatTime(times.take(3).sum(), numbersAfterDecimal), x.toFloat(), (currentSplits.splits.size) * getMCTextHeight().toFloat(), 1f, Colors.WHITE, shadow = true, center = false)
        }
        getMCTextWidth("Split 0: 0h 00m 00s") + 2f to 80f
    }
    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, desc = "Split for boss entry.")
    val sendSplits by BooleanSetting("Send Splits", true, desc = "Send splits to chat.")
    val sendOnlyPB by BooleanSetting("Send Only PB", false, desc = "Send only personal bests.")
    private val numbersAfterDecimal by NumberSetting("Numbers After Decimal", 2, 0, 5, 1, desc = "Numbers after decimal in time.")
}
