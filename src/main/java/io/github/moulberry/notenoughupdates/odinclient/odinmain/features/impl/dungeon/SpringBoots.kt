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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.getSafe
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.skyblockID
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toAABB
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object SpringBoots : Module(
    name = "Spring Boots",
    desc = "Shows the current jump height of your spring boots."
) {
    private val hud by HudSetting("Display", 10f, 10f, 1f, true) { example ->
        if (example) {
            RenderUtils.drawText("Jump: 6.5", 1f, 1f, 1f, Colors.WHITE, center = true)
            getTextWidth("Jump: 6.5", 12f) to 12f
        } else {
            val blockAmount = blocksList.getSafe(pitchCounts.sum()).takeIf { it != 0.0 } ?: return@HudSetting 0f to 0f
            RenderUtils.drawText("Jump: ${colorHud(blockAmount)}", 1f, 1f, 1f, Colors.WHITE, center = true)
            getTextWidth("Jump: ${colorHud(blockAmount)}", 12f) to 12f
        }
    }
    private val renderGoal by BooleanSetting("Render Goal", true, desc = "Render the goal block.")
    private val goalColor by ColorSetting("Goal Color", Colors.MINECRAFT_GREEN, desc = "Color of the goal block.")
    private val offset by NumberSetting("Offset", 0.0, -10.0, 10.0, 0.1, desc = "The offset of the goal block.")

    private val blocksList: List<Double> = listOf(
        0.0, 3.0, 6.5, 9.0, 11.5, 13.5, 16.0, 18.0, 19.0,
        20.5, 22.5, 25.0, 26.5, 28.0, 29.0, 30.0, 31.0, 33.0,
        34.0, 35.5, 37.0, 38.0, 39.5, 40.0, 41.0, 42.5, 43.5,
        44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0, 51.0, 52.0,
        53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0, 61.0
    )

    private val pitchCounts = IntArray(2) { 0 }
    private var blockPos: Vec3? = null

    init {
        onPacket<S29PacketSoundEffect> { packet ->
            if (!LocationUtils.isInSkyblock) return@onPacket
            when (packet.soundName) {
                "random.eat", "fireworks.launch" -> if (packet.pitch.equalsOneOf(0.0952381f, 1.6984127f)) pitchCounts.fill(0)
                "note.pling" -> if (Minecraft.getMinecraft().thePlayer?.isSneaking == true && Minecraft.getMinecraft().thePlayer?.getCurrentArmor(0)?.skyblockID == "SPRING_BOOTS") {
                    when (packet.pitch) {
                        0.6984127f -> pitchCounts[0] = (pitchCounts[0] + 1).takeIf { it <= 2 } ?: 0
                        0.82539684f, 0.8888889f, 0.93650794f, 1.0476191f, 1.1746032f, 1.3174603f, 1.7777778f -> pitchCounts[1]++
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || !LocationUtils.isInSkyblock) return
        if (Minecraft.getMinecraft().thePlayer?.getCurrentArmor(0)?.skyblockID != "SPRING_BOOTS" || Minecraft.getMinecraft().thePlayer?.isSneaking == false) pitchCounts.fill(0)
        blocksList.getSafe(pitchCounts.sum())?.let { blockPos = if (it != 0.0) Minecraft.getMinecraft().thePlayer?.positionVector?.addVec(x = -0.5 + offset, y = it, z = -0.5) else null }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!renderGoal || !LocationUtils.isInSkyblock) return
        blockPos?.let { Renderer.drawBox(it.toAABB(), goalColor, fillAlpha = 0f) }
    }

    private fun colorHud(blocks: Double): String {
        return when {
            blocks <= 13.5 -> "§c"
            blocks <= 22.5 -> "§e"
            blocks <= 33.0 -> "§6"
            blocks <= 43.5 -> "§a"
            else -> "§b"
        } + blocks
    }
}
