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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.config.Config
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock.PetKeybinds.petList
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isHolding
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.uuid

val petCommand = Commodore("petkeys") {
    literal("add").runs {
        val petID = if (isHolding("PET")) Minecraft.getMinecraft().thePlayer?.heldItem.uuid else null
        if (petID == null) return@runs modMessage("You can only add pets to the pet list!")
        if (petList.size >= 9) return@runs modMessage("You cannot add more than 9 pets to the list. Remove a pet using /petkeys remove or clear the list using /petkeys clear.")
        if (petID in petList) return@runs modMessage("This pet is already in the list!")

        petList.add(petID)
        modMessage("Added this pet to the pet list in position ${petList.indexOf(petID) +1}!")
        Config.save()
    }

    literal("petpos").runs {
        val petID = if (isHolding("PET")) Minecraft.getMinecraft().thePlayer?.heldItem.uuid else return@runs modMessage("This is not a pet!")
        if (petID !in petList) return@runs modMessage("This pet is not in the list!")
        modMessage("This pet is position ${petList.indexOf(petID) +1} in the list.")
    }

    literal("remove").runs {
        val petID = if (isHolding("PET")) Minecraft.getMinecraft().thePlayer?.heldItem.uuid else return@runs modMessage("This is not a pet!")
        if (petID !in petList) return@runs modMessage("This pet is not in the list!")

        petList.remove(petID)
        modMessage("Removed this pet from the pet list!")
        Config.save()
    }

    literal("clear").runs {
        petList.clear()
        modMessage("Cleared the pet list!")
        Config.save()
    }

    literal("list").runs {
        if (petList.isEmpty()) return@runs modMessage("Pet list is empty")
        modMessage("Pet list:\n${petList.joinToString("\n")}")
    }
}
