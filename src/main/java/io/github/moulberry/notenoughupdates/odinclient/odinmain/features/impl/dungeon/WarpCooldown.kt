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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.StringSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toFixed
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors

object WarpCooldown : Module(
    name = "Warp Cooldown",
    desc = "Displays the time until you can warp into a new dungeon."
) {
    private val announceKick: Boolean by BooleanSetting("Announce Kick", false, desc = "Announce when you get kicked from skyblock.")
    private val kickText: String by StringSetting("Kick Text", "Kicked!", desc = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val hud by HudSetting("Warp Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("§eWarp: §a30s", 1f, 1f, 1f, Colors.WHITE, shadow = true)
            getMCTextWidth("Warp: 30s") + 2f to 12f
        } else {
            if (warpTimer.timeLeft() <= 0) return@HudSetting 0f to 0f
            RenderUtils.drawText("§eWarp: §a${(warpTimer.timeLeft() / 1000f).toFixed()}s", 1f, 1f, 1f, Colors.WHITE, shadow = true)
            getMCTextWidth("§eWarp: §a30s") + 2f to 12f
        }
    }

    private var warpTimer = Clock(30_000L)

    init {
        onMessage(Regex("^You were kicked while joining that server!$"), { enabled && announceKick }) {
            partyMessage(kickText)
        }

        onMessage(Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")) {
            warpTimer.updateCD()
        }
    }
}
