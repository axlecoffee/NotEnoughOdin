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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RenderEntityModelEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.ColorSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.NumberSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.addVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component1
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component2
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.component3
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.OutlineUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderVec
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderX
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderY
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.renderZ
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isHolding
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isLeap
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isShortbow
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.multiplyAlpha
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper.sqrt_double
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Trajectories : Module(
    name = "Trajectories",
    desc = "Displays the trajectory of pearls and bows."
) {
    private val bows by BooleanSetting("Bows", true, desc = "Render trajectories of bow arrows.")
    private val pearls by BooleanSetting("Pearls", true, desc = "Render trajectories of ender pearls.")
    private val plane by BooleanSetting("Show Plane", false, desc = "Shows a flat square rotated relative to the predicted block that will be hit.")
    private val boxes by BooleanSetting("Show Boxes", true, desc = "Shows boxes displaying where arrows or pearls will hit, if this is disabled it will only highlight entities your arrows will hit.")
    private val lines by BooleanSetting("Show Lines", true, desc = "Shows the trajectory as a line.")
    private val range by NumberSetting("Solver Range", 30, 1, 120, 1, desc = "How many ticks are simulated, performance impact scales with this.")
    private val width by NumberSetting("Line Width", 1f, 0.1f, 5.0, 0.1f, desc = "The width of the line.")
    private val planeSize by NumberSetting("Plane Size", 2f, 0.1f, 5.0, 0.1f, desc = "The size of the plane.").withDependency { plane }
    private val boxSize by NumberSetting("Box Size", 0.5f, 0.5f, 3.0f, 0.1f, desc = "The size of the box.").withDependency { boxes }
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_AQUA, true, desc = "The color of the trajectory.")
    private val depth by BooleanSetting("Depth Check", true, desc = "Whether or not to depth check the trajectory.")

    private var boxRenderQueue: MutableList<AxisAlignedBB> = mutableListOf()
    private var entityRenderQueue = mutableListOf<Entity>()
    private var pearlImpactPos: AxisAlignedBB? = null
    private var planePos: MovingObjectPosition? = null

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entityRenderQueue.clear()
        planePos = null
        if (Minecraft.getMinecraft().thePlayer?.heldItem == null) return
        if (bows && Minecraft.getMinecraft().thePlayer.heldItem.item is ItemBow) {
            val pair1: Pair<ArrayList<Vec3>, MovingObjectPosition?>
            var pair2: Pair<ArrayList<Vec3>, MovingObjectPosition?>
            var pair3: Pair<ArrayList<Vec3>, MovingObjectPosition?>
            if (Minecraft.getMinecraft().thePlayer.heldItem?.isShortbow == true) {
                pair1 = setBowTrajectoryHeading(0f, false)
                pair2 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
                pair3 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
                if (isHolding("TERMINATOR")) {
                    pair2 = setBowTrajectoryHeading(-5f, false)
                    pair3 = setBowTrajectoryHeading(5f, false)
                }
            } else {
                if (Minecraft.getMinecraft().thePlayer.itemInUseDuration == 0) return
                pair1 = setBowTrajectoryHeading(0f, true)
                pair2 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
                pair3 = Pair(arrayListOf(Vec3(0.0, 0.0, 0.0)), null)
            }
            if (boxes) drawBowCollisionBoxes()
            if (plane) {
                drawPlaneCollision(pair1.second)
                drawPlaneCollision(pair2.second)
                drawPlaneCollision(pair3.second)
            }
            if (lines) {
                Renderer.draw3DLine(pair1.first, color = color, lineWidth = width, depth = true)
                Renderer.draw3DLine(pair2.first, color = color, lineWidth = width, depth = true)
                Renderer.draw3DLine(pair3.first, color = color, lineWidth = width, depth = true)
            }
        }
        if (pearls) {
            pearlImpactPos = null
            val itemStack = Minecraft.getMinecraft().thePlayer?.heldItem ?: return
            if (itemStack.item is ItemEnderPearl && !itemStack.isLeap) {
                val pair = setPearlTrajectoryHeading()
                if (boxes) drawPearlCollisionBox()
                if (lines) Renderer.draw3DLine(pair.first, color = color, lineWidth = width, depth = true)
                if (plane) drawPlaneCollision(pair.second)
            }
        }
    }

    private fun setPearlTrajectoryHeading(): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        val player = Minecraft.getMinecraft().thePlayer ?: return Pair(arrayListOf(), null)

        val yawRadians = Math.toRadians(player.rotationYaw.toDouble())
        val pitchRadians = Math.toRadians(player.rotationPitch.toDouble())

        var motionX = -sin(yawRadians) * cos(pitchRadians) * 0.4
        var motionZ = cos(yawRadians) * cos(pitchRadians) * 0.4
        var motionY = -sin(pitchRadians) * 0.4

        val posX = player.renderX - cos(yawRadians) * 0.16
        val posY = player.renderY + player.eyeHeight - 0.1
        val posZ = player.renderZ - sin(yawRadians) * 0.16

        val f = sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ)
        motionX = (motionX / f) * 1.5
        motionY = (motionY / f) * 1.5
        motionZ = (motionZ / f) * 1.5

        return calculatePearlTrajectory(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun calculatePearlTrajectory(mV: Vec3,pV: Vec3): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        var hitResult = false
        var motionVec = mV
        var posVec = pV
        val lines = arrayListOf<Vec3>()
        var rayTraceHit: MovingObjectPosition? = null
        repeat(range + 1) {
            if (hitResult) return@repeat
            lines.add(posVec)
            Minecraft.getMinecraft().theWorld?.rayTraceBlocks(posVec, motionVec.add(posVec), false, true, false)?.let {
                rayTraceHit = it
                lines.add(it.hitVec)
                pearlImpactPos =  AxisAlignedBB(
                    it.hitVec.xCoord - 0.15 * boxSize, it.hitVec.yCoord - 0.15 * boxSize, it.hitVec.zCoord - 0.15 * boxSize,
                    it.hitVec.xCoord + 0.15 * boxSize, it.hitVec.yCoord + 0.15 * boxSize, it.hitVec.zCoord + 0.15 * boxSize
                )
                hitResult = true
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.03, motionVec.zCoord * 0.99)
        }

        return lines to rayTraceHit
    }

    private fun setBowTrajectoryHeading(yawOffset: Float, bowCharge: Boolean): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        val charge = if (bowCharge) minOf((72000 - Minecraft.getMinecraft().thePlayer.itemInUseCount) / 20f, 1.0f) * 2 else 2f

        val yawRadians = Math.toRadians((Minecraft.getMinecraft().thePlayer.rotationYaw + yawOffset).toDouble())
        val pitchRadians = Math.toRadians(Minecraft.getMinecraft().thePlayer.rotationPitch.toDouble())
        val (renderX, renderY, renderZ) = Minecraft.getMinecraft().thePlayer?.renderVec ?: return Pair(arrayListOf(), null)

        val posX = renderX - cos(Math.toRadians(Minecraft.getMinecraft().thePlayer.rotationYaw.toDouble())) * 0.16
        val posY = renderY + Minecraft.getMinecraft().thePlayer.eyeHeight - 0.1
        val posZ = renderZ - sin(Math.toRadians(Minecraft.getMinecraft().thePlayer.rotationYaw.toDouble())) * 0.16

        var motionX = -sin(yawRadians) * cos(pitchRadians)
        var motionY = -sin(pitchRadians)
        var motionZ = cos(yawRadians) * cos(pitchRadians)

        val lengthOffset = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)
        motionX = (motionX / lengthOffset) * charge * 1.5
        motionY = (motionY / lengthOffset) * charge * 1.5
        motionZ = (motionZ / lengthOffset) * charge * 1.5

        return calculateBowTrajectory(Vec3(motionX, motionY, motionZ), Vec3(posX, posY, posZ))
    }

    private fun calculateBowTrajectory(mV: Vec3,pV: Vec3): Pair<ArrayList<Vec3>, MovingObjectPosition?> {
        var hitResult = false
        var motionVec = mV
        var posVec = pV
        val lines = arrayListOf<Vec3>()
        var rayTraceHit: MovingObjectPosition? = null
        repeat(range + 1) {
            if (hitResult) return@repeat
            lines.add(posVec)
            val aabb = AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .offset(posVec.xCoord, posVec.yCoord, posVec.zCoord)
                .addCoord(motionVec.xCoord, motionVec.yCoord, motionVec.zCoord)
                .expand(0.01, 0.01, 0.01)
            val entityHit = Minecraft.getMinecraft().theWorld?.getEntitiesWithinAABBExcludingEntity(Minecraft.getMinecraft().thePlayer, aabb)?.filter { it !is EntityArrow && it !is EntityArmorStand }.orEmpty()
            if (entityHit.isNotEmpty()) {
                hitResult = true
                entityRenderQueue.addAll(entityHit)
            } else {
                Minecraft.getMinecraft().theWorld?.rayTraceBlocks(posVec, motionVec.add(posVec), false, true, false)?.let {
                    rayTraceHit = it
                    lines.add(it.hitVec)
                    if (boxes) {
                        boxRenderQueue.add(
                            AxisAlignedBB(
                                it.hitVec.xCoord - 0.15 * boxSize, it.hitVec.yCoord - 0.15 * boxSize, it.hitVec.zCoord - 0.15 * boxSize,
                                it.hitVec.xCoord + 0.15 * boxSize, it.hitVec.yCoord + 0.15 * boxSize, it.hitVec.zCoord + 0.15 * boxSize
                            )
                        )
                    }
                    hitResult = true
                }
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.05, motionVec.zCoord * 0.99)
        }
        return lines to rayTraceHit
    }

    private fun drawPlaneCollision(rayTrace: MovingObjectPosition?) {
        val vec1: Vec3
        val vec2: Vec3
        when (rayTrace?.sideHit) {
            EnumFacing.DOWN, EnumFacing.UP -> {
                vec1 = rayTrace.hitVec.addVec(-0.15 * planeSize, -0.02, -0.15 * planeSize)
                vec2 = rayTrace.hitVec.addVec(0.15 * planeSize, 0.02, 0.15 * planeSize)
            }
            EnumFacing.NORTH, EnumFacing.SOUTH -> {
                vec1 = rayTrace.hitVec.addVec(-0.15 * planeSize, -0.15 * planeSize, -0.02)
                vec2 = rayTrace.hitVec.addVec(0.15 * planeSize, 0.15 * planeSize, 0.02)
            }
            EnumFacing.WEST, EnumFacing.EAST -> {
                vec1 = rayTrace.hitVec.addVec(-0.02, -0.15 * planeSize, -0.15 * planeSize)
                vec2 = rayTrace.hitVec.addVec(0.02, 0.15 * planeSize, 0.15 * planeSize)
            }
            else -> return
        }
        RenderUtils.drawFilledAABB(AxisAlignedBB(vec1.xCoord, vec1.yCoord, vec1.zCoord, vec2.xCoord, vec2.yCoord, vec2.zCoord), color.multiplyAlpha(0.5f), false)
    }

    private fun drawPearlCollisionBox() {
        pearlImpactPos?.let { aabb ->
            Renderer.drawBox(aabb, color, width, depth = false, fillAlpha = 0)
            pearlImpactPos = null
        }
    }

    private fun drawBowCollisionBoxes() {
        if (boxRenderQueue.isEmpty()) return
        val renderVec = Minecraft.getMinecraft().thePlayer?.renderVec ?: return
        for (axisAlignedBB in boxRenderQueue) {
            if (
                hypot(
                    renderVec.xCoord - axisAlignedBB.minX,
                    renderVec.yCoord + Minecraft.getMinecraft().thePlayer.eyeHeight - axisAlignedBB.minY,
                    renderVec.zCoord - axisAlignedBB.minZ
                ) < 2
            ) {
                boxRenderQueue.clear()
                return
            }

            Renderer.drawBox(axisAlignedBB, color, width, depth = true, fillAlpha = 0)
        }
        boxRenderQueue.clear()
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        val player = Minecraft.getMinecraft().thePlayer ?: return
        val entity = event.entity

        if (entity !in entityRenderQueue ||
            !player.canEntityBeSeen(entity) ||
            !bows || player.heldItem?.item !is ItemBow ||
            (entity is EntityBlaze && DungeonUtils.inDungeons) ||
            (entity is EntityWither && entity.isInvisible)
        ) return

        OutlineUtils.outlineEntity(event, color, width, depth)
    }

    private fun hypot(x: Double, y: Double, d: Double): Double = sqrt(x * x + y * y + d * d)
}
