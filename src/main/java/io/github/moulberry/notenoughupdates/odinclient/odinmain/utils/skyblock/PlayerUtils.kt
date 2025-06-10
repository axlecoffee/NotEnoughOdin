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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termsim.TermSimGUI
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.postAndCatch
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PlayerUtils {
    var shouldBypassVolume = false

    /**
     * Plays a sound at a specified volume and pitch, bypassing the default volume setting.
     *
     * @param sound The identifier of the sound to be played.
     * @param volume The volume at which the sound should be played.
     * @param pitch The pitch at which the sound should be played.
     *
     * @author Aton
     */
    fun playLoudSound(sound: String?, volume: Float, pitch: Float, pos: Vec3? = null) {
        Minecraft.getMinecraft().addScheduledTask {
            shouldBypassVolume = true
            Minecraft.getMinecraft().theWorld?.playSound(pos?.xCoord ?: posX, pos?.yCoord ?: posY, pos?.zCoord  ?: posZ, sound, volume, pitch, false)
            shouldBypassVolume = false
        }
    }

    /**
     * Displays an alert on screen and plays a sound
     *
     * @param title String to be displayed.
     * @param playSound Toggle for sound.
     *
     * @author Odtheking, Bonsai
     */
    fun alert(title: String, time: Int = 20, color: Color = Colors.WHITE, playSound: Boolean = true, displayText: Boolean = true) {
        if (playSound) playLoudSound("note.pling", 100f, 1f)
        if (displayText) Renderer.displayTitle(title , time, color = color)
    }

    inline val posX get() = Minecraft.getMinecraft().thePlayer?.posX ?: 0.0
    inline val posY get() = Minecraft.getMinecraft().thePlayer?.posY ?: 0.0
    inline val posZ get() = Minecraft.getMinecraft().thePlayer?.posZ ?: 0.0

    fun getPositionString(): String {
        val blockPos = BlockPos(posX, posY, posZ)
        return "x: ${blockPos.x}, y: ${blockPos.y}, z: ${blockPos.z}"
    }

    private var lastClickSent = 0L

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (event.packet !is C0EPacketClickWindow) return
        //modMessage(System.currentTimeMillis() - lastClickSent)
        lastClickSent = System.currentTimeMillis()
    }

    fun windowClick(slotId: Int, button: Int, mode: Int) {
        if (lastClickSent + 45 > System.currentTimeMillis()) return devMessage("§cIgnoring click on slot §9$slotId.")
        Minecraft.getMinecraft().thePlayer?.openContainer?.let {
            if (slotId !in 0 until it.inventorySlots.size) return
            if (Minecraft.getMinecraft().currentScreen is TermSimGUI) {
                PacketEvent.Send(C0EPacketClickWindow(-2, slotId, button, mode, it.inventorySlots[slotId].stack, 0)).postAndCatch()
                return
            }
            Minecraft.getMinecraft().playerController?.windowClick(it.windowId, slotId, button, mode, Minecraft.getMinecraft().thePlayer)
            //Minecraft.getMinecraft().netHandler?.networkManager?.sendPacket(C0EPacketClickWindow(it.windowId, slotId, button, mode, it.inventory[slotId], it.getNextTransactionID(Minecraft.getMinecraft().thePlayer?.inventory)))
        }
    }

    fun windowClick(slotId: Int, clickType: ClickType) {
        when (clickType) {
            is ClickType.Left -> windowClick(slotId, 0, 0)
            is ClickType.Right -> windowClick(slotId, 1, 0)
            is ClickType.Middle -> windowClick(slotId, 2, 3)
            is ClickType.Shift -> windowClick(slotId, 0, 1)
        }
    }
}

sealed class ClickType {
    object Left   : ClickType()
    object Right  : ClickType()
    object Middle : ClickType()
    object Shift  : ClickType()
}
