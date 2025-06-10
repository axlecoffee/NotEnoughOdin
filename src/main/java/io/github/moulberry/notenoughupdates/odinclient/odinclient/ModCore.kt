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

package io.github.moulberry.notenoughupdates.odinclient.odinclient

import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon.AutoGFS
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon.*
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.FreezeGame
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.FuckDiorite
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.RelicAura
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.floor7.p3.*
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.skyblock.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain
import io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.CommandRegistry
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.ModuleManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


class ModCore {


    fun init(event: FMLInitializationEvent) {


        OdinMain.init()
        MinecraftForge.EVENT_BUS.register(this)
    }


    fun postInit(event: FMLPostInitializationEvent) {
        ModuleManager.addModules(
            AutoGFS, /*AutoIceFill,*/ AutoSell, CancelInteract, CloseChest, SecretHitboxes,
            HoverTerms, LightsDevice, SimonSays, ArrowsDevice, FuckDiorite, RelicAura,
            Trajectories, Ghosts, NoDebuff, ChocolateFactory, AutoExperiments, AutoHarp,
            FarmingHitboxes, NoBlock, AutoClicker, Triggerbot, GhostBlocks, FreezeGame, EtherWarpHelper, ChestEsp,
            EscrowFix, TerminalAura, AutoTerms, Camera, DungeonAbilities, QueueTerms, HidePlayers
        )
        OdinMain.postInit()
    }


    fun loadComplete(event: FMLLoadCompleteEvent) {
        OdinMain.loadComplete()
    }


    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        OdinMain.onTick()
    }

    companion object {
        const val MOD_ID = "odclient"
        const val NAME = "OdinClient"
        const val VERSION = OdinMain.VERSION
    }
}
