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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock.ChatCommands.blacklist
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage

val chatCommandsCommand = Commodore("chatcommandslist", "cclist", "chatclist", "ccommandslist") {
    literal("add").runs { name: String ->
        val lowercase = name.lowercase()
        if (lowercase in blacklist) return@runs modMessage("$name is already in the list.")
        modMessage("Added $name to list.")
        blacklist.add(lowercase)
        Config.save()
    }

    literal("remove").runs { name: String ->
        val lowercase = name.lowercase()
        if (lowercase !in blacklist) return@runs modMessage("$name isn't in the list.")
        modMessage("Removed $name from list.")
        blacklist.remove(lowercase)
        Config.save()
    }

    literal("clear").runs {
        modMessage("List cleared.")
        blacklist.clear()
        Config.save()
    }

    literal("list").runs {
        if (blacklist.isEmpty()) return@runs modMessage("List is empty.")
        modMessage("List:\n${blacklist.joinToString("\n")}")
    }
}
