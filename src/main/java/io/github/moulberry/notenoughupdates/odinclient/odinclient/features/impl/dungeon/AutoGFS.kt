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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.fillItemFromSack
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.runIn
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendCommand
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.skyblockID
import net.minecraft.client.Minecraft

object AutoGFS : Module(
    name = "Auto GFS",
    desc = "Automatically refills certain items from your sacks."
) {
    private val inKuudra by BooleanSetting("In Kuudra", true, desc = "Only gfs in Kuudra.")
    private val inDungeon by BooleanSetting("In Dungeon", true, desc = "Only gfs in Dungeons.")
    private val refillOnDungeonStart by BooleanSetting("Refill on Dungeon Start", true, desc = "Refill when a dungeon starts.")
    private val refillPearl by BooleanSetting("Refill Pearl", true, desc = "Refill ender pearls.")
    private val refillJerry by BooleanSetting("Refill Jerry", true, desc = "Refill inflatable jerrys.")
    private val refillTNT by BooleanSetting("Refill TNT", true, desc = "Refill superboom tnt.")
    private val refillOnTimer by BooleanSetting("Refill on Timer", true, desc = "Refill on a 5s intervals.")
    private val timerIncrements by NumberSetting("Timer Increments", 5L, 1, 60, desc = "The interval in which to refill.", unit = "s")
    private val autoGetDraf by BooleanSetting("Auto Get Draf", true, desc = "Automatically get draf from the sack.")

    init {
        execute({ timerIncrements * 1000 }) {
            if (refillOnTimer) refill()
        }

        onMessage(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!")) {
            if (refillOnDungeonStart) refill()
        }

        onMessage(Regex("^PUZZLE FAIL! (\\w{1,16}) .+\$|^\\[STATUE\\] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.\$")) {
            if (!autoGetDraf) return@onMessage
            runIn(30) {
                modMessage("§7Fetching Draf from sack...")
                sendCommand("gfs architect's first draft 1")
            }
        }
    }

    private fun refill() {
        if (DungeonUtils.isGhost || Minecraft.getMinecraft().currentScreen != null || !(inKuudra && KuudraUtils.inKuudra) && !(inDungeon && DungeonUtils.inDungeons)) return
        val inventory = Minecraft.getMinecraft().thePlayer?.inventory?.mainInventory ?: return

        inventory.find { it?.skyblockID == "ENDER_PEARL" }?.takeIf { refillPearl }?.also { fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", false) }

        inventory.find { it?.skyblockID == "INFLATABLE_JERRY" }?.takeIf { refillJerry }?.also { fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", false) }

        inventory.find { it?.skyblockID == "SUPERBOOM_TNT" }.takeIf { refillTNT }?.also { fillItemFromSack(64, "SUPERBOOM_TNT", "superboom_tnt", false) }
    }
}
