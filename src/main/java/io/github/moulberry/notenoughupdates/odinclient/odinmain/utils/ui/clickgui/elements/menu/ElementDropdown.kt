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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.DropdownSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.font.OdinFont
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.LinearAnimation
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.Element
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.ElementType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.elements.ModuleButton
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.HoverHandler
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.isAreaHovered
import net.minecraft.client.renderer.texture.DynamicTexture

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementDropdown(parent: ModuleButton, setting: DropdownSetting) : Element<DropdownSetting>(
    parent, setting, ElementType.DROPDOWN
) {
    private val linearAnimation = LinearAnimation<Float>(200)

    private val hover = HoverHandler(0, 150)

    override val isHovered: Boolean get() =
        isAreaHovered(x, y, w, h)

    override fun draw() {
        roundedRectangle(x, y, w, h, elementBackground)
        text(name, x + 6f, y + h / 2f, textColor, 12f, OdinFont.REGULAR)

        val rotation = linearAnimation.get(180f, 90f, !setting.value)
        rotate(rotation, x + w - 20f, y + 15f, 0f, 0f, 0f, 1f)
        drawDynamicTexture(arrow, x + w - 35f, y, 30f, 30f)
        rotate(-rotation, x + w - 20f, y + 15f, 0f, 0f, 0f, 1f)
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (linearAnimation.start()) {
                setting.enabled = !setting.enabled
                parent.updateElements()
                return true
            }
        }
        return false
    }

    companion object {
        val arrow = DynamicTexture(RenderUtils.loadBufferedImage("/assets/odinmain/clickgui/arrow.png"))
    }
}
