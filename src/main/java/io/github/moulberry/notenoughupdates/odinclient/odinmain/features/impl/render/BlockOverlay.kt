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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.getBlockAt
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.usingEtherWarp
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@OptIn(ExperimentalStdlibApi::class)
object BlockOverlay : Module(
    name = "Block Overlay",
    desc = "Lets you customize the vanilla block overlay.",
) {
    private val blockOverlayToggle by BooleanSetting("Block Overlay", true, desc = "Master toggle for Block Overlay feature.")

    private val style by SelectorSetting("Block Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { blockOverlayToggle }
    private val color by ColorSetting("Block Color", Colors.BLACK.withAlpha(0.4f), allowAlpha = true, desc = "The color of the box.").withDependency { blockOverlayToggle }
    private val lineWidth by NumberSetting("Block Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.").withDependency { blockOverlayToggle }
    private val depthCheck by BooleanSetting("Depth check", true, desc = "Boxes show through walls.").withDependency { blockOverlayToggle }
    private val lineSmoothing by BooleanSetting("Line Smoothing", true, desc = "Makes the lines smoother.").withDependency { blockOverlayToggle && (style == 1 || style == 2) }
    private val disableWhenEtherwarping by BooleanSetting("Disable When Etherwarping", true, desc = "Disables the block overlay when etherwarping.").withDependency { blockOverlayToggle }

    private val entityToggle by BooleanSetting("Entity Hover", false, desc = "Master toggle for Entity Hover feature.")

    private val entityMode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION).withDependency { entityToggle }
    private val entityColor by ColorSetting("Entity Color", Colors.WHITE.withAlpha(0.75f), true, desc = "The color of the highlight.").withDependency { entityToggle }
    private val thickness by NumberSetting("Entity Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { entityToggle && entityMode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val entityStyle by SelectorSetting("Entity Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { entityToggle && entityMode == HighlightRenderer.HighlightType.Boxes.ordinal }

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[entityMode]}) {
            if (!entityToggle || !enabled) emptyList()
            else Minecraft.getMinecraft().objectMouseOver.entityHit?.takeIf { !it.isInvisible }?.let { listOf(HighlightRenderer.HighlightEntity(it, entityColor, thickness, true, entityStyle)) } ?: emptyList()
        }
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: DrawBlockHighlightEvent) {
        if (event.target.typeOfHit != MovingObjectType.BLOCK || Minecraft.getMinecraft().gameSettings?.thirdPersonView != 0 || (disableWhenEtherwarping && Minecraft.getMinecraft().thePlayer.usingEtherWarp)) return
        event.isCanceled = true

        if (getBlockAt(event.target.blockPos).material === Material.air || event.target.blockPos !in Minecraft.getMinecraft().theWorld.worldBorder) return

        Renderer.drawStyledBlock(event.target.blockPos, color, style, lineWidth, depthCheck, lineSmoothing)
    }
}
