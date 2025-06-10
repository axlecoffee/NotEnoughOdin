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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.p3

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalTypes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termGUI.TermGui
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.mouseX
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.mouseY
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoverTerms : Module(
    name = "Hover Terms",
    desc = "Clicks the hovered item in a terminal if it is correct.",
    tag = TagType.RISKY
) {
    private val triggerDelay by NumberSetting("Delay", 170L, 130, 300, unit = "ms", desc = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 300, 500, unit = "ms", desc = "Delay before first click.")
    private val triggerBotClock = Clock(triggerDelay)

    @SubscribeEvent(receiveCanceled = true)
    fun onDrawGuiContainer(event: GuiEvent.DrawGuiBackground) = with (TerminalSolver.currentTerm) {
        if (
            this?.type == null ||
            solution.isEmpty() ||
            !triggerBotClock.hasTimePassed(triggerDelay) ||
            System.currentTimeMillis() - timeOpened <= firstClickDelay ||
            isClicked
        ) return

        val hoveredItem =
            when {
                TerminalSolver.renderType == 3 && TerminalSolver.enabled -> TermGui.getHoveredItem(mouseX.toInt(), mouseY.toInt())
                else -> {
                    if (event.gui.slotUnderMouse?.inventory == Minecraft.getMinecraft().thePlayer?.inventory) return
                    event.gui.slotUnderMouse?.slotIndex
                }
            } ?: return

        when (type) {
            TerminalTypes.RUBIX -> {
                val needed = solution.count { it == hoveredItem } >= 3
                if (!canClick(hoveredItem, if (needed) 1 else 0)) return
                click(hoveredItem, if (needed) ClickType.Right else ClickType.Middle)
                triggerBotClock.update()
            }

            TerminalTypes.NUMBERS ->
                if (canClick(hoveredItem, 2)) {
                    click(hoveredItem, ClickType.Middle)
                    triggerBotClock.update()
                }

            TerminalTypes.MELODY ->
                if (canClick(hoveredItem, 0)) {
                    click(hoveredItem, ClickType.Left)
                    triggerBotClock.update()
                }

            TerminalTypes.PANES, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT -> {
                if (canClick(hoveredItem, 2)) {
                    click(hoveredItem, ClickType.Middle)
                    triggerBotClock.update()
                }
            }
            else -> return
        }
    }
}
