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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component1
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component2
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component3
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object RelicAura : Module(
    name = "Relic Aura",
    desc = "Automatically picks up relics in the Wither King boss.",
    tag = TagType.RISKY
){
    private val distance by NumberSetting("Distance", 3f, 1.0, 6.0, 0.1, desc = "The distance to the relic to pick it up.")

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5) return
        val armorStand = Minecraft.getMinecraft().theWorld?.loadedEntityList?.firstOrNull {
            it is EntityArmorStand && it.inventory?.get(4)?.displayName?.contains("Relic") == true && Minecraft.getMinecraft().thePlayer.getDistanceToEntity(it) < distance } ?: return
        interactWithEntity(armorStand)
    }

    private fun interactWithEntity(entity: Entity) {
        val (x, y, z) = Minecraft.getMinecraft().objectMouseOver?.hitVec ?: return
        Minecraft.getMinecraft().netHandler.addToSendQueue(C02PacketUseEntity(entity, Vec3(x - entity.posX, y - entity.posY, z - entity.posZ)))
    }
}
