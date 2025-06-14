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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.logger
import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RoomEnterEvent
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.RenderUtils
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Renderer
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.startsWithOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.toVec3
import net.minecraft.util.BlockPos
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object QuizSolver {
    private var answers: MutableMap<String, List<String>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/quizAnswers.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
    private var triviaAnswers: List<String>? = null

    private var triviaOptions: MutableList<TriviaAnswer> = MutableList(3) { TriviaAnswer(null, false) }
    private data class TriviaAnswer(var blockPos: BlockPos?, var isCorrect: Boolean)

    init {
        try {
            val text = isr?.readText()
            answers = gson.fromJson(text, object : TypeToken<MutableMap<String, List<String>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading quiz answers", e)
            answers = mutableMapOf()
        }
    }

    fun onMessage(msg: String) {
        if (msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.endsWith("correctly!")) {
            if (msg.contains("answered the final question")) {
                onPuzzleComplete("Quiz")
                reset()
                return
            }
            if (msg.contains("answered Question #")) triviaOptions.forEach { it.isCorrect = false }
        }

        if (msg.trim().startsWithOneOf("ⓐ", "ⓑ", "ⓒ", ignoreCase = true) && triviaAnswers?.any { msg.endsWith(it) } == true) {
            when (msg.trim()[0]) {
                'ⓐ' -> triviaOptions[0].isCorrect = true
                'ⓑ' -> triviaOptions[1].isCorrect = true
                'ⓒ' -> triviaOptions[2].isCorrect = true
            }
        }

        triviaAnswers = when {
            msg.trim() == "What SkyBlock year is it?" -> listOf("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
            else -> answers.entries.find { msg.contains(it.key) }?.value ?: return
        }
    }

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Quiz") return

        triviaOptions[0].blockPos = getRealCoords(BlockPos(20.0, 70.0, 6.0))
        triviaOptions[1].blockPos = getRealCoords(BlockPos(15.0, 70.0, 9.0))
        triviaOptions[2].blockPos = getRealCoords(BlockPos(10.0, 70.0, 6.0))
    }

    fun onRenderWorld(quizColor: Color, quizDepth: Boolean) {
        if (triviaAnswers == null || triviaOptions.isEmpty()) return
        triviaOptions.forEach { answer ->
            if (!answer.isCorrect) return@forEach
            answer.blockPos?.add(0.0, -1.0, 0.0)?.let {
                Renderer.drawBlock(it, quizColor, depth = quizDepth)
                RenderUtils.drawBeaconBeam(it.toVec3(), quizColor, depth = quizDepth)
            }
        }
    }

    fun reset() {
        triviaOptions = MutableList(3) { TriviaAnswer(null, false) }
        triviaAnswers = null
    }
}
