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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver.hideClicked
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.postAndCatch
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Box
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.isPointWithin
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

object CustomTermGui {
    fun render() {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 0f)
        GlStateManager.translate(Minecraft.getMinecraft().displayWidth / 2f, Minecraft.getMinecraft().displayHeight / 2f, 0f)
        GlStateManager.scale(TerminalSolver.customScale, TerminalSolver.customScale, 0f)
        TerminalSolver.currentTerm?.type?.gui?.render()
        GlStateManager.scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale, 0f)
        GlStateManager.translate(-Minecraft.getMinecraft().displayWidth / 2f, -Minecraft.getMinecraft().displayHeight / 2f, 0f)
        GlStateManager.scale(sr.scaleFactor.toDouble(), sr.scaleFactor.toDouble(), 0.0)
    }

    fun mouseClicked(x: Int, y: Int, button: Int) = TerminalSolver.currentTerm?.type?.gui?.mouseClicked(x, y, button)
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find { it.value.isPointWithin(x, y) }?.let { (slot, _) ->
            TerminalSolver.currentTerm?.let {
                if (System.currentTimeMillis() - it.timeOpened >= 300 && !GuiEvent.CustomTermGuiClick(slot, button).postAndCatch() && it.canClick(slot, button))
                    it.click(slot, if (button == 0) ClickType.Middle else ClickType.Right, hideClicked && !it.isClicked)
            }
        }
    }

    companion object {
        private var currentGui: TermGui? = null

        fun setCurrentGui(gui: TermGui) {
            currentGui = gui
        }

        fun getHoveredItem(x: Int, y: Int): Int? {
            return currentGui?.itemIndexMap?.entries?.find {
                it.value.isPointWithin(x, y)
            }?.key
        }
    }

    open fun render() {}
}

