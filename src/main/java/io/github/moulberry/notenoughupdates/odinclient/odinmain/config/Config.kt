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

import com.google.gson.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.OdinMain.logger
import net.minecraft.client.Minecraft
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.ModuleManager
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.Saving
import java.io.File

/**
 * This class handles loading and saving Modules and their settings.
 *
 * @author Stivais
 */
object Config {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val parser = JsonParser()

    private val configFile = File(Minecraft.getMinecraft().mcDataDir, "config/odin/odin-config.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config\n${e.message}")
            logger.error("Error initializing module config", e)
        }
    }

    fun load() {
        try {
            with (configFile.bufferedReader().use { it.readText() }) {
                if (isEmpty()) return

                val jsonArray = parser.parse(this).asJsonArray ?: return
                for (modules in jsonArray) {
                    val moduleObj = modules?.asJsonObject ?: continue
                    val module = ModuleManager.getModuleByName(moduleObj.get("name").asString) ?: continue
                    if (moduleObj.get("enabled").asBoolean != module.enabled) module.toggle()

                    for (j in moduleObj.get("settings").asJsonArray) {
                        val settingObj = j?.asJsonObject?.entrySet() ?: continue
                        val setting = module.getSettingByName(settingObj.firstOrNull()?.key) ?: continue
                        if (setting is Saving) setting.read(settingObj.first().value)
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading config.\n${e.message}")
            logger.error("Error initializing module config", e)
        }
    }

    fun save() {
        try {
            // reason doing this is better is that
            // using like a custom serializer leaves 'null' in settings that don't save
            // code looks hideous tho, but it fully works
            val jsonArray = JsonArray().apply {
                for (module in ModuleManager.modules) {
                    add(JsonObject().apply {
                        add("name", JsonPrimitive(module.name))
                        add("enabled", JsonPrimitive(module.enabled))
                        add("settings", JsonArray().apply {
                            for (setting in module.settings) {
                                if (setting is Saving) {
                                    add(JsonObject().apply { add(setting.name, setting.write()) })
                                }
                            }
                        })
                    })
                }
            }
            configFile.bufferedWriter().use { it.write(gson.toJson(jsonArray)) }
        } catch (e: Exception) {
            println("Error saving config.\n${e.message}")
            logger.error("Error saving config.", e)
        }
    }
}
