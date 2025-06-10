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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinclient.utils.skyblock.PlayerUtils.dropItem
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import org.lwjgl.input.Keyboard

object DungeonAbilities : Module(
    name = "Dungeon Abilities",
    desc = "Automatically uses your ability in dungeons."
) {
    private val autoUlt by BooleanSetting("Auto Ult", false, desc = "Automatically uses your ultimate ability whenever needed.")
    private val abilityKeybind by KeybindSetting("Ability Keybind", Keyboard.KEY_NONE, description = "Keybind to use your ability.").onPress {
        if (!DungeonUtils.inDungeons || !enabled) return@onPress
        dropItem(dropAll = true)
    }

    init {
        onMessage(Regex("⚠ Maxor is enraged! ⚠"), { enabled && autoUlt }) {
            dropItem()
            modMessage("§aUsing ult!")
        }

        onMessage(Regex("\\[BOSS] Goldor: You have done it, you destroyed the factory…"), { enabled && autoUlt }) {
            dropItem()
            modMessage("§aUsing ult!")
        }

        onMessage(Regex("\\[BOSS] Sadan: My giants! Unleashed!"), { enabled && autoUlt }) {
            dropItem(delay = 25)
            modMessage("§aUsing ult!")
        }
    }
}
