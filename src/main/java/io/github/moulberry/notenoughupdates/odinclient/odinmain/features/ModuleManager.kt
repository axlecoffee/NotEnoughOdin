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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.ChatPacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.InputEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PacketEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.TerminalSimulator
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.TickTimers
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ClickGUIModule.hudChat
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.logError
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.profile
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.getTextWidth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.hud.EditHUDGui
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.hud.HudElement
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Class that contains all Modules and huds
 * @author Aton, Bonsai
 */
object ModuleManager {
    data class PacketFunction<T : Packet<*>>(val type: Class<T>, val shouldRun: () -> Boolean, val function: (T) -> Unit)
    data class MessageFunction(val filter: Regex, val shouldRun: () -> Boolean, val function: (MatchResult) -> Unit)
    data class TickTask(var ticksLeft: Int, val server: Boolean, val function: () -> Unit)

    val packetFunctions = arrayListOf<PacketFunction<Packet<*>>>()
    val messageFunctions = arrayListOf<MessageFunction>()
    val worldLoadFunctions = arrayListOf<() -> Unit>()
    val tickTasks = CopyOnWriteArrayList<TickTask>()
    val huds = arrayListOf<HudElement>()

    val modules: ArrayList<Module> = arrayListOf(
        // dungeon
        DungeonRequeue, BlessingDisplay, PositionalMessages, ExtraStats, KeyHighlight, Mimic, TeammatesHighlight,
        TerracottaTimer, BloodCamp, SecretClicked, DungeonWaypoints, LeapMenu, PuzzleSolvers, /*MageBeam,*/
        WarpCooldown, MapInfo, SwapSound, LividSolver,

        // floor 7
        TerminalSolver, TerminalTimes, MelodyMessage, TickTimers, InactiveWaypoints, WitherDragons,
        TerminalSimulator, TerminalSounds, ArrowAlign,

        // render
        BPSDisplay, ClickGUIModule, CustomHighlight, CPSDisplay, DragonHitboxes, GyroWand, NameChanger,
        PersonalDragon, RenderOptimizer, ServerHud, Waypoints, CanClip, Animations, SpaceHelmet,
        BlockOverlay, VisualWords, DVD, Sidebar, HideArmor,

        //skyblock
        NoCursorReset, AutoSprint, BlazeAttunement, ChatCommands, DeployableTimer, DianaHelper, ArrowHit,
        RagnarockAxe, MobSpawn, Splits, WardrobeKeybinds, InvincibilityTimer, ItemsHighlight, PlayerDisplay,
        FarmKeys, PetKeybinds, CommandKeybinds, SpringBoots, AbilityTimers, SlotBinds,

        // kuudra
        BuildHelper, FreshTimer, KuudraDisplay, NoPre, PearlWaypoints, RemovePerks, SupplyHelper, TeamHighlight,
        VanqNotifier, KuudraReminders, KuudraRequeue,
    )

    init {
        for (module in modules) {
            module.keybinding?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module"))
            }
        }
    }

    fun addModules(vararg module: Module) {
        for (i in module) {
            modules.add(i)
            i.keybinding?.let { i.register(KeybindSetting("Keybind", it, "Toggles the module")) }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickTaskTick(false)
    }

    private fun tickTaskTick(server: Boolean = false) {
        val toRemove = tickTasks.filter { tickTask ->
            if (tickTask.server != server) return@filter false
            if (tickTask.ticksLeft <= 0) {
                runCatching { tickTask.function() }.onFailure { logError(it, this) }
                true
            } else {
                tickTask.ticksLeft--
                false
            }
        }
        tickTasks.removeAll(toRemove.toSet())
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onReceivePacket(event: PacketEvent.Receive) {
        if (event.packet is S32PacketConfirmTransaction) tickTaskTick(true)
        packetFunctions.forEach {
            if (it.shouldRun() && it.type.isInstance(event.packet)) it.function(event.packet)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onSendPacket(event: PacketEvent.Send) {
        packetFunctions.forEach {
            if (it.shouldRun() && it.type.isInstance(event.packet)) it.function(event.packet)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatPacket(event: ChatPacketEvent) {
        messageFunctions.forEach {
            if (it.shouldRun()) it.function(it.filter.find(event.message) ?: return@forEach)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        worldLoadFunctions.forEach { it.invoke() }
    }

    @SubscribeEvent
    fun activateModuleKeyBinds(event: InputEvent.Keyboard) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key == event.keycode) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: InputEvent.Mouse) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key + 100 == event.keycode) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if ((Minecraft.getMinecraft().currentScreen != null && !hudChat) || event.type != RenderGameOverlayEvent.ElementType.ALL || Minecraft.getMinecraft().currentScreen == EditHUDGui) return

        profile("Odin Hud") {
            for (i in 0 until huds.size) {
                huds[i].draw(false)
            }
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        val featureList = StringBuilder()

        for ((category, modulesInCategory) in modules.groupBy { it.category }.entries) {
            featureList.appendLine("Category: ${category.displayName}")
            for (module in modulesInCategory.sortedByDescending { getTextWidth(it.name, 18f) }) {
                featureList.appendLine("- ${module.name}: ${module.desc}")
            }
            featureList.appendLine()
        }
        return featureList.toString()
    }
}
