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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Saving
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color

/**
 * Setting that represents a [Color].
 *
 * @author Stivais
 */
class ColorSetting(
    name: String,
    override val default: Color,
    var allowAlpha: Boolean = false,
    desc: String,
    hidden: Boolean = false
) : Setting<Color>(name, hidden, desc), Saving {

    override var value: Color = default.copy()

    var hue: Float
        get() = value.hue
        set(value) {
            this.value.hue = value.coerceIn(0f, 1f)
        }

    var saturation: Float
        get() = value.saturation
        set(value) {
            this.value.saturation = value.coerceIn(0f, 1f)
        }

    var brightness: Float
        get() = value.brightness
        set(value) {
            this.value.brightness = value.coerceIn(0f, 1f)
        }

    var alpha: Float
        get() = value.alphaFloat
        set(value) {
            this.value.alphaFloat = value.coerceIn(0f, 1f)
        }

    override fun read(element: JsonElement?) {
        if (element?.asString?.startsWith("#") == true) {
            value = Color(element.asString.drop(1))
        } else element?.asInt?.let {
            value = Color(it)
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive("#${this.value.hex}")
    }
}
