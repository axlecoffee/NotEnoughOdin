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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ClickGUIModule
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color

object ColorUtil {

    inline val clickGUIColor: Color
        get() = ClickGUIModule.color

    val buttonColor = Color(38, 38, 38)

    val moduleButtonColor = Color(26, 26, 26)
    val elementBackground = Color(37, 38, 38, 0.7f)
    val textColor = Color(239, 239, 239)

    /**
     * Changes or creates a new color with the given alpha. (There is no checks if alpha is in valid range for now.)
     */
    fun Color.withAlpha(alpha: Float, newInstance: Boolean = true): Color {
        if (!newInstance) {
            this.alphaFloat = alpha
            return this
        }
        return Color(red, green, blue, alpha)
    }

    fun Color.multiplyAlpha(factor: Float): Color {
        return Color(red, green, blue, (this.alphaFloat * factor).coerceIn(0f, 1f))
    }

    fun Color.brighter(factor: Float = 1.3f): Color {
        return Color(hue, saturation, (brightness * factor.coerceAtLeast(1f)).coerceAtMost(1f),
            this.alphaFloat
        )
    }

    fun Color.darker(factor: Float = 0.7f): Color {
        return Color(hue, saturation, brightness * factor, this.alphaFloat)
    }

    fun Color.darkerIf(condition: Boolean, factor: Float = 0.7f): Color {
        return if (condition) darker(factor) else this
    }

    fun Color.hsbMax(): Color {
        return Color(hue, 1f, 1f)
    }
}
