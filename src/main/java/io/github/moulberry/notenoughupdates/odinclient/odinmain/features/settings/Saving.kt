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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings

import com.google.gson.JsonElement

/**
 * Used for settings that you want to save/load.
 *
 * @see io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
 */
internal interface Saving {
    /**
     * Used to update the setting from the json.
     */
    fun read(element: JsonElement?)

    /**
     * Used to create the json.
     */
    fun write(): JsonElement
}
