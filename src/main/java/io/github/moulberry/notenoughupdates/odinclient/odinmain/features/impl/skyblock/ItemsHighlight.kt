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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.containsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonItemDrops
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.getRarity
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.lore
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.unformattedName
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityItem

@OptIn(ExperimentalStdlibApi::class)
object ItemsHighlight : Module(
    name = "Item Highlight",
    desc = "Highlights items on the ground."
) {
    private val mode by SelectorSetting("Mode", "Overlay", arrayListOf("Boxes", "Box 2D", "Overlay"), desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val onlySecrets by BooleanSetting("Only Secrets", false, desc = "Only highlights secret drops in dungeons.")
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")
    private val colorList = arrayListOf("Rarity", "Distance", "Custom")
    private val colorStyle by SelectorSetting("Color Style", "Rarity", colorList, desc = "Which color style to use.")
    private val rarityAlpha by NumberSetting("Rarity Alpha", 1f, 0f, 1f, .1f, desc = "The alpha of the rarity color.").withDependency { colorStyle == 0 }
    private val customColor by ColorSetting("Custom Color", Colors.WHITE.withAlpha(1f), true, desc = "The custom color to use.").withDependency { colorStyle == 2 }

    private var currentEntityItems = mutableSetOf<EntityItem>()

    init {
        execute(100) {
            currentEntityItems = mutableSetOf()
            Minecraft.getMinecraft().theWorld?.loadedEntityList?.forEach { entity ->
                if (entity !is EntityItem) return@forEach
                if (!onlySecrets || entity.entityItem?.unformattedName?.containsOneOf(dungeonItemDrops, true) == true) currentEntityItems.add(entity)
            }
        }

        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode + 1]}) {
            if (!enabled) emptyList()
            else currentEntityItems.map { HighlightRenderer.HighlightEntity(it, getEntityOutlineColor(it), thickness, depthCheck, style) }
        }
    }

    private fun getEntityOutlineColor(entity: EntityItem): Color {
        return when (colorStyle){
            0 -> getRarity(entity.entityItem.lore)?.color?.withAlpha(rarityAlpha) ?: Colors.WHITE
            1 -> when {
                entity.ticksExisted <= 11 -> Colors.MINECRAFT_YELLOW
                entity.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) <= 3.5 -> Colors.MINECRAFT_GREEN
                else -> Colors.MINECRAFT_RED
            }
            else -> customColor
        }
    }
}
