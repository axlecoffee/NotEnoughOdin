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


/**
 * Setting that lets you pick between an array of strings.
 * @author Aton, Stivais
 */
class SelectorSetting(
    name: String,
    defaultSelected: String,
    var options: ArrayList<String>,
    desc: String,
    hidden: Boolean = false
) : Setting<Int>(name, hidden, desc), Saving {

    override val default: Int = optionIndex(defaultSelected)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    var index: Int = optionIndex(defaultSelected)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
        }

    var selected: String
        get() = options[index]
        set(value) {
            index = optionIndex(value)
        }

    override fun write(): JsonElement {
        return JsonPrimitive(selected)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            selected = it
        }
    }

    private fun optionIndex(string: String): Int {
        return options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)
    }
}
