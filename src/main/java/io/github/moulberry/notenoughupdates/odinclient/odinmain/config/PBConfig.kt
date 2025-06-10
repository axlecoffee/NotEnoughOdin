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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.logger
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.scope
import java.io.File

object PBConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    var pbs: MutableMap<String, MutableList<Double>> = mutableMapOf()
    private val configFile = File(Minecraft.getMinecraft().mcDataDir, "config/odin/personal-bests.json").apply {
        try {
            createNewFile()
        } catch (_: Exception) {
            println("Error creating personal bests config file.")
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (isEmpty()) return

                pbs = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<Double>>>() {}.type
                )
                println("Successfully loaded pb config $pbs")
            }
        } catch (e: Exception) {
            println("Odin: Error parsing pbs.")
            println(e.message)
            logger.error("Error parsing pbs.", e)
        }
    }

    fun saveConfig() {
        scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(pbs))
                }
            } catch (_: Exception) {
                println("Odin: Error saving PB config.")
            }
        }
    }
}
