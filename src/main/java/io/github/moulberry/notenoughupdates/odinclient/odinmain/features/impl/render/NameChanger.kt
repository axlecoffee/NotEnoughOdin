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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.StringSetting
import net.minecraft.client.Minecraft

object NameChanger : Module(
    name = "Name Changer",
    desc = "Replaces your name with the given nick, color codes work (&)."
) {
    private val nick by StringSetting("Nick", "Odin", 32, desc = "The nick to replace your name with.")

    @JvmStatic
    fun modifyString(string: String?): String? {
        if (!enabled || string == null) return string
        return string.replace(Minecraft.getMinecraft().session.username, nick.replace("&", "§").replace("$", ""))
    }
}
