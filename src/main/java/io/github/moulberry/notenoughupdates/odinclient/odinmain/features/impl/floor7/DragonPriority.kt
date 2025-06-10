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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.dragonPriorityToggle
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.dragonTitle
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.easyPower
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.normalPower
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.paulBuff
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.soloDebuff
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.devMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.Blessing
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonClass
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage

object DragonPriority {

    fun findPriority(spawningDragons: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        return if (!dragonPriorityToggle) {
            spawningDragons.sortBy { listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green).indexOf(it) }
            spawningDragons[0]
        } else
            sortPriority(spawningDragons)
    }

    fun displaySpawningDragon(dragon: WitherDragonsEnum) {
        if (dragon == WitherDragonsEnum.None) return
        if (dragonTitle && WitherDragons.enabled) PlayerUtils.alert("§${dragon.colorCode}${dragon.name} is spawning!", 30)
        if (dragonPriorityToggle && WitherDragons.enabled) modMessage("§${dragon.colorCode}${dragon.name} §7is your priority dragon!")
    }

    private fun sortPriority(spawningDragons: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = Blessing.POWER.current * (if (paulBuff) 1.25 else 1.0) + (if (Blessing.TIME.current > 0) 2.5 else 0.0)
        val playerClass = DungeonUtils.currentDungeonPlayer.clazz.apply { if (this == DungeonClass.Unknown) modMessage("§cFailed to get dungeon class.") }

        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)
        val priorityList =
            if (totalPower >= normalPower || (spawningDragons.any { it == WitherDragonsEnum.Purple } && totalPower >= easyPower))
                if (playerClass.equalsOneOf(DungeonClass.Berserk, DungeonClass.Mage)) dragonList else dragonList.reversed()
            else listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

        spawningDragons.sortBy { priorityList.indexOf(it) }

        if (totalPower >= easyPower) {
            if (soloDebuff == 1 && playerClass == DungeonClass.Tank && (spawningDragons.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll)) spawningDragons.sortByDescending { priorityList.indexOf(it) }
            else if (playerClass == DungeonClass.Healer && (spawningDragons.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll)) spawningDragons.sortByDescending { priorityList.indexOf(it) }
        }

        devMessage("§7Priority: §6$totalPower §7Class: §${playerClass.colorCode}${playerClass.name} §7Dragons: §a${spawningDragons.joinToString(", ") { it.name }} §7-> §c${priorityList.joinToString(", ") { it.name.first().toString() }}")
        return spawningDragons[0]
    }
}
