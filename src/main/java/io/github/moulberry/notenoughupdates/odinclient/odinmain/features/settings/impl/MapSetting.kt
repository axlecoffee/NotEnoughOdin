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
import com.google.gson.reflect.TypeToken
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Saving
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting
import java.lang.reflect.Type

/**
 * This setting is only designed to store values as a map, and shouldn't be rendered in the gui.
 *
 * @author Stivais
 */
class MapSetting<K : Any?, V : Any?, T : MutableMap<K, V>>(
    name: String,
    override val default: T,
    private val type: Type,
) : Setting<T>(name, true, description = ""), Saving {

    override var value: T = default

    override fun write(): JsonElement {
        return gson.toJsonTree(value)
    }

    override fun read(element: JsonElement?) {
        element?.let {
            val temp = gson.fromJson<Map<K, V>>(it, type)
            value.clear()
            value.putAll(temp)
        }
    }
}

inline fun <reified K : Any?, reified V : Any?, reified T : MutableMap<K, V>> MapSetting(
    name: String,
    default: T,
): MapSetting<K, V, T> = MapSetting(name, default, object : TypeToken<T>() {}.type)
