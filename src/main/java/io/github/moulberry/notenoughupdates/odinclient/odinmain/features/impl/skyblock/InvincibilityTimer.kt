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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.ChatPacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent.DrawSlotOverlay
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.skyblockID
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    desc = "Timer to show how long you have left Invincible."
)  {
    private val showCooldown by BooleanSetting("Show Cooldown", true, desc = "Shows the cooldown of the mask.")
    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", true, desc = "Announces when you get invincibility.")
    private val hud by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("${if(showPrefix) "§bBonzo§f: " else ""}59t", 1f, 1f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("Bonzo: 59t") + 2f to 12f
        } else {
            if (invincibilityTime.time <= 0) return@HudSetting 0f to 0f
            val invincibilityType = if (invincibilityTime.type == "Bonzo") "§bBonzo§f:" else if (invincibilityTime.type == "Phoenix") "§6Phoenix§f:" else "§5Spirit§f:"

            RenderUtils.drawText("${if (showPrefix) invincibilityType else ""} ${invincibilityTime.time}t", 1f, 1f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("Bonzo: 59t") + 2f to 12f
        }
    }
    private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timer.")

    private data class Timer(var time: Int, var type: String)
    private var invincibilityTime = Timer(0, "")
    private val bonzoMaskRegex = Regex("^Your (?:. )?Bonzo's Mask saved your life!$")
    private val phoenixPetRegex = Regex("^Your Phoenix Pet saved you from certain death!$")
    private val spiritPetRegex = Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$")

    private var spiritMaskProc = 0L
    private var bonzoMaskProc = 0L

    init {
        onPacket<S32PacketConfirmTransaction> {
            invincibilityTime.time--
        }

        onWorldLoad {
            invincibilityTime = Timer(0, "")
            spiritMaskProc = 0L
            bonzoMaskProc = 0L
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val type = when {
            event.message.matches(bonzoMaskRegex) -> {
                bonzoMaskProc = System.currentTimeMillis()
                "Bonzo"
            }
            event.message.matches(spiritPetRegex) -> {
                spiritMaskProc = System.currentTimeMillis()
                "Spirit"
            }
            event.message.matches(phoenixPetRegex) -> "Phoenix"
            else -> return
        }

        if (invincibilityAnnounce) partyMessage("$type Procced")
        invincibilityTime = Timer(60, type)
    }

    @SubscribeEvent
    fun onRenderSlotOverlay(event: DrawSlotOverlay) {
        if (!LocationUtils.isInSkyblock || !showCooldown) return
        val durability = when (event.stack.skyblockID) {
            "BONZO_MASK", "STARRED_BONZO_MASK" -> (System.currentTimeMillis() - bonzoMaskProc) / 180_000.0
            "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> (System.currentTimeMillis() - spiritMaskProc) / 30_000.0
            else -> return
        }.takeIf { it < 1.0 } ?: return
        RenderUtils.renderDurabilityBar(event.x ?: return, event.y ?: return, durability)
    }
}
