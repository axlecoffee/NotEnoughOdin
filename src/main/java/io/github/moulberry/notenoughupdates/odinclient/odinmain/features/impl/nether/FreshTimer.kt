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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.runIn
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toFixed
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft

object FreshTimer : Module(
    name = "Fresh Timer",
    desc = "Shows the time until fresh timer."
){
    private val notifyFresh by BooleanSetting("Notify Fresh", true, desc = "Notifies your party when you get fresh timer.")
    val highlightFresh by BooleanSetting("Highlight Fresh", true, desc = "Highlights fresh timer users.")
    val highlightFreshColor by ColorSetting("Highlight Fresh Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the highlight.").withDependency { highlightFresh }
    private val freshTimerHUDColor by ColorSetting("Fresh Timer Color", Colors.MINECRAFT_GOLD, true, desc = "Color of the fresh timer HUD.")
    private val hud by HudSetting("Fresh timer HUD", 10f, 10f, 1f, true) { example ->
        if (example) {
            RenderUtils.drawText("Fresh§f: 9s", 1f, 1f, 1f, freshTimerHUDColor, shadow = true)
            getMCTextWidth("Fresh: 9s") + 2f to 12f
        } else {
            val player = KuudraUtils.kuudraTeammates.find { teammate -> teammate.playerName == Minecraft.getMinecraft().thePlayer.name } ?: return@HudSetting 0f to 0f
            val timeLeft = (10000L - (System.currentTimeMillis() - player.eatFreshTime)).takeIf { it > 0 } ?: return@HudSetting 0f to 0f
            if (player.eatFresh && KuudraUtils.phase == 2)
                RenderUtils.drawText("Fresh§f: ${(timeLeft / 1000f).toFixed()}s", 1f, 1f, 1f, highlightFreshColor, shadow = true)

            getMCTextWidth("Fresh: 10s") + 2f to 12f
        }
    }

    init {
        onMessage(Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!$")) {
            val teammate = KuudraUtils.kuudraTeammates.find { it.playerName == Minecraft.getMinecraft().thePlayer.name } ?: return@onMessage
            teammate.eatFreshTime = System.currentTimeMillis()
            teammate.eatFresh = true
            if (notifyFresh) modMessage("Fresh tools has been activated")
            if (notifyFresh) partyMessage("FRESH")
            runIn(200) {
                if (notifyFresh) modMessage("Fresh tools has expired")
                teammate.eatFresh = false
            }
        }
    }
}
