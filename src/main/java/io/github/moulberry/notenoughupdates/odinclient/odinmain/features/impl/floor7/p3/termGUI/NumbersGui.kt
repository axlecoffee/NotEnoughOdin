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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termGUI

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor2
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor3
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors

object NumbersGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-300, -125, 600, 225, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Click in order!", -295, -113, Colors.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -85, getTextWidth("Click in order!", 20f), 3, Colors.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Click in order!", 0, -113, Colors.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Click in order!", 20f) / 2, -85, getTextWidth("Click in order!", 20f), 3, Colors.WHITE, radius = 5f)
        }
        with (TerminalSolver.currentTerm ?: return) {
            solution.forEach { pane ->
                val row = pane / 9 - 1
                val col = pane % 9 - 2
                val amount = items[pane]?.stackSize ?: return@forEach
                val index = solution.indexOf(pane)
                val box = BoxWithClass((-163 + ((gap-20).unaryPlus() * 0.5)) + col * 70, -60 + row * 70, 70 - gap, 70 - gap)
                if (index in 0 until 3) {
                    val color = when (index) {
                        0    -> orderColor
                        1    -> orderColor2
                        else -> orderColor3
                    }
                    roundedRectangle(box, color)
                }
                itemIndexMap[pane] = Box(
                    box.x.toFloat() * customScale + Minecraft.getMinecraft().displayWidth / 2,
                    box.y.toFloat() * customScale + Minecraft.getMinecraft().displayHeight / 2,
                    box.w.toFloat() * customScale,
                    box.h.toFloat() * customScale
                )

                if (TerminalSolver.showNumbers && index != -1)
                    RenderUtils.drawText(amount.toString(), -163 + col * 70 + 26f , -60f + row * 70f + (27f - (textScale * 3f) - (gap * 0.5f)), 2f + textScale, Colors.WHITE, center = true)
            }
        }

    }
}
