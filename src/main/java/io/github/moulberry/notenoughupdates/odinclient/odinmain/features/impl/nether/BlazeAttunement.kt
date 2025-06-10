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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.noControlCodes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton
import kotlin.collections.set

@OptIn(ExperimentalStdlibApi::class)
object BlazeAttunement : Module(
    name = "Blaze Attunement",
    desc = "Displays what attunement a blaze boss currently requires."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }

    private var currentBlazes = hashMapOf<Entity, Color>()

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentBlazes.map { HighlightRenderer.HighlightEntity(it.key, it.value, thickness, true, style) }
        }

        execute(250) {
            currentBlazes.clear()
            Minecraft.getMinecraft().theWorld?.loadedEntityList?.forEach { entity ->
                if (entity !is EntityArmorStand || currentBlazes.any { it.key == entity }) return@forEach
                val name = entity.name.noControlCodes

                val color = when {
                    name.contains("CRYSTAL ♨") -> Colors.MINECRAFT_AQUA
                    name.contains("ASHEN ♨") -> Colors.MINECRAFT_GRAY
                    name.contains("AURIC ♨") -> Colors.MINECRAFT_YELLOW
                    name.contains("SPIRIT ♨") -> Colors.WHITE
                    else -> return@forEach
                }.withAlpha(.4f)

                currentBlazes[Minecraft.getMinecraft().theWorld?.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
                    ?.filter { it is EntityBlaze || it is EntitySkeleton || it is EntityPigZombie }
                    ?.sortedByDescending { it.positionVector.squareDistanceTo(entity.positionVector) }
                    ?.takeIf { it.isNotEmpty() }?.firstOrNull() ?: return@execute] = color
            }
        }
    }
}
