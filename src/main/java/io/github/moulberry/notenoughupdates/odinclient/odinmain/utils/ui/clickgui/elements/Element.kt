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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.ClickGUI
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.HoverHandler
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.isAreaHovered

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
open class Element<S : Setting<*>>(val parent: ModuleButton, val setting: S, type: ElementType) {

    inline val name: String
        get () = setting.name

    val w: Float
        inline get() = parent.width

    var h: Float = when (type) {
        ElementType.SLIDER -> 40f
        else -> DEFAULT_HEIGHT
    }

    var extended = false
    var listening = false

    val x: Float
        inline get() = parent.x

    var y: Float = 0f
        get() = field + parent.y

    open val isHovered
        get() = isAreaHovered(x, y, w, h)

    private val hoverHandler = HoverHandler(1250, 200)

    open fun render(): Float {
        hoverHandler.handle(x, y, w, h)
        if (hoverHandler.percent() > 0) {
            ClickGUI.setDescription(setting.description, x + w + 10f, y, hoverHandler)
        }
        draw()
        return h
    }

    protected open fun draw() {}

    open fun mouseClicked(mouseButton: Int): Boolean = isAreaHovered(x, y, w, h)
    open fun mouseReleased(state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean = false

    companion object {
        const val DEFAULT_HEIGHT = 32f
    }
}
