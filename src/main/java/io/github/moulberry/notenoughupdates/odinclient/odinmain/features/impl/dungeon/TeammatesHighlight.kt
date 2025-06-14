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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RenderOverlayNoCaching
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils2D
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@OptIn(ExperimentalStdlibApi::class)
object TeammatesHighlight : Module(
    name = "Teammate Highlight",
    desc = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes / 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showClass by BooleanSetting("Show class", true, desc = "Shows the class of the teammate.")
    private val showHighlight by BooleanSetting("Show highlight", true, desc = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, desc = "Highlights teammates with a name tag.")
    private val nameStyle by SelectorSetting("Name Style", "Plain Text", arrayListOf("Plain Text", "Oringo Style"), desc = "The style of the name tag to render.").withDependency { showName }
    private val backgroundColor by ColorSetting("Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the nametag background").withDependency { showName && nameStyle == 1 }
    private val accentColor by ColorSetting("Accent Color", Colors.MINECRAFT_BLUE, true, desc = "The color of the nametag accent").withDependency { showName && nameStyle == 1 }
    private val padding by NumberSetting("Padding", 5, min = 0, max = 20, increment = 1, desc = "The padding around the text of the nametag.").withDependency { showName && nameStyle == 1 }
    private val scale by NumberSetting("Scale", 0.8f, min = 0, max = 2, increment = 0.1, desc = "The scale of the nametag").withDependency { showName && nameStyle ==1 }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Highlights teammates only when they are visible.")
    private val inBoss by BooleanSetting("In boss", true, desc = "Highlights teammates in boss rooms.")

    private inline val shouldRender get() = (inBoss || !DungeonUtils.inBoss) && DungeonUtils.inDungeons

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || !shouldRender || !showHighlight) emptyList()
            else dungeonTeammatesNoSelf.mapNotNull { it.entity?.let { entity -> HighlightRenderer.HighlightEntity(entity, it.clazz.color, thickness, depthCheck, style) } }
        }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Specials.Pre<EntityOtherPlayerMP>) {
        if (!showName || !shouldRender) return
        val teammate = dungeonTeammatesNoSelf.find { it.entity == event.entity } ?: return
        event.isCanceled = true
        if (nameStyle == 0) RenderUtils.drawMinecraftLabel(
            if (showClass) "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]" else "§${teammate.clazz.colorCode}${teammate.name}",
            Vec3(event.x, event.y + 0.5, event.z), 0.05, false
        )
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderOverlayNoCaching) {
        if (!showName || !shouldRender || nameStyle == 0) return
        dungeonTeammatesNoSelf.forEach { teammate -> teammate.entity?.let { entity ->
            RenderUtils2D.drawBackgroundNameTag(
                if (showClass) "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]" else "§${teammate.clazz.colorCode}${teammate.name}",
                entity, padding, backgroundColor, accentColor, scale = scale
            )
        } }
    }
}
