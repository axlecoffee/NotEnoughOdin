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

package io.github.moulberry.notenoughupdates.odinclient.odinmain

import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import kotlinx.coroutines.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.CommandRegistry
import io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.impl.OdinCommand
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.DungeonWaypointConfig
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.PBConfig
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.EventDispatcher
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.ModuleManager
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ClickGUIModule
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.RandomPlayers
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.WaypointManager
import io.github.moulberry.notenoughupdates.odinclient.odinmain.font.OdinFont
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ServerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.SplitsManager
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils2D
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.sendDataToServer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.ScanUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.ClickGUI
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

import kotlin.coroutines.EmptyCoroutineContext


object OdinMain {


    const val VERSION = "@VER@"
    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)
    val logger: Logger = LogManager.getLogger("Odin")

    var display: GuiScreen? = null
    inline val isLegitVersion: Boolean
        get() = Loader.instance().activeModList.none { it.modId == "odclient" }

    fun init() {
        println("OdinMain initialized")
        println("This is odin printing session" + Minecraft.getMinecraft().session.sessionID)
        PBConfig.loadConfig()
        listOf(
            LocationUtils, ServerUtils, PlayerUtils,
            RenderUtils, Renderer, DungeonUtils, KuudraUtils,
            EventDispatcher, Executor, ModuleManager,
            WaypointManager, RandomPlayers, SkyblockPlayer,
            ScanUtils, HighlightRenderer, //OdinUpdater,
            SplitsManager, RenderUtils2D, ArrowTracker,
            this
        ).forEach { MinecraftForge.EVENT_BUS.register(it) }
        OdinFont.init()

        scope.launch(Dispatchers.IO) { RandomPlayers.preloadCapes() }
    }

    fun postInit() {
        File(Minecraft.getMinecraft().mcDataDir, "config/odin").takeIf { !it.exists() }?.mkdirs()
    }

    fun loadComplete() {
        runBlocking(Dispatchers.IO) {
            Config.load()
            ClickGUIModule.lastSeenVersion = VERSION
        }
        ClickGUI.init()

        val name = Minecraft.getMinecraft().session?.username?.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return
        scope.launch(Dispatchers.IO) {
            DungeonWaypointConfig.loadConfig()
            ClickGUIModule.latestVersionNumber = ClickGUIModule.checkNewerVersion(VERSION)
            sendDataToServer(body = """{"username": "$name", "version": "${if (isLegitVersion) "legit" else "cheater"} $VERSION"}""")
        }
    }

    fun onTick() {
        if (display == null) return
        Minecraft.getMinecraft().displayGuiScreen(display)
        display = null
    }
}
