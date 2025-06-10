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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors

object RubixGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-300, -175, 600, 300, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Change all to same color!", -295, -163, Colors.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -135, getTextWidth("Change all to same color!", 20f), 3, Colors.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Change all to same color!", 0, -163, Colors.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Change all to same color!", 20f) / 2, -135, getTextWidth("Change all to same color!", 20f), 3, Colors.WHITE, radius = 5f)
        }
        currentTerm?.solution?.distinct()?.forEach { pane ->
            val needed = currentTerm?.solution?.count { it == pane } ?: return@forEach
            val text = if (needed < 3) needed else (needed - 5)

            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass((-168 + ((gap -20).unaryPlus() * 0.5)) + col * 70, -110 + row * 70, 70 - gap, 70 - gap)

            if (text != 0) {
                val color = when (text) {
                    2 -> TerminalSolver.rubixColor2
                    1 -> TerminalSolver.rubixColor1
                    -2 -> TerminalSolver.oppositeRubixColor2
                    else -> TerminalSolver.oppositeRubixColor1
                }
                roundedRectangle(box, color)
                RenderUtils.drawText(text.toString(), -168 + col * 70 + 26f , -110f + row * 70f + (27f - (textScale * 3f) - (gap * 0.5f)), 2f + textScale, Colors.WHITE, center = true)
            }
            
            itemIndexMap[pane] = Box(
                box.x.toFloat() * customScale + Minecraft.getMinecraft().displayWidth / 2,
                box.y.toFloat() * customScale + Minecraft.getMinecraft().displayHeight / 2,
                box.w.toFloat() * customScale,
                box.h.toFloat() * customScale
            )
        }
    }
}
