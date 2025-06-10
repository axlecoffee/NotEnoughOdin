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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.hud.HudElement
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.hud.Render

/**
 * @author Stivais, Bonsai
 */
class HudSetting( // todo redo
    name: String,
    hud: HudElement,
    val displayToggle: Boolean = false,
    desc: String = "",
    hidden: Boolean = false
) : Setting<HudElement>(name, hidden, desc) {

    constructor(name: String, x: Float, y: Float, scale: Float = 1f, toggleable: Boolean, draw: Render) :
            this(name, HudElement(x, y, toggleable, scale, draw, name), toggleable)

    override val default: HudElement = hud

    /**
     * Not intended to be used.
     */
    override var value: HudElement = default

    inline var enabled: Boolean
        get() = value.enabled
        set(value) {
            this.value.enabled = value
        }

    init {
        if (!displayToggle) value.enabled = true
    }
}
