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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalTypes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoTerms : Module(
    name = "Auto Terms",
    desc = "Automatically solves terminals.",
    tag = TagType.RISKY
) {
    private val autoDelay by NumberSetting("Delay", 170L, 130, 300, unit = "ms", desc = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 300, 500, unit = "ms", desc = "Delay before first click.")
    private val breakThreshold by NumberSetting("Break Threshold", 500L, 350L, 1000L, 10L, unit = "ms", desc = "Time before breaking the click.")
    private val disableMelody by BooleanSetting("Disable Melody", false, desc = "Disables melody terminals.")
    private var lastClickTime = 0L
    private var firstClick = true

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) = with(TerminalSolver.currentTerm) {
        if (event.phase != TickEvent.Phase.START) return
        if (this?.type == null) {
            lastClickTime = System.currentTimeMillis()
            firstClick = true
            return
        }

        if (firstClick && (System.currentTimeMillis() - lastClickTime < firstClickDelay)) return

        if (System.currentTimeMillis() - lastClickTime < autoDelay) return

        if (System.currentTimeMillis() - lastClickTime > breakThreshold) isClicked = false

        if (solution.isEmpty() || (disableMelody && type == TerminalTypes.MELODY) || isClicked) return

        val item = solution.firstOrNull() ?: return

        lastClickTime = System.currentTimeMillis()
        firstClick = false

        when (type) {
            TerminalTypes.RUBIX ->
                click(item, if (solution.count { it == item } >= 3) ClickType.Right else ClickType.Middle, false)

            TerminalTypes.MELODY ->
                click(solution.find { it % 9 == 7 } ?: return, ClickType.Middle, false)

            else -> click(item, ClickType.Middle, false)
        }
    }
}
