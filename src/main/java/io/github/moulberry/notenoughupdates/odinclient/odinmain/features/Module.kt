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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render.ClickGUIModule
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.AlwaysActive
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.HudSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.Keybinding
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executable
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor.Companion.register
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor.LimitedExecutor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import net.minecraft.network.Packet
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton, Bonsai
 */
abstract class Module(
    val name: String,
    key: Int? = Keyboard.KEY_NONE,
    @Transient var desc: String,
    @Transient val tag: TagType = TagType.NONE,
    toggled: Boolean = false,
) {

    /**
     * Category for this module.
     *
     * It is defined by the package of the module. (For example: me.odin.features.impl.render == [Category.RENDER]).
     * If it is in an invalid package, it will use [Category.RENDER] as a default
     */
    @Transient
    val category: Category = getCategory(this::class.java) ?: Category.RENDER

    var enabled: Boolean = toggled
        private set

    /**
     * Settings for the module
     */
    val settings: ArrayList<Setting<*>> = ArrayList()

    /**
     * Main keybinding of the module
     */
    val keybinding: Keybinding? = key?.let { Keybinding(it).apply { onPress = ::onKeybind } }

    /**
     * Indicates if the module has the annotation [AlwaysActive],
     * which keeps the module registered to the eventbus, even if disabled
     */
    @Transient
    val alwaysActive = this::class.java.isAnnotationPresent(AlwaysActive::class.java)

    init {
        if (alwaysActive) {
            @Suppress("LeakingThis")
            MinecraftForge.EVENT_BUS.register(this)
        }
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) MinecraftForge.EVENT_BUS.register(this)
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) MinecraftForge.EVENT_BUS.unregister(this)
    }

    open fun onKeybind() {
        toggle()
        if (ClickGUIModule.enableNotification) {
            modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
        }
    }

    fun toggle() {
        enabled = !enabled
        if (enabled) onEnable()
        else onDisable()
    }

    fun <K : Setting<*>> register(setting: K): K {
        settings.add(setting)
        if (setting is HudSetting) {
            setting.value.init(this)
        }
        return setting
    }

    fun register(vararg setting: Setting<*>) {
        setting.forEach(::register)
    }

    operator fun <K : Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String?): Setting<*>? {
        for (setting in settings) {
            if (setting.name.equals(name, ignoreCase = true)) {
                return setting
            }
        }
        return null
    }

    /**
     * Helper function to make cleaner code, and better performance, since we don't need multiple registers for packet received events.
     *
     * @param T The type of the packet to listen for.
     * @param shouldRun Get whether the function should run (Will in most cases be used with the "enabled" value)
     * @param func The function to run when the packet is received.
     */
    inline fun <reified T : Packet<*>> onPacket(noinline shouldRun: () -> Boolean = { alwaysActive || enabled }, noinline func: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        ModuleManager.packetFunctions.add(
            ModuleManager.PacketFunction(T::class.java, shouldRun, func) as ModuleManager.PacketFunction<Packet<*>>
        )
    }

    /**
     * Runs the given function when a Chat Packet is sent with a message that matches the given regex filter.
     *
     * @param filter The regex the message should match
     * @param shouldRun Boolean getter to decide if the function should run at any given time, could check if the option is enabled for instance.
     * @param func The function to run if the message matches the given regex and shouldRun returns true.
     *
     * @author Bonsai
     */
    fun onMessage(filter: Regex, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (MatchResult) -> Unit) {
        ModuleManager.messageFunctions.add(ModuleManager.MessageFunction(filter, shouldRun) { matchResult -> func(matchResult) })
    }

    fun onWorldLoad(func: () -> Unit) {
        ModuleManager.worldLoadFunctions.add(func)
    }

    fun execute(delay: Long, repeats: Int, profileName: String = "${this.name} Executor", shouldRun: () -> Boolean = { this.enabled || this.alwaysActive }, func: Executable) {
        LimitedExecutor(delay, repeats, profileName, shouldRun, func).register()
    }

    fun execute(delay: () -> Long, profileName: String = "${this.name} Executor", shouldRun: () -> Boolean = { this.enabled || this.alwaysActive }, func: Executable) {
        Executor(delay, profileName, shouldRun, func).register()
    }

    fun execute(delay: Long, profileName: String = "${this.name} Executor", shouldRun: () -> Boolean = { this.enabled || this.alwaysActive }, func: Executable) {
        Executor(delay, profileName, shouldRun, func).register()
    }

    enum class TagType {
        NONE, RISKY, FPSTAX
    }

    private companion object {
        @OptIn(ExperimentalStdlibApi::class)
        private fun getCategory(clazz: Class<out Module>): Category? =
            Category.entries.find { clazz.`package`.name.contains(it.name, true) }
    }
}
