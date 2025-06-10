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
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether.FreshTimer.highlightFresh
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.nether.FreshTimer.highlightFreshColor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@OptIn(ExperimentalStdlibApi::class)
object TeamHighlight : Module(
    name = "Team Highlight",
    desc = "Highlights your teammates in Kuudra."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showHighlight by BooleanSetting("Show highlight", true, desc = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, desc = "Highlights teammates with a name tag.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Highlights teammates only when they are visible.")
    private val outlineColor by ColorSetting("Outline Color", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the player outline.").withDependency { showHighlight }
    private val nameColor by ColorSetting("Name Color", Colors.MINECRAFT_LIGHT_PURPLE, true, desc = "Color of the name highlight.").withDependency { showName }

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || !KuudraUtils.inKuudra || KuudraUtils.phase < 1 || !showHighlight) emptyList()
            else {
                kuudraTeammates.mapNotNull {
                    if (it.entity == Minecraft.getMinecraft().thePlayer) return@mapNotNull null
                    it.entity?.let { entity -> HighlightRenderer.HighlightEntity(entity, if (it.eatFresh && highlightFresh) highlightFreshColor else outlineColor, thickness, depthCheck, style) }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Specials.Pre<EntityOtherPlayerMP>) {
        if (!showName || !KuudraUtils.inKuudra || KuudraUtils.phase < 1 || event.entity == Minecraft.getMinecraft().thePlayer) return
        val teammate = kuudraTeammates.find { it.entity == event.entity } ?: return

        RenderUtils.drawMinecraftLabel(teammate.playerName, Vec3( event.x, event.y + 0.5, event.z), 0.05, false, if (teammate.eatFresh) highlightFreshColor else nameColor)
        event.isCanceled = true
    }
}
