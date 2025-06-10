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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PostEntityMetadata
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.StringSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Clock
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.sendChatMessage
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MobSpawn: Module(
    name = "Mob Spawn",
    desc = "Sends a message whenever a mob spawns."
) {
    private val mobName by StringSetting("Mob Name", "MobName", 40, desc = "Message sent when mob is detected as spawned.")
    private val soundOnly by BooleanSetting("Sound Only", false, desc = "Only plays sound when mob spawns.")
    private val delay by NumberSetting("Time between alerts", 3000L, 10, 10000, 10, desc = "Time between alerts.", unit = "ms")
    private val ac by BooleanSetting("All Chat", false , desc = "Send message in all chat.")
    private val pc by BooleanSetting("Party Chat", false, desc = "Send message in party chat.")

    private val time = Clock(delay)

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        val entity = Minecraft.getMinecraft().theWorld?.getEntityByID(event.packet.entityId) ?: return
        if (!entity.name.contains(mobName, true) || !time.hasTimePassed(delay)) return
        time.update()

        modMessage("§5$mobName has spawned!")
        PlayerUtils.alert("§5$mobName has spawned!", playSound = !soundOnly)
        if (ac) sendChatMessage("$mobName spawned at: ${PlayerUtils.getPositionString()}")
        if (pc) partyMessage("$mobName spawned at: x: ${PlayerUtils.getPositionString()}")
    }
}
