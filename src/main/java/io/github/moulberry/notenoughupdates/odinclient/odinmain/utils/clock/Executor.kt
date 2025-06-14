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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.clock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.profile
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Class that allows repeating execution of code while being dynamic.
 * @author Stivais
 */
open class Executor(val delay: () -> Long, private val profileName: String = "Unspecified odin executor", val shouldRun: () -> Boolean = { true }, val func: Executable) {

    constructor(delay: Long, profileName: String = "Unspecified odin executor", shouldRun: () -> Boolean = { true }, func: Executable) : this({ delay }, profileName, shouldRun, func)

    internal val clock = Clock()
    internal var shouldFinish = false

    open fun run(): Boolean {
        if (shouldFinish) return true
            if (clock.hasTimePassed(delay(), true)) {
                profile(profileName) {
                    runCatching {
                        func()
                    }
                }
            }
        return false
    }

    /**
     * Starts an executor that ends after a certain number of times.
     */
    class LimitedExecutor(delay: Long, repeats: Int, profileName: String = "Unspecified odin executor", shouldRun: () -> Boolean = { true }, func: Executable) : Executor(delay, profileName, shouldRun, func) {
        private val repeats = repeats - 1
        private var totalRepeats = 0

        override fun run(): Boolean {
            if (shouldFinish) return true
            if (clock.hasTimePassed(delay(), true)) {
                runCatching {
                    if (totalRepeats >= repeats) destroyExecutor()
                    totalRepeats++
                    func()
                }
            }
            return false
        }
    }

    /**
     * Allows stopping executing an executor permanently
     *
     * Returning [Nothing] allows for us to stop running the function without specifying
     * @author Stivais
     */
    fun Executor.destroyExecutor(): Nothing {
        shouldFinish = true
        throw Throwable()
    }

    companion object {
        private val executors = ArrayList<Executor>()

        fun Executor.register() {
            executors.add(this)
        }

        @SubscribeEvent
        fun onRender(event: RenderWorldLastEvent) {
            profile("Executors") {
                executors.removeAll {
                    if (!it.shouldRun()) return@removeAll false
                    else it.run()
                }
            }
        }
    }
}

/**
 * Here for more readability
 */
typealias Executable = Executor.() -> Unit
