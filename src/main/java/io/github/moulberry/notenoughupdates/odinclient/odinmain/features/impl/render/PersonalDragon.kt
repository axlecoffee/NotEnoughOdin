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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderX
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderY
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderZ
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

object PersonalDragon : Module(
    name = "Personal Dragon",
    desc = "Spawns your own personal dragon."
) {
    private val onlyF5 by BooleanSetting("Only F5", true, desc = "Only render the dragon when in F5 mode.")
    private val scale by NumberSetting("Scale", 0.5f, 0f, 1f, 0.01f, desc = "The scale of the dragon.")
    private val horizontal by NumberSetting("Horizontal", -1f, -10f, 10f, 0.1f, desc = "The horizontal offset of the dragon.")
    private val vertical by NumberSetting("Vertical", 0f, -10f, 10f, 0.1f, desc = "The vertical offset of the dragon.")
    private val degrees by NumberSetting("Degrees", 0f, -180f, 180f, 1f, desc = "The degrees of the dragon.")
    private val animationSpeed by NumberSetting("Animation Speed", 0.5f, 0.0f, 1f, 0.01f, desc = "The speed of the dragon's animation.")
    private val color by ColorSetting("Color", Colors.WHITE, desc = "The color of the dragon.")

    var dragon: EntityDragon? = null

    override fun onDisable() {
        dragon?.let {
            Minecraft.getMinecraft().theWorld?.removeEntityFromWorld(it.entityId)
            dragon = null
        }
        super.onDisable()
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        dragon?.let {
            Minecraft.getMinecraft().theWorld?.removeEntityFromWorld(it.entityId)
            dragon = null
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (dragon == null && Minecraft.getMinecraft().theWorld != null) {
            dragon = EntityDragon(Minecraft.getMinecraft().theWorld)
            dragon?.let { Minecraft.getMinecraft().theWorld?.addEntityToWorld(it.entityId, it) }
            return
        }
        Minecraft.getMinecraft().thePlayer?.let { player ->
            var yaw = player.rotationYaw
            if (yaw < 0) yaw += 180 else if (yaw > 0) yaw -= 180
            dragon?.apply { setLocationAndAngles(player.renderX, player.renderY + 8, player.renderZ, yaw, player.rotationPitch) }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        dragon?.apply {
            animTime -= (1 - animationSpeed) / 5
            isSilent = true
        }
    }

    @SubscribeEvent
    fun onRenderEntityPre(event: RenderLivingEvent.Pre<EntityDragon>) {
        dragon?.let {
            if (event.entity.entityId != it.entityId) return
            if (onlyF5 && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                event.isCanceled = true
                return
            }
            val yawRadians = Math.toRadians(Minecraft.getMinecraft().thePlayer.rotationYaw.toDouble() + degrees)
            GlStateManager.pushMatrix()
            GlStateManager.translate(horizontal * cos(yawRadians), vertical.toDouble(), horizontal * sin(yawRadians))
            GlStateManager.scale(scale / 4, scale / 4, scale / 4)
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f)
        }
    }

    @SubscribeEvent
    fun onRenderEntityPost(event: RenderLivingEvent.Post<EntityDragon>) {
        dragon?.let {
            if (event.entity.entityId != it.entityId) return
            GlStateManager.popMatrix()
        }
    }
}
