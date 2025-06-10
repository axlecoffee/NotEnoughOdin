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

import io.github.moulberry.notenoughupdates.odinclient.odinclient.utils.skyblock.PlayerUtils.rightClick
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.block.BlockLever
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LightsDevice : Module(
    name = "Lights Device",
    desc = "Features to help with the lights device."
) {
    private val triggerBot by BooleanSetting("Triggerbot", false, desc = "Toggles correct levers automatically when you look at them.")
    private val delay by NumberSetting("Delay", 200L, 70, 500, unit = "ms", desc = "The delay between each click.").withDependency { triggerBot }
    val bigLevers by BooleanSetting("Big Levers", false, desc = "Makes the levers you want to toggle a 1x1x1 hitbox so they are easier to hit.")
    private val triggerBotClock = Clock(delay)

    val levers = setOf(
        BlockPos(58, 136, 142),
        BlockPos(58, 133, 142),
        BlockPos(60, 135, 142),
        BlockPos(60, 134, 142),
        BlockPos(62, 136, 142),
        BlockPos(62, 133, 142),
    )

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!triggerBotClock.hasTimePassed(delay) || DungeonUtils.getF7Phase() != M7Phases.P3 || !triggerBot) return
        val pos = Minecraft.getMinecraft().objectMouseOver?.blockPos ?: return
        if (pos !in levers || Minecraft.getMinecraft().theWorld.getBlockState(pos).getValue(BlockLever.POWERED)) return
        rightClick()
        triggerBotClock.update()
    }
}
