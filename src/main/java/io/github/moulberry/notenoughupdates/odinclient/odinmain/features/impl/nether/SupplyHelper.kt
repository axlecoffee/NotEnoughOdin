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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether.NoPre.missing
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.formatTime
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils.SupplyPickUpSpot
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object SupplyHelper : Module(
    name = "Supply Helper",
    desc = "Provides visual aid for supply drops in Kuudra."
) {
    private val suppliesWaypoints by BooleanSetting("Supplies Waypoints", true, desc = "Renders the supply waypoints.")
    private val supplyWaypointColor by ColorSetting("Supply Waypoint Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the supply waypoints.").withDependency { suppliesWaypoints }
    private val supplyDropWaypoints by BooleanSetting("Supply Drop Waypoints", true, desc = "Renders the supply drop waypoints.")
    private val sendSupplyTime by BooleanSetting("Send Supply Time", true, desc = "Sends a message when a supply is collected.")

    private var startRun = 0L
    private val supplyPickUpRegex = Regex("(?:\\[[^]]*])? ?(\\w{1,16}) recovered one of Elle's supplies! \\((\\d)/(\\d)\\)")
    // https://regex101.com/r/xsDImP/1
    init {
        onMessage(Regex("\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!")) {
            startRun = System.currentTimeMillis()
        }

        onMessage(supplyPickUpRegex) {
            if (!sendSupplyTime || !KuudraUtils.inKuudra || KuudraUtils.phase != 1) return@onMessage
            val (name, current, total) = it.destructured
            modMessage("§6$name §a§lrecovered a supply in ${formatTime((System.currentTimeMillis() - startRun))}! §r§8($current/$total)", "")
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (sendSupplyTime && KuudraUtils.inKuudra && KuudraUtils.phase == 1 && supplyPickUpRegex.matches(event.message.unformattedText))
            event.isCanceled = true
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return
        if (supplyDropWaypoints) {
            locations.forEachIndexed { index, (position, name) ->
                if (!KuudraUtils.supplies[index]) return@forEachIndexed
                Renderer.drawCustomBeacon("", position, if (missing == name) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED, increase = false)
            }
        }

        if (suppliesWaypoints) {
            KuudraUtils.giantZombies.forEach {
                Renderer.drawCustomBeacon("Supply",
                    Vec3(it.posX + (3.7 * cos((it.rotationYaw + 130) * (Math.PI / 180))), 73.0, it.posZ + (3.7 * sin((it.rotationYaw + 130) * (Math.PI / 180)))), supplyWaypointColor, increase = false)
            }
        }
    }
    private val locations = listOf(
        Pair(Vec3(-98.0, 78.0, -112.0), SupplyPickUpSpot.Shop),
        Pair(Vec3(-98.0, 78.0, -99.0), SupplyPickUpSpot.Equals),
        Pair(Vec3(-110.0, 78.0, -106.0), SupplyPickUpSpot.xCannon),
        Pair(Vec3(-106.0, 78.0, -112.0), SupplyPickUpSpot.X ),
        Pair(Vec3(-94.0, 78.0, -106.0), SupplyPickUpSpot.Triangle),
        Pair(Vec3(-106.0, 78.0, -99.0), SupplyPickUpSpot.Slash),
    )
}
