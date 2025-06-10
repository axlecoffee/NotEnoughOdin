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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.commands.impl

import com.github.stivais.commodore.Commodore
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.smoothRotateTo
import net.minecraft.client.Minecraft

val OdinClientCommand = Commodore("odinclient") {
    literal("set") {
        runs { yaw: Float, pitch: Float ->
            Minecraft.getMinecraft().thePlayer.rotationYaw = yaw.coerceIn(minimumValue = -180f, maximumValue = 180f)
            Minecraft.getMinecraft().thePlayer.rotationPitch = pitch.coerceIn(minimumValue = -90f, maximumValue = 90f)
        }
    }

    literal("rotate") {
        runs { yaw: Float, pitch: Float, time: Long? ->
            smoothRotateTo(yaw, pitch, time ?: 100L) { modMessage("§aFinished rotating!") }
        }
    }
}
