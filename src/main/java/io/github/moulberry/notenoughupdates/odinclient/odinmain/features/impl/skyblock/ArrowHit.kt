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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.ArrowEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.StringSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getMCTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ArrowHit : Module(
    name = "Arrow hit",
    desc = "Counts how many arrows you hit in certain time periods."
) {
    private val resetOnNumber by BooleanSetting("Reset on number", false, desc = "Reset the arrow count after a certain number of arrows.")
    private val resetCount by StringSetting("Reset count", 999999.toString(), 16, desc = "The amount of arrows to hit before resetting the count.")
    private val resetOnWorldLoad by BooleanSetting("Reset on world load", true, desc = "Reset the arrow count when you join a world.")
    val resetOnDragons by BooleanSetting("Reset on dragon spawn", true, desc = "Reset the arrow count when a m7 dragon has spawned.")

    private var arrowCount = 0

    private val hud by HudSetting("Display", 10f, 10f, 2f, false) {
        if (it) {
            RenderUtils.drawText("156", 0f, 2f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("156").toFloat() to 12f
        } else {
            RenderUtils.drawText("$arrowCount", 0f, 2f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("$arrowCount").toFloat() to 12f
        }
    }
    init {
        onWorldLoad { if (resetOnWorldLoad) arrowCount = 0  }
    }

    @SubscribeEvent
    fun onArrowHit(event: ArrowEvent.Hit) {
        arrowCount++
        if (arrowCount >= (resetCount.toIntOrNull() ?: 9999) && resetOnNumber) arrowCount = 0
    }

    fun onDragonSpawn() {
        arrowCount = 0
    }

    override fun onKeybind() {
        if (Minecraft.getMinecraft().currentScreen != null || !enabled) return
        arrowCount = 0
    }
}
