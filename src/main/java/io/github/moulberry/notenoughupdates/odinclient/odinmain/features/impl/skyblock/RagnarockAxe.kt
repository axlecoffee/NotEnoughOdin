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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.*
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S29PacketSoundEffect

object RagnarockAxe : Module(
    name = "Rag Axe",
    desc = "Provides alerts about ragnarock axe's state."
) {
    private val alert by BooleanSetting("Alert", true, desc = "Alerts you when you start casting rag axe.")
    private val alertCancelled by BooleanSetting("Alert Cancelled", true, desc = "Alerts you when your rag axe is cancelled.")
    private val strengthGainedMessage by BooleanSetting("Strength Gained", true, desc = "Sends a mod message which will notify of strength gained from rag axe after casting")
    private val announceStrengthGained by BooleanSetting("Send to party", false, desc = "Sends party message of strength gained after casting").withDependency { strengthGainedMessage }

    init {
        onMessage(Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")) {
            if (alertCancelled) PlayerUtils.alert("§cRag Axe Cancelled")
        }

        onPacket<S29PacketSoundEffect> {
            if (it.soundName != "mob.wolf.howl" || it.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@onPacket
            if (alert) PlayerUtils.alert("§aCasted Rag Axe")
            val strengthGain = ((Minecraft.getMinecraft().thePlayer?.heldItem?.getSBStrength ?: return@onPacket) * 1.5).toInt()
            if (strengthGainedMessage) modMessage("§7Gained strength: §4$strengthGain")
            if (announceStrengthGained) partyMessage("Gained strength from Ragnarock Axe: $strengthGain")
        }
    }
}
