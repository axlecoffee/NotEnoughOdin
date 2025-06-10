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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.isOtherPlayer
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage

object KuudraReminders : Module(
    name = "Kuudra Reminders",
    desc = "Displays reminders about Kuudra."
) {
    private val displayText by BooleanSetting("Display Text", true, desc = "Displays kuudra information in chat.")
    private val playSound by BooleanSetting("Play Sound", true, desc = "Plays a sound when a kuudra event occurs.")
    private val keyReminder by BooleanSetting("Key Reminder", true, desc = "Reminds you to bring a key.")
    private val buyUpgrades by BooleanSetting("Buy Upgrades", true, desc = "Reminds you to buy upgrades.")
    private val pickUpSupplies by BooleanSetting("Pick Up Supplies", true, desc = "Reminds you to pick up supplies.")
    private val buildBallista by BooleanSetting("Build Ballista", true, desc = "Reminds you to build the ballista.")
    private val freshTools by BooleanSetting("Fresh Tools", true, desc = "Reminds you to use fresh tools.")
    private val manaDrain by BooleanSetting("Mana Drain", true, desc = "Notifies your party when you use mana on them.")
    private val onlyKuudra by BooleanSetting("Notify in Kuudra Only", true, desc = "Notify of mana drain only when in Kuudra.").withDependency { manaDrain }

    private data class Reminder(val regex: Regex, val shouldRun: () -> Boolean, val alert: String)
    private val reminders = listOf(
        Reminder(Regex("WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards."), { keyReminder }, "No key in inventory"),
        Reminder(Regex("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!"), { freshTools }, "Fresh Tools"),
        Reminder(Regex("\\[NPC] Elle: It's time to build the Ballista again! Cover me!"), { buildBallista }, "Build Ballista"),
        Reminder(Regex("\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!"), { buyUpgrades }, "Buy Upgrades"),
        Reminder(Regex("\\[NPC] Elle: Not again!"), { pickUpSupplies }, "Pick up supplies")
    )

    init {
        reminders.forEach { reminder ->
            onMessage(reminder.regex, { enabled && reminder.shouldRun() }) {
                PlayerUtils.alert(reminder.alert, playSound = playSound, displayText = displayText)
            }
        }

        onMessage(Regex("Used Extreme Focus! \\((\\d+) Mana\\)"), { enabled && manaDrain && (onlyKuudra && KuudraUtils.inKuudra) }) {
            val mana = it.groupValues[1].toIntOrNull() ?: return@onMessage
            val players = Minecraft.getMinecraft().theWorld?.playerEntities?.filter { entity -> entity.isOtherPlayer() && entity.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer) < 49 }?.takeIf { it.isNotEmpty() } ?: return@onMessage
            partyMessage("Used $mana mana on ${players.joinToString(", ") { player -> player.name }}.")
        }
    }
}
