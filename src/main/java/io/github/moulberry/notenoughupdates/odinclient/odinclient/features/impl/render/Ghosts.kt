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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.getSBMaxHealth
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft
import net.minecraft.entity.monster.EntityCreeper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Ghosts : Module(
    name = "Ghosts",
    desc = "Adds visual changes to ghosts."
) {
    private var showGhostNametag by BooleanSetting("Show Ghost Nametag", desc = "Show the ghost's name tag.")
    private var showGhosts by BooleanSetting("Hide Ghosts", desc = "Hide ghosts.")
    private var hideChargedLayer by BooleanSetting("Hide Charged Layer", desc = "Hide the charged layer of the ghost.")

    private var creeperList = mutableSetOf<EntityCreeper>()

    init {
        execute(500) {
            creeperList.clear()
             Minecraft.getMinecraft().theWorld?.loadedEntityList?.forEach { entity ->
                 if (entity !is EntityCreeper || entity.getSBMaxHealth() < 1000000) return@forEach
                 entity.isInvisible = showGhosts
                 entity.dataWatcher.updateObject(17, (if (hideChargedLayer) 0 else 1).toByte())
                 creeperList.add(entity)
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!showGhostNametag) return
        creeperList.forEach { entity ->
            if (entity.isDead) return@forEach
            val isRunic = entity.getSBMaxHealth() == 4000000f
            val bracketsColor = if (isRunic) "&5" else "&8"
            val lvlColor = if (isRunic) "&d" else "&7"
            val nameColor = if (isRunic) "&5" else "&c"
            val currentHealthColor = if (isRunic) "&d" else if (entity.health < entity.getSBMaxHealth() / 2) "&e" else "&a"
            val maxHealthColor = if (isRunic) "&5" else "&a"
            val name = "${bracketsColor}[${lvlColor}Lv250${bracketsColor}] ${nameColor + if (isRunic) "Runic " else ""}Ghost ${currentHealthColor + transformToSuffixedNumber(entity.health) + "&f"}/${maxHealthColor + transformToSuffixedNumber(entity.getSBMaxHealth()) + "&c" + "❤"}".replace("&", "§")

            Renderer.drawStringInWorld(name, entity.renderVec.addVec(y = entity.height + 0.5), Colors.WHITE, depth = false)
        }
    }

    private fun transformToSuffixedNumber(number: Float): String {
        val result = if (number >= 1000000) {
            val short = (number / 1000000).toString()
            val shortSplit = short.split(".")
            if (shortSplit[1] != "0") short else shortSplit[0] + "M"
        } else (number / 1000).toInt().toString() + "k"
        return result
    }
}
