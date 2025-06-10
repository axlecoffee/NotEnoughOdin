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


import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.moulberry.notenoughupdates.util.brigadier.DefaultSource
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.display
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ClickGUIModule
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ServerHud.colorizeFPS
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ServerHud.colorizePing
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ServerHud.colorizeTps
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ServerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.fillItemFromSack
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.ClickGUI
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.hud.EditHUDGui
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.writeToClipboard
import kotlin.math.round

/**
 * Registers the /od and /odin commands and all subcommands.
 */
object OdinCommand {

    fun register(dispatcher: CommandDispatcher<DefaultSource>) {
        val root = LiteralArgumentBuilder.literal<DefaultSource>("od")
        val odin = LiteralArgumentBuilder.literal<DefaultSource>("odin")

        root.executes { display = ClickGUI; 1 }

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("edithud")
            .executes { display = EditHUDGui; 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("ep")
            .executes { fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", true); 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("ij")
            .executes { fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", true); 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("sl")
            .executes { fillItemFromSack(16, "SPIRIT_LEAP", "spirit_leap", true); 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("sb")
            .executes { fillItemFromSack(64, "SUPERBOOM_TNT", "superboom_tnt", true); 1 }
        )

        val reset = LiteralArgumentBuilder.literal<DefaultSource>("reset")
        reset.then(LiteralArgumentBuilder.literal<DefaultSource>("clickgui")
            .executes { ClickGUIModule.resetPositions(); modMessage("Reset click gui positions."); 1 }
        )
        reset.then(LiteralArgumentBuilder.literal<DefaultSource>("hud")
            .executes { EditHUDGui.resetHUDs(); modMessage("Reset HUD positions."); 1 }
        )
        root.then(reset)

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("rq")
            .executes { sendCommand("instancerequeue"); modMessage("requeing dungeon run"); 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("help")
            .executes {
                modMessage(
                    """
                    List of commands:
                    §3- /od §7» §8Main command.
                    §3- /od edithud §7» §8Edit HUD.
                    §3- /od reset <clickgui|hud> §7» §8Resets positions accordingly. 
                    §3- /dwp §7» §8Dungeon waypoints command.
                    §3- /petkeys §7» §8Pet keys command.
                    §3- /posmsg §7» §8Position message command.
                    §3- /chatclist §7» §8Used to configure your blacklist/whitelist.
                    §3- /highlight §7» §8Used to configure Highlight list.
                    §3- /waypoint §7» §8Configure waypoints.
                    §3- /termsim <ping>? §7» §8Simulates terminals so you can practice them.
                    §3- /od rq §7» §8Requeues dungeon run.
                    §3- /od m? » §8Teleports you to a floor in master mode.
                    §3- /od f? » §8Teleports you to a floor in normal mode.
                    §3- /od t? » §8Teleports you to a kuudra run.
                    §3- /od dianareset §7» §8Resets all active diana waypoints.
                    §3- /od sendcoords §7» §8Sends coords in patcher's format.
                    §3- /od ping §7» §8Sends your ping in chat.
                    §3- /od tps §7» §8Sends the server's tps in chat.
                    §3- /od ep §7» §8Refills ender pearls up to 16.
                    §3- /od ij §7» §8Refills inflatable Jerry's up to 64.
                    §3- /od sl §7» §8Refills spirit leaps up to 16.
                    §3- /od sb §7» §8Refills super booms up to 64.
                    §3- /spcmd §7» §8Use /spcmd help for command list.
                    §3- /visualwords §7» §8Command to replace words in the game.
                    §3- /od leaporder §7» §8Sets custom leap order.
                    """.trimIndent()
                )
                1
            }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("dianareset")
            .executes {
                modMessage("§aResetting all active diana waypoints.")
                DianaBurrowEstimate.activeBurrows.clear()
                1
            }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("sendcoords")
            .then(RequiredArgumentBuilder.argument<DefaultSource, String>("message", StringArgumentType.greedyString())
                .executes { ctx ->
                    val msg = StringArgumentType.getString(ctx, "message")
                    sendChatMessage(PlayerUtils.getPositionString() + " $msg")
                    1
                }
            )
            .executes {
                sendChatMessage(PlayerUtils.getPositionString())
                1
            }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("ping")
            .executes { modMessage("${colorizePing(ServerUtils.averagePing.toInt())}ms"); 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("fps")
            .executes { modMessage(colorizeFPS(Minecraft.getMinecraft().debug.split(" ")[0].toIntOrNull() ?: 0)); 1 }
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("tps")
            .executes { modMessage("${colorizeTps(round(ServerUtils.averageTps))}ms"); 1 }
        )

        Floors.values().forEach { floor ->
            root.then(LiteralArgumentBuilder.literal<DefaultSource>(floor.name.lowercase())
                .executes { sendCommand("joininstance ${floor.instance()}"); 1 }
            )
        }
        KuudraTier.values().forEach { tier ->
            root.then(LiteralArgumentBuilder.literal<DefaultSource>(tier.name.lowercase())
                .executes { sendCommand("joininstance ${tier.instance()}"); 1 }
            )
        }

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("leaporder")
            .then(RequiredArgumentBuilder.argument<DefaultSource, String>("player1", StringArgumentType.word())
                .then(RequiredArgumentBuilder.argument<DefaultSource, String>("player2", StringArgumentType.word())
                    .then(RequiredArgumentBuilder.argument<DefaultSource, String>("player3", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.argument<DefaultSource, String>("player4", StringArgumentType.word())
                            .executes { ctx ->
                                val p1 = StringArgumentType.getString(ctx, "player1")
                                val p2 = StringArgumentType.getString(ctx, "player2")
                                val p3 = StringArgumentType.getString(ctx, "player3")
                                val p4 = StringArgumentType.getString(ctx, "player4")
                                val players = listOf(p1, p2, p3, p4).map { it.lowercase() }
                                DungeonUtils.customLeapOrder = players
                                modMessage("§aCustom leap order set to: §f$p1, $p2, $p3, $p4")
                                1
                            }
                        )
                    )
                )
            )
        )

        root.then(LiteralArgumentBuilder.literal<DefaultSource>("copy")
            .then(RequiredArgumentBuilder.argument<DefaultSource, String>("message", StringArgumentType.greedyString())
                .executes { ctx ->
                    val msg = StringArgumentType.getString(ctx, "message")
                    writeToClipboard(msg, "§aCopied to clipboard.")
                    1
                }
            )
        )

        dispatcher.register(root)
        dispatcher.register(odin.redirect(root.build()))
    }

    private enum class Floors {
        F1, F2, F3, F4, F5, F6, F7, M1, M2, M3, M4, M5, M6, M7;
        private val floors = listOf("one", "two", "three", "four", "five", "six", "seven");
        fun instance() = "${if (ordinal > 6) "master_" else ""}catacombs_floor_${floors[(ordinal % 7)]}";
    }

    private enum class KuudraTier(private val tier: String) {
        T1("normal"), T2("hot"), T3("burning"), T4("fiery"), T5("infernal");
        fun instance() = "kuudra_${tier}";
    }
}
