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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.KeybindSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

object FarmKeys: Module(
    name = "Farm Keys",
    desc = "Temporarily changes your minecraft keybind configuration for farming in Skyblock."
) {
    private val blockBreakKey by KeybindSetting("Block breaking", Keyboard.KEY_NONE, "Changes the keybind for breaking blocks.")
    private val jumpKey by KeybindSetting("Jump", Keyboard.KEY_NONE, "Changes the keybind for jumping.")
    private val previousSensitivity by NumberSetting("Previous Sensitivity", 100f, 0f, 200f, desc = "The sensitivity before enabling the module.")

    override fun onEnable() {
        updateKeyBindings(blockBreakKey.key, jumpKey.key, -1 / 3f)
        super.onEnable()
    }

    override fun onDisable() {
        updateKeyBindings(-100, 57, previousSensitivity / 200)
        super.onDisable()
    }

    private fun updateKeyBindings(breakKeyCode: Int, jumpKeyCode: Int, sensitivity: Float) {
        setKeyBindingState(Minecraft.getMinecraft().gameSettings.keyBindAttack, breakKeyCode)
        setKeyBindingState(Minecraft.getMinecraft().gameSettings.keyBindJump, jumpKeyCode)
        Minecraft.getMinecraft().gameSettings.mouseSensitivity = sensitivity
        Minecraft.getMinecraft().gameSettings.saveOptions()
        Minecraft.getMinecraft().gameSettings.loadOptions()
    }

    private fun setKeyBindingState(keyBinding: KeyBinding, keyCode: Int) {
        KeyBinding.setKeyBindState(keyBinding.keyCode, false)
        keyBinding.keyCode = keyCode
    }
}
