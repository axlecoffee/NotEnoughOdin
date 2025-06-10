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
import com.github.stivais.commodore.utils.SyntaxException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.scope
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.fetchURLData
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage

val soopyCommand = Commodore("soopycmd", "spcmd", "spc") {
    val commands = listOf(
        "auctions", "bestiary", "bank", "classaverage", "currdungeon", "dojo", "dungeon", "essence", "faction",
        "guildof", "kuudra", "nucleus", "nw", "overflowskillaverage", "overflowskills", "pet", "rtca", "sblvl",
        "secrets", "skillaverage", "skills"
    )
    literal("help").runs {
        modMessage("Available commands for /spcmd:\n ${commands.joinToString()}")
    }

    executable {
        param("command") {
            parser { string: String ->
                if (!commands.contains(string)) throw SyntaxException("Invalid argument.")
                string
            }
            suggests { commands }
        }

        runs { command: String, user: String? ->
            val player = user ?: Minecraft.getMinecraft().thePlayer.name
            modMessage("Running command...")
            scope.launch {
                try {
                    modMessage(withTimeout(5000) {
                        fetchURLData("https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$player") }
                    )
                } catch (_: TimeoutCancellationException) {
                    modMessage("Request timed out")
                } catch (e: Exception) {
                    modMessage("Failed to fetch data: ${e.message}")
                }
            }
        }
    }
}
