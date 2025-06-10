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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.melodyColumColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.melodyCorrectRowColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.melodyPressColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.melodyPressColumColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.melodyRowColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.item.Item
import kotlin.math.ceil

object MelodyGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-325, -250, 650, 500, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Click the button on time!", -320, -238, Colors.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-248, -210, getTextWidth("Click the button on time!", 20f), 3, Colors.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Click the button on time!", 0, -238, Colors.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Click the button on time!", 20f) / 2, -210, getTextWidth("Click the button on time!", 20f), 3, Colors.WHITE, radius = 5f)
        }

        TerminalSolver.currentTerm?.solution?.forEach { pane ->
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val colorMelody = when {
                pane / 9 == 0 || pane / 9 == 5 -> melodyColumColor
                (pane % 9) in 1..5  -> melodyRowColor
                else -> melodyPressColor
            }
            if ((pane % 9) in 1..5 && pane / 9 != 0 && pane / 9 != 5) {
                roundedRectangle((-163 + ((gap-20).unaryPlus() * 0.5)) -1*70, -115 + row * 70,350 - gap, 70 - gap, melodyCorrectRowColor)
            }
            val box = BoxWithClass(ceil(-163 + ((gap-20).unaryPlus() * 0.5)) + col * 70, -115 + row * 70, 70 - gap, 70 - gap)
            roundedRectangle(box, colorMelody)
        }

        TerminalSolver.currentTerm?.let {
            it.items.forEachIndexed { index, item ->
                if (Item.getIdFromItem(item?.item) != 159) return@forEachIndexed
                val row = index / 9 - 1
                val col = index % 9 - 2
                val box = BoxWithClass(ceil(-163 + ((gap - 20).unaryPlus() * 0.5)) + col * 70, -115 + row * 70, 70 - gap, 70 - gap)
                if (index !in it.solution) roundedRectangle(box, melodyPressColumColor)
                itemIndexMap[index] = Box(
                    box.x.toFloat() * customScale + Minecraft.getMinecraft().displayWidth / 2,
                    box.y.toFloat() * customScale + Minecraft.getMinecraft().displayHeight / 2,
                    box.w.toFloat() * customScale,
                    box.h.toFloat() * customScale
                )
            }
        }
    }
}
