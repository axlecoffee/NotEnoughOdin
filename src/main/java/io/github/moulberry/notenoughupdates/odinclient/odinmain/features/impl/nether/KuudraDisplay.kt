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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderBoundingBox
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.round
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils.kuudraEntity
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object KuudraDisplay : Module(
    name = "Kuudra Display",
    desc = "Displays information about Kuudra entity itself."
) {
    private val highlightKuudra by BooleanSetting("Highlight Kuudra", true, desc = "Highlights the kuudra entity.")
    private val kuudraColor by ColorSetting("Kuudra Color", Colors.MINECRAFT_RED, true, desc = "Color of the kuudra highlight.").withDependency { highlightKuudra }
    private val thickness by NumberSetting("Thickness", 3f, 0.1, 8f, desc = "Thickness of the kuudra highlight.").withDependency { highlightKuudra }
    private val kuudraSpawnAlert by BooleanSetting("Kuudra Spawn Alert", true, desc = "Alerts you where kuudra spawns.")
    private val kuudraHPDisplay by BooleanSetting("Kuudra HP", true, desc = "Renders kuudra's hp on him.")
    private val healthSize by NumberSetting("Health Size", 0.3f, 0.1f, 1.0f, 0.1, desc = "Size of the health display.").withDependency { kuudraHPDisplay }
    private val healthFormat by BooleanSetting("Health Format", true, desc = "Format of the health display (true for Absolute, false for Percentage).").withDependency { kuudraHPDisplay }
    private val scaledHealth by BooleanSetting("Use Scaled", true, desc = "Use scaled health display.").withDependency { kuudraHPDisplay }
    private val hud by HudSetting("Health Display", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("§a99.975M/240M", 1f, 1f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("99.975k/100k") + 2f to 12f
        } else {
            if (!KuudraUtils.inKuudra) return@HudSetting 0f to 0f

            RenderUtils.drawText(getCurrentHealthDisplay(), 1f, 1f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("99.975k/100k") + 2f to 12f
        }
    }

    private var kuudraHP = 100000f
    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra) return

        kuudraEntity?.let {
            if (highlightKuudra)
                Renderer.drawBox(it.renderBoundingBox, kuudraColor, depth = false, fillAlpha = 0, outlineWidth = thickness)

            if (kuudraHPDisplay)
                Renderer.drawStringInWorld(getCurrentHealthDisplay(), it.positionVector.addVec(y = 10), Colors.WHITE, depth = false, scale = healthSize, shadow = true)
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !KuudraUtils.inKuudra) return

        kuudraHP = kuudraEntity?.health ?: return
        val kuudraPos = kuudraEntity?.positionVector ?: return
        if (kuudraSpawnAlert && kuudraHP in 24900f..25000f) {
            when {
                kuudraPos.xCoord < -128 -> "§c§lRIGHT"
                kuudraPos.xCoord > -72 -> "§2§lLEFT"
                kuudraPos.zCoord > -84 -> "§a§lFRONT"
                kuudraPos.zCoord < -132 -> "§4§lBACK"
                else -> null
            }?.let { PlayerUtils.alert(it, playSound = false) }
        }
    }

    private fun getCurrentHealthDisplay(): String {
        val color = when {
            kuudraHP > 99000 -> "§a"
            kuudraHP > 75000 -> "§2"
            kuudraHP > 50000 -> "§e"
            kuudraHP > 25000 -> "§6"
            kuudraHP > 10000 -> "§c"
            else -> "§4"
        }
        val health = kuudraHP / 1000

        return when {
            // Scaled
            kuudraHP <= 25000 && scaledHealth && KuudraUtils.kuudraTier == 5 -> "$color${(health * 9.6).round(2)}M§7/§a240M §c❤"
            // Percentage
            healthFormat -> "$color${health}§a% §c❤"
            // Exact
            else -> "$color${health}K§7/§a100k §c❤"
        }
    }
}
