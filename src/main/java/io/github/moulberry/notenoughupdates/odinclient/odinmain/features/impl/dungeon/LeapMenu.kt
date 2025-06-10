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

import io.github.moulberry.notenoughupdates.NEUApi
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.LeapHelper.leapHelperBossChatEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.LeapHelper.worldLoad
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Setting.Companion.withDependency
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.equalsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.name
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.noControlCodes
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils.drawTexturedModalRect
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.ClickType
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.PlayerUtils.windowClick
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonClass
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonPlayer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.modMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.partyMessage
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.unformattedName
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.Colors
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.ui.util.MouseUtils.getQuadrant
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display

object LeapMenu : Module(
    name = "Leap Menu",
    desc = "Renders a custom leap menu when in the Spirit Leap gui."
) {
    val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class (BetterMap)", "A-Z Name", "Custom sorting", "No Sorting"), desc = "How to sort the leap menu. /od leaporder to configure custom sorting.")
    private val onlyClass by BooleanSetting("Only Classes", false, desc = "Renders classes instead of names.")
    private val colorStyle by BooleanSetting("Color Style", false, desc = "Which color style to use.")
    private val backgroundColor by ColorSetting("Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.75f), allowAlpha = true, desc = "Color of the background of the leap menu.").withDependency { !colorStyle }
    private val roundedRect by BooleanSetting("Rounded Rect", true, desc = "Toggles the rounded rect for the gui.")
    private val useNumberKeys by BooleanSetting("Use Number Keys", false, desc = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind by KeybindSetting("Top Left", Keyboard.KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind by KeybindSetting("Top Right", Keyboard.KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", Keyboard.KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind by KeybindSetting("Bottom right", Keyboard.KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val size by NumberSetting("Scale Factor", 1.0f, 0.5f, 2.0f, 0.1f, desc = "Scale factor for the leap menu.")
    private val leapHelperToggle by BooleanSetting("Leap Helper", false, desc = "Highlights the leap helper player in the leap menu.")
    private val leapHelperColor by ColorSetting("Leap Helper Color", Colors.WHITE, desc = "Color of the Leap Helper highlight.").withDependency { leapHelperToggle }
    val delay by NumberSetting("Reset Leap Helper Delay", 30, 10.0, 120.0, 1.0, desc = "Delay for clearing the leap helper highlight.").withDependency { leapHelperToggle }
    private val leapAnnounce by BooleanSetting("Leap Announce", false, desc = "Announces when you leap to a player.")
    private val hoveredAnims = List(4) { EaseInOut(200L) }
    private var hoveredQuadrant = -1
    private var previouslyHoveredQuadrant = -1

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown, 0, ResourceLocation("textures/entity/steve.png"))
    private val keybindList = listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)

    @SubscribeEvent
    fun onDrawScreen(event: GuiEvent.DrawGuiBackground) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || !chest.name.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return
        hoveredQuadrant = getQuadrant()
        if (hoveredQuadrant != previouslyHoveredQuadrant && previouslyHoveredQuadrant != -1) {
            hoveredAnims[hoveredQuadrant - 1].start()
            hoveredAnims[previouslyHoveredQuadrant - 1].start(true)
        }
        previouslyHoveredQuadrant = hoveredQuadrant

        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.scale(1f / scaleFactor,  1f / scaleFactor, 0f)

            val displayWidth = Display.getWidth()
            val displayHeight = Display.getHeight()
            GlStateManager.translate(displayWidth / 2f, displayHeight / 2f, 0f)
            GlStateManager.scale(size, size, 1f)
            GlStateManager.translate(-displayWidth / 2f, -displayHeight / 2f, 0f)
            GlStateManager.translate(displayWidth / 2f, displayHeight / 2f, 0f)
            val boxWidth = 800
            val boxHeight = 300
            val x = when (index) {
                0, 2 -> -((displayWidth - (boxWidth * 2)) / 6 + boxWidth)
                else -> ((displayWidth - (boxWidth * 2)) / 6)
            }
            val y = when (index) {
                0, 1 -> -((displayHeight - (boxHeight * 2)) / 8 + boxHeight)
                else -> ((displayHeight - (boxHeight * 2)) / 8)
            }
            Minecraft.getMinecraft().textureManager.bindTexture(it.locationSkin)
            val color = if (colorStyle) it.clazz.color else backgroundColor
            if (it.name == LeapHelper.leapHelperName && leapHelperToggle)
                roundedRectangle(x - 25, y - 25, boxWidth + 50, boxHeight + 50, leapHelperColor, if (roundedRect) 12f else 0f)

            val box = Box(x, y, boxWidth, boxHeight).expand(hoveredAnims.getOrNull(index)?.get(0f, 15f, hoveredQuadrant - 1 != index) ?: 0f)
            dropShadow(box, 10f, 15f, if (getQuadrant() - 1 != index) backgroundColor else Colors.WHITE)
            roundedRectangle(box, color, if (roundedRect) 12f else 0f)

            drawTexturedModalRect(x + 30, y + 30, 240, 240,8f, 8f, 8, 8, 64f, 64f)

            text(if (!onlyClass) it.name else it.clazz.name, x + 265f, y + 155f, if (!colorStyle) it.clazz.color else backgroundColor, 48f)
            if (!onlyClass || it.isDead) text(if (it.isDead) "§cDEAD" else it.clazz.name, x + 270f, y + 210f, Colors.WHITE, 30f, shadow = true)
            rectangleOutline(x + 30, y + 30, 240, 240, color, 25f, 15f, 100f)
            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || !chest.name.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return
        if (Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiEvent.MouseClick) {
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (!gui.name.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty())  return

        val quadrant = getQuadrant()
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui)

        event.isCanceled = true
    }

    @SubscribeEvent
    fun keyTyped(event: GuiEvent.KeyPress) {
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (!useNumberKeys || !gui.name.equalsOneOf("Spirit Leap", "Teleport to Player") || keybindList.none { it.key == event.key } || leapTeammates.isEmpty()) return

        val index = keybindList.indexOfFirst { it.key == event.key }
        val playerToLeap = if (index + 1 > leapTeammates.size) return else leapTeammates[index]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui)

        event.isCanceled = true
    }

    private fun leapTo(name: String, containerChest: ContainerChest) {
        val index = containerChest.inventorySlots.subList(11, 16).firstOrNull {
            it.stack?.unformattedName?.noControlCodes?.substringAfter(' ')?.lowercase() == name.noControlCodes.lowercase()
        }?.slotIndex ?: return modMessage("Can't find player $name. This shouldn't be possible! are you nicked?")
        modMessage("Teleporting to $name.")
        windowClick(index, ClickType.Middle)
    }

    init {
        onMessage(Regex(".*")) {
            leapHelperBossChatEvent(it.value)
        }

        onMessage(Regex("You have teleported to (\\w{1,16})!")) {
            if (leapAnnounce) partyMessage("Leaped to ${it.groupValues[1]}!")
        }

        onWorldLoad {
            worldLoad()
        }
    }


   /* private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stiviaisd", DungeonClass.Healer),
        DungeonPlayer("Odtheking", DungeonClass.Archer),
        DungeonPlayer("Bonzi", DungeonClass.Mage),
        DungeonPlayer("Cezar", DungeonClass.Tank)
    )*/


    /**
     * Sorts the list of players based on their default quadrant and class priority.
     * The function first tries to place each player in their default quadrant. If the quadrant is already occupied,
     * the player is added to a second round list. After all players have been processed, the function fills the remaining
     * empty quadrants with the players from the second round list.
     *
     * @param players The list of players to be sorted.
     * @return An array of sorted players.
     */
    fun odinSorting(players: List<DungeonPlayer>): Array<DungeonPlayer> {
        val result = Array(4) { EMPTY }
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in players.sortedBy { it.clazz.priority }) {
            when {
                result[player.clazz.defaultQuadrant] == EMPTY -> result[player.clazz.defaultQuadrant] = player
                else -> secondRound.add(player)
            }
        }

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, _ ->
            when {
                result[index] == EMPTY -> {
                    result[index] = secondRound.removeAt(0)
                    if (secondRound.isEmpty()) return result
                }
            }
        }
        return result
    }
}
