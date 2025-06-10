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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.p3

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PostEntityMetadata
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.TerminalSolver
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.noControlCodes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object TerminalAura : Module(
    name = "Terminal Aura",
    desc = "Automatically interacts with inactive terminals.",
    tag = TagType.RISKY
) {
    private val onGround by BooleanSetting("On Ground", true, desc = "Only click when on the ground.")
    private val distance by NumberSetting("Distance", 3.5f, 1.0, 4.5, 0.1, desc = "The distance to click the terminal.")

    private val clickClock = Clock(1000)
    private val interactClock = Clock(500)
    private val terminalEntityList = mutableListOf<EntityArmorStand>()

    init {
        onWorldLoad {
            terminalEntityList.clear()
        }

        onMessage(Regex("This Terminal doesn't seem to be responsive at the moment.")) {
            interactClock.update()
        }

        onPacket<S2DPacketOpenWindow> {
            if (it.windowTitle.formattedText.noControlCodes == "Click the button on time!") interactClock.update()
        }
    }

    @SubscribeEvent
    fun onPacketSent(event: PacketEvent.Send) {
        (event.packet as? C02PacketUseEntity)?.getEntityFromWorld(Minecraft.getMinecraft().theWorld)?.let {
            if (it.name.noControlCodes != "Inactive Terminal") return
            if (!interactClock.hasTimePassed() || TerminalSolver.currentTerm != null) event.isCanceled = true else interactClock.update()
        }
    }

    @SubscribeEvent
    fun onEntityLoaded(event: PostEntityMetadata) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val entity = Minecraft.getMinecraft().theWorld?.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return
        if (entity.name.noControlCodes == "Inactive Terminal") terminalEntityList.add(entity)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || Minecraft.getMinecraft().thePlayer?.openContainer !is ContainerPlayer || (!Minecraft.getMinecraft().thePlayer.onGround && onGround) || !clickClock.hasTimePassed()) return
        val terminal = terminalEntityList.firstOrNull {
            Minecraft.getMinecraft().thePlayer.positionVector.addVec(y = Minecraft.getMinecraft().thePlayer.getEyeHeight()).distanceTo(Vec3(it.posX, it.posY + it.height / 2, it.posZ)) < distance
        } ?: return
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(terminal, C02PacketUseEntity.Action.INTERACT))
        clickClock.update()
    }
}
