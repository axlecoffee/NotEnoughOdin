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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.isLegitVersion
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.isOtherPlayer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer.HighlightEntity
import net.minecraft.client.Minecraft

@OptIn(ExperimentalStdlibApi::class)
object CustomHighlight : Module(
    name = "Custom Highlight",
    tag = TagType.FPSTAX,
    desc = "Allows you to highlight selected mobs. (/highlight)"
) {
    private val starredMobESP by BooleanSetting("Starred Mob Highlight", true, desc = "Highlights mobs with a star in their name.")
    private val shadowAssassin by BooleanSetting("Shadow Assassin", false, desc = "Highlights Shadow Assassins.").withDependency { !isLegitVersion }
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)

    private val color by ColorSetting("Color", Colors.WHITE.withAlpha(0.75f), true, desc = "The color of the highlight.")
    private val starredColor by ColorSetting("Starred Mob Color", Colors.WHITE.withAlpha(0.75f), true, desc = "The color of highlighted starred mobs.").withDependency { starredMobESP }
    private val shadowAssassinColor by ColorSetting("Shadow Assassin Color", Colors.WHITE.withAlpha(0.75f), true, desc = "The color of highlighted Shadow Assassins.").withDependency { !isLegitVersion && shadowAssassin }
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val scanDelay by NumberSetting("Scan Delay", 100L, 20L, 2000L, 20L, desc = "The delay between entity scans.", unit = "ms")

    private val xray by BooleanSetting("Depth Check", false, desc = "Highlights entities through walls.").withDependency { !isLegitVersion }
    private val showInvisible by BooleanSetting("Show Invisible", false, desc = "Highlights invisible entities.").withDependency { !isLegitVersion }

    val highlightMap: MutableMap<String, Color?> by MapSetting("highlightMap", mutableMapOf())

    private inline val depthCheck get() = if (isLegitVersion) true else xray
    val currentEntities = mutableSetOf<HighlightEntity>()

    init {
        execute({ scanDelay }) {
            if (highlightMap.isEmpty() && ((!DungeonUtils.inDungeons || !starredMobESP && !shadowAssassin) )) return@execute
            currentEntities.clear()
            getEntities()
        }

        onWorldLoad { currentEntities.clear() }

        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentEntities
        }
    }

    private fun getEntities() {
        Minecraft.getMinecraft().theWorld?.loadedEntityList?.forEach { entity ->
            checkEntity(entity)
            if (starredMobESP) checkStarred(entity)
            if (shadowAssassin && !isLegitVersion) checkAssassin(entity)
            if (showInvisible && entity.isInvisible && !isLegitVersion && currentEntities.any { it.entity == entity }) entity.isInvisible = false
        }
    }

    private fun checkEntity(entity: Entity) {
        if (entity !is EntityArmorStand || highlightMap.none { entity.name.contains(it.key, true) } || currentEntities.any { it.entity == entity}  || !entity.alwaysRenderNameTag && !depthCheck) return
        val highlightColor = getColorFromList(entity.name)
        currentEntities.add(HighlightEntity(getMobEntity(entity) ?: return, highlightColor, thickness, depthCheck, style))
    }

    private fun checkStarred(entity: Entity) {
        if (entity !is EntityArmorStand || !entity.name.startsWith("§6✯ ") || !entity.name.endsWith("§c❤") || currentEntities.any { it.entity == entity} || (!entity.alwaysRenderNameTag && depthCheck)) return
        currentEntities.add(HighlightEntity(getMobEntity(entity) ?: return, starredColor, thickness, depthCheck, style))
    }

    private fun checkAssassin(entity: Entity) {
        if (entity !is EntityOtherPlayerMP || entity.name != "Shadow Assassin") return
        currentEntities.add(HighlightEntity(entity, shadowAssassinColor, thickness, depthCheck, style))
    }

    private fun getMobEntity(entity: Entity): Entity? {
        return Minecraft.getMinecraft().theWorld?.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
            ?.filter { it !is EntityArmorStand && Minecraft.getMinecraft().thePlayer != it && !(it is EntityWither && it.isInvisible) && !(it is EntityOtherPlayerMP && it.isOtherPlayer()) }
            ?.minByOrNull { entity.getDistanceToEntity(it) }
    }

    private fun getColorFromList(name: String): Color =
        highlightMap.entries.firstOrNull { name.contains(it.key, true) }?.value ?: color

}
