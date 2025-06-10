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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.EtherWarpHelper
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.getAbilityCooldown
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isAir
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isHolding
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toVec3
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GyroWand : Module(
    name = "Gyro Wand",
    desc = "Shows area of effect and cooldown of the Gyrokinetic Wand."
) {
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_PURPLE.withAlpha(0.5f), allowAlpha = true, desc = "The color of the Gyrokinetic Wand range.")
    private val thickness by NumberSetting("Thickness", 0.4f, 0, 10, 0.05f, desc = "The thickness of the Gyrokinetic Wand range.")
    private val steps by NumberSetting("Smoothness", 40, 20, 80, 1, desc = "The amount of steps to use when rendering the Gyrokinetic Wand range.")
    private val showCooldown by BooleanSetting("Show Cooldown", true, desc = "Shows the cooldown of the Gyrokinetic Wand.")
    private val cooldownColor by ColorSetting("Cooldown Color", Colors.MINECRAFT_RED.withAlpha(0.5f), allowAlpha = true, desc = "The color of the cooldown of the Gyrokinetic Wand.").withDependency { showCooldown }

    private val gyroCooldown = Clock(30_000L)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!isHolding("GYROKINETIC_WAND")) return
        val position = EtherWarpHelper.getEtherPos(distance = 25.0).pos?.takeIf { !isAir(it) }?.toVec3() ?: return

        Renderer.drawCylinder(
            position.addVector(0.5, 1.0, 0.5),
            10f, 10f - thickness, 0.2f,
            steps, 1, 0f, 90f, 90f, if (showCooldown && !gyroCooldown.hasTimePassed(getAbilityCooldown(30_000L))) cooldownColor else color
        )
    }

    init {
        onMessage(Regex("(?s)(.*(-\\d+ Mana \\(Gravity Storm\\)).*)")) {
            gyroCooldown.update()
        }
    }
}
