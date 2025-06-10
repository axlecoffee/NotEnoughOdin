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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.DropdownSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.getPositionEyes
import net.minecraft.client.Minecraft
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import kotlin.math.cos
import kotlin.math.sin

object Camera : Module(
    name = "Camera",
    desc = "Various camera improvements and settings."
) {
    private val frontCamera by BooleanSetting("No Front Camera", false, desc = "Disables front camera.")
    private val cameraClip by BooleanSetting("Camera Clip", false, desc = "Allows the camera to clip through blocks.")
    private val cameraDist by NumberSetting("Distance", 4f, 3.0, 12.0, 0.1, desc = "The distance of the camera from the player.")
    private val customFOV by BooleanSetting("Custom FOV", desc = "Allows you to change the FOV.")
    private val fov by NumberSetting("FOV", Minecraft.getMinecraft().gameSettings.fovSetting, 1f, 180f, 1f, desc = "The field of view of the camera.").withDependency { customFOV }
    private val freelookDropdown by DropdownSetting("Freelook")
    private val toggleMode by BooleanSetting("Toggle Mode", false, desc = "If enabled, toggle freelook. Otherwise, hold to use.").withDependency { freelookDropdown }
    private val freelookKeybind by KeybindSetting("Freelook Key", Keyboard.KEY_NONE, description = "Keybind to toggle/ hold for freelook.")
        .withDependency { freelookDropdown }
        .onPress {
            if (!freelookToggled && enabled) enable()
            else if ((toggleMode || !enabled) && freelookToggled) disable()
    }
    @JvmStatic
    var freelookToggled = false
    private var cameraYaw = 0f
    private var cameraPitch = 0f
    private var lastPerspective = -1

    private var previousFov = Minecraft.getMinecraft().gameSettings.fovSetting

    override fun onEnable() {
        previousFov = Minecraft.getMinecraft().gameSettings.fovSetting
        super.onEnable()
    }

    override fun onDisable() {
        Minecraft.getMinecraft().gameSettings.fovSetting = previousFov
        super.onDisable()
    }

    @JvmStatic
    fun getCameraDistance(): Float {
        return if (enabled) cameraDist else 4f
    }

    @JvmStatic
    fun getCameraClipEnabled(): Boolean {
        return if (enabled) cameraClip else false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (customFOV && Minecraft.getMinecraft().gameSettings.fovSetting != fov)
            Minecraft.getMinecraft().gameSettings.fovSetting = fov

        if (frontCamera && Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
            Minecraft.getMinecraft().gameSettings.thirdPersonView = 0

        if (!freelookKeybind.isDown() && freelookToggled && !toggleMode) disable()
    }

    private fun enable() {
        cameraYaw = Minecraft.getMinecraft().thePlayer.rotationYaw + 180
        cameraPitch = Minecraft.getMinecraft().thePlayer.rotationPitch
        freelookToggled = true
        lastPerspective = Minecraft.getMinecraft().gameSettings.thirdPersonView
        Minecraft.getMinecraft().gameSettings.thirdPersonView = 1
    }

    private fun disable() {
        freelookToggled = false
        Minecraft.getMinecraft().gameSettings.thirdPersonView = if (lastPerspective != -1) lastPerspective else 0
        lastPerspective = -1
    }

    @SubscribeEvent
    fun cameraSetup(e: EntityViewRenderEvent.CameraSetup) {
        if (!freelookToggled) return
        e.yaw = cameraYaw
        e.pitch = cameraPitch
    }

    @JvmStatic
    fun updateCameraAndRender(f2: Float, f3: Float) {
        if (!freelookToggled) return
        cameraYaw += f2 / 7
        cameraPitch = MathHelper.clamp_float((cameraPitch + f3 / 7), -90f, 90f)
    }

    @JvmStatic
    fun calculateCameraDistance(): Float {
        val eyes = getPositionEyes()
        var dist = getCameraDistance()
        var f2 = cameraPitch

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) f2 += 180.0f

        val d4 = (sin(cameraYaw / 180.0f * Math.PI.toFloat()) * cos(f2 / 180.0f * Math.PI.toFloat())).toDouble() * dist
        val d5 = (-cos(cameraYaw / 180.0f * Math.PI.toFloat()) * cos(f2 / 180.0f * Math.PI.toFloat())).toDouble() * dist
        val d6 = (-sin(f2 / 180.0f * Math.PI.toFloat())).toDouble() * dist

        if (!cameraClip) repeat(8) {
            var f3 = ((it and 1) * 2 - 1).toFloat()
            var f4 = ((it shr 1 and 1) * 2 - 1).toFloat()
            var f5 = ((it shr 2 and 1) * 2 - 1).toFloat()
            f3 *= .1f
            f4 *= .1f
            f5 *= .1f
            val movingObjectPosition = Minecraft.getMinecraft().theWorld?.rayTraceBlocks(
                Vec3(eyes.xCoord + f3.toDouble(), eyes.yCoord + f4.toDouble(), eyes.zCoord + f5.toDouble()),
                Vec3(eyes.xCoord - d4 + f3.toDouble() + f5.toDouble(), eyes.yCoord - d6 + f4.toDouble(), eyes.zCoord - d5 + f5.toDouble())
            )

            if (movingObjectPosition != null) {
                val d7 = movingObjectPosition.hitVec.distanceTo(Vec3(eyes.xCoord, eyes.yCoord, eyes.zCoord))
                if (d7 < dist) dist = d7.toFloat()
            }
        }
        return dist
    }
}
