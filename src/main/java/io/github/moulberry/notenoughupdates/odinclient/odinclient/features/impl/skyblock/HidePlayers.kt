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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HidePlayers : Module(
    name = "Hide Players",
    desc = "Hides players in your vicinity."
) {
    private val hideAll by BooleanSetting("Hide all", false, desc = "Hides all players, regardless of distance.")
    private val distance by NumberSetting("distance", 3.0f, 0.0, 32.0, .5, "The number of blocks away to hide players.").withDependency { !hideAll }
    private val clickThrough by BooleanSetting("Click Through", false, desc = "Allows clicking through players.")
    private val onlyDevs by BooleanSetting("only at Devs", false, desc = "Only hides players when standing at ss or fourth device.")

    @SubscribeEvent
    fun onRenderEntity(event: RenderPlayerEvent.Pre) {
        if (Minecraft.getMinecraft().isSingleplayer) return
        val atDevs = (Minecraft.getMinecraft().thePlayer.getDistance(108.63, 120.0, 94.0) <= 1.8 || Minecraft.getMinecraft().thePlayer.getDistance(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.getF7Phase() == M7Phases.P3
        if (event.entity.uniqueID.version() == 2 || clickThrough || event.entity == Minecraft.getMinecraft().thePlayer || (!atDevs && onlyDevs)) return
        if (event.entity.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) <= distance || hideAll) event.isCanceled = true
    }

    @SubscribeEvent
    fun onPosUpdate(event: LivingEvent.LivingUpdateEvent) {
        if (Minecraft.getMinecraft().isSingleplayer || event.entity !is EntityPlayer) return
        val atDevs = (Minecraft.getMinecraft().thePlayer.getDistance(108.63, 120.0, 94.0) <= 1.8 || Minecraft.getMinecraft().thePlayer.getDistance(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.getF7Phase() == M7Phases.P3
        if (event.entity.uniqueID.version() == 2 || !clickThrough || event.entity == Minecraft.getMinecraft().thePlayer || (!atDevs && onlyDevs)) return
        if (event.entity.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) <= distance || hideAll) {
            event.entity.posX = 9999999.0
            event.isCanceled = true
        }
    }
}
