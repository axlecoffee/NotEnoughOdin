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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendCommand
import org.lwjgl.input.Keyboard

object CommandKeybinds : Module(
    name = "Command Keybinds",
    desc = "Various keybinds for common skyblock commands.",
    key = null
) {
    private val pets by KeybindSetting("Pets", Keyboard.KEY_NONE, description = "Opens the pets menu.").onPress {
        if (!enabled) return@onPress
        sendCommand("pets")
    }
    private val storage by KeybindSetting("Storage", Keyboard.KEY_NONE, description = "Opens the storage menu.").onPress {
        if (!enabled) return@onPress
        sendCommand("storage")
    }
    private val wardrobe by KeybindSetting("Wardrobe", Keyboard.KEY_NONE, description = "Opens the wardrobe menu.").onPress {
        if (!enabled) return@onPress
        sendCommand("wardrobe")
    }
    private val equipment by KeybindSetting("Equipment", Keyboard.KEY_NONE, description = "Opens the equipment menu.").onPress {
        if (!enabled) return@onPress
        sendCommand("equipment")
    }
    private val dhub by KeybindSetting("Dungeon Hub", Keyboard.KEY_NONE, description = "Warps to the dungeon hub.").onPress {
        if (!enabled) return@onPress
        sendCommand("warp dungeon_hub")
    }
}
