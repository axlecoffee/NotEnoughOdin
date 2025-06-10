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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinclient.utils.skyblock.PlayerUtils.leftClick
import io.github.moulberry.notenoughupdates.odinclient.odinclient.utils.skyblock.PlayerUtils.rightClick
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isHolding
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object AutoClicker : Module(
    name = "Auto Clicker",
    desc = "Auto clicker with options for left-click, right-click, or both."
) {
    private val terminatorOnly by BooleanSetting("Terminator Only", true, desc = "Only click when the terminator and right click are held.")
    private val cps by NumberSetting("Clicks Per Second", 5.0f, 3.0, 15.0, .5, desc = "The amount of clicks per second to perform.").withDependency { terminatorOnly }

    private val enableLeftClick by BooleanSetting("Enable Left Click", true, desc = "Enable auto-clicking for left-click.").withDependency { !terminatorOnly }
    private val enableRightClick by BooleanSetting("Enable Right Click", true, desc = "Enable auto-clicking for right-click.").withDependency { !terminatorOnly }
    private val leftCps by NumberSetting("Left Clicks Per Second", 5.0f, 3.0, 15.0, .5, desc = "The amount of left clicks per second to perform.").withDependency { !terminatorOnly }
    private val rightCps by NumberSetting("Right Clicks Per Second", 5.0f, 3.0, 15.0, .5, desc = "The amount of right clicks per second to perform.").withDependency { !terminatorOnly }
    private val leftClickKeybind by KeybindSetting("Left Click", Keyboard.KEY_NONE, description = "The keybind to hold for the auto clicker to click left click.").withDependency { !terminatorOnly }
    private val rightClickKeybind by KeybindSetting("Right Click", Keyboard.KEY_NONE, description = "The keybind to hold for the auto clicker to click right click.").withDependency { !terminatorOnly }

    private var nextLeftClick = .0
    private var nextRightClick = .0

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (Minecraft.getMinecraft().currentScreen != null) return
        val nowMillis = System.currentTimeMillis()
        if (terminatorOnly) {
            if (!isHolding("TERMINATOR") || !Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown) return
            if (nowMillis < nextRightClick) return
            nextRightClick = nowMillis + ((1000 / cps) + ((Math.random() - .5) * 60.0))
            leftClick()
        } else {
            if (enableLeftClick && leftClickKeybind.isDown() && nowMillis >= nextLeftClick) {
                nextLeftClick = nowMillis + ((1000 / leftCps) + ((Math.random() - .5) * 60.0))
                leftClick()
            }

            if (enableRightClick && rightClickKeybind.isDown() && nowMillis >= nextRightClick) {
                nextRightClick = nowMillis + ((1000 / rightCps) + ((Math.random() - .5) * 60.0))
                rightClick()
            }
        }
    }
}
