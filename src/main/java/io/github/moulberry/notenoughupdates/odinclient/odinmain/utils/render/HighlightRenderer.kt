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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RenderEntityModelEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RenderOverlayNoCaching
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock.Executor.Companion.register
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderBoundingBox
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HighlightRenderer {
    enum class HighlightType {
        Outline, Boxes, Box2d, Overlay
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val boxStyle: Int = 0)
    const val HIGHLIGHT_MODE_DEFAULT = "Outline"

    val highlightModeList = arrayListOf("Outline", "Boxes", "Box 2D", "Overlay")
    const val HIGHLIGHT_MODE_DESCRIPTION = "The type of highlight to use."

    private val entityGetters: MutableList<Pair<() -> HighlightType, () -> Collection<HighlightEntity>>> = mutableListOf()
    val entities = mapOf<HighlightType, MutableList<HighlightEntity>>(
        HighlightType.Outline to mutableListOf(),
        HighlightType.Boxes to mutableListOf(),
        HighlightType.Box2d to mutableListOf(),
        HighlightType.Overlay to mutableListOf()
    )

    fun addEntityGetter(type: () -> HighlightType, getter: () -> Collection<HighlightEntity>) {
        this.entityGetters.add(type to getter)
    }

    init {
        Executor(200, "HighlightRenderer") {
            entities.values.forEach { it.clear() }
            entityGetters.forEach { (type, getter) ->
                entities[type()]?.addAll(getter())
            }
        }.register()
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entities[HighlightType.Boxes]?.forEach {
            if (!it.entity.isEntityAlive) return@forEach
            Renderer.drawStyledBox(it.entity.renderBoundingBox, it.color, it.boxStyle, it.thickness, it.depth)
        }
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        entities[HighlightType.Outline]?.find { it.entity.isEntityAlive && it.entity == event.entity && (!it.depth || Minecraft.getMinecraft().thePlayer.isEntitySeen(it.entity)) }?.let {
            OutlineUtils.outlineEntity(event, it.color, it.thickness, false)
        }
    }

    @SubscribeEvent
    fun onOverlay(event: RenderOverlayNoCaching) {
        entities[HighlightType.Box2d]?.filter { !it.depth || Minecraft.getMinecraft().thePlayer.isEntitySeen(it.entity) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.color, it.thickness)
        }
    }

    private fun EntityPlayerSP.isEntitySeen(entityIn: Entity): Boolean {
        return Minecraft.getMinecraft().theWorld?.rayTraceBlocks(
            Vec3(this.posX, this.posY + this.getEyeHeight(), this.posZ),
            Vec3(entityIn.posX, entityIn.posY + entityIn.eyeHeight.toDouble(), entityIn.posZ), false, true, false
        ) == null
    }
}
