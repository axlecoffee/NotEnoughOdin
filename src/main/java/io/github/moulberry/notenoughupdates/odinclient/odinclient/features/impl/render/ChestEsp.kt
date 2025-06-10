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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RenderChestEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.SelectorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.Island
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.LocationUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.getBlockAt
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toAABB
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object ChestEsp : Module(
    name = "Chest Esp",
    desc = "Displays chests through walls."
) {
    private val onlyDungeon by BooleanSetting("Only Dungeon", desc = "Only show chests in dungeons.")
    private val onlyCH by BooleanSetting("Only Crystal Hollows", desc = "Only show chests in Crystal Hollows.")
    private val hideClicked by BooleanSetting("Hide Clicked", desc = "Hide chests that have been clicked.")
    private val renderMode by SelectorSetting("Render Mode", "Chams", arrayListOf("Chams", "Outline"), desc = "The rendering mode.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_RED, allowAlpha = true, desc = "The color of the chest ESP.")

    private val clickedChests = mutableSetOf<BlockPos>()
    private var chests = mutableSetOf<BlockPos>()

    init {
        onWorldLoad { clickedChests.clear() }

        onPacket<C08PacketPlayerBlockPlacement> {
            if (getBlockAt(it.position).equalsOneOf(Blocks.chest, Blocks.trapped_chest)) clickedChests.add(it.position)
        }

        execute(200) {
            chests = Minecraft.getMinecraft().theWorld?.loadedTileEntityList?.mapNotNull { (it as? TileEntityChest)?.pos }?.toMutableSet() ?: mutableSetOf()
        }
    }

    @SubscribeEvent
    fun onRenderChest(event: RenderChestEvent.Pre) {
        if (renderMode != 0 || event.chest != Minecraft.getMinecraft().theWorld?.getTileEntity(event.chest.pos)) return
        if (hideClicked && event.chest.pos in clickedChests) return
        if ((onlyDungeon && DungeonUtils.inDungeons) || (onlyCH && LocationUtils.currentArea.isArea(Island.CrystalHollows)) || (!onlyDungeon && !onlyCH)) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL)
            GlStateManager.color(1f, 1f, 1f, color.alphaFloat)
            GlStateManager.enablePolygonOffset()
            GlStateManager.doPolygonOffset(1f, -1000000f)
        }
    }

    @SubscribeEvent
    fun onRenderChest(event: RenderChestEvent.Post) {
        if (!(onlyDungeon && DungeonUtils.inDungeons) && !(onlyCH && LocationUtils.currentArea.isArea(Island.CrystalHollows)) && !(!onlyDungeon && !onlyCH)) return
        if (hideClicked && event.chest.pos in clickedChests) return

        if (renderMode == 1) Renderer.drawBox(event.chest.pos.toAABB(), color, 1f, depth = false, fillAlpha = 0)
        else if (renderMode == 0 && event.chest == Minecraft.getMinecraft().theWorld?.getTileEntity(event.chest.pos)) {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL)
            GlStateManager.doPolygonOffset(1f, 1000000f)
            GlStateManager.disablePolygonOffset()
        }
    }
}
