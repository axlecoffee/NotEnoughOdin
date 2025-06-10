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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.menu

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ActionSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.font.OdinFont
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.TextAlign
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.TextPos
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.roundedRectangle
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.text
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.Element
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.ElementType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.ModuleButton
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.darker
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementAction(parent: ModuleButton, setting: ActionSetting) : Element<ActionSetting>(parent, setting, ElementType.ACTION) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + 20f, y, w - 40f, h - 10f)

// todo: improve this
    override fun draw() {
        roundedRectangle(x, y, w, h, elementBackground)
        text(name, x + w / 2f, y + h / 2f, if (isHovered) textColor.darker() else textColor, 12f , OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle)
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            setting.action()
            return true
        }
        return false
    }
}
