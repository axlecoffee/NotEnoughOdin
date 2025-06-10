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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.utils.skyblock

import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.runIn
import net.minecraft.client.settings.KeyBinding



object PlayerUtils {

    /**
     * Right-clicks the next tick
     */
    fun rightClick() {
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.keyCode) // Simple way of making completely sure the right-clicks are sent at the same time as vanilla ones.
    }

    /**
     * Left-clicks the next tick
     */
    fun leftClick() {
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindAttack.keyCode) // Simple way of making completely sure the left-clicks are sent at the same time as vanilla ones.
    }

    fun dropItem(dropAll: Boolean = false, delay: Int = 1) {
        runIn(delay.coerceAtLeast(1)) { Minecraft.getMinecraft().thePlayer.dropOneItem(dropAll) } // just so that this runs on tick properly
    }

    fun swapToIndex(index: Int) {
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[index].keyCode)
    }
}
