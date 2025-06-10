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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.containsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.name
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RemovePerks : Module(
    name = "Remove Perks",
    desc = "Removes certain perks from the perk menu."
) {
    private val renderStun by BooleanSetting("Show Stun", false, desc = "Shows the stun role perks.")

    @SubscribeEvent
    fun renderSlot(event: GuiEvent.DrawSlot) {
        if (event.gui.inventorySlots?.name == "Perk Menu" && slotCheck(event.slot.stack?.unformattedName ?: return)) event.isCanceled = true
    }

    @SubscribeEvent
    fun guiMouseClick(event: GuiEvent.MouseClick) = with(event.gui) {
        if (this is GuiChest && inventorySlots?.name == "Perk Menu" && slotCheck(slotUnderMouse?.stack?.unformattedName ?: return))
            event.isCanceled = true
    }

    private fun slotCheck(slot: String): Boolean {
        return slot.containsOneOf("Steady Hands", "Bomberman", "Mining Frenzy") || slot.equalsOneOf("Elle's Lava Rod", "Elle's Pickaxe", "Auto Revive") ||
                (!renderStun && slot.containsOneOf("Human Cannonball"))
    }
}
