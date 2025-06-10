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

package io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3

import com.github.stivais.commodore.parsers.CommandParsable
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termGUI.*
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.floor7.p3.termsim.*

@CommandParsable
enum class TerminalTypes(
    val windowName: String,
    val windowSize: Int,
    val gui: TermGui?
) : Type {
    PANES("Correct all the panes!", 45, PanesGui) {
        override fun getSimulator() = PanesSim
    },
    RUBIX("Change all to same color!", 45, RubixGui) {
        override fun getSimulator() = RubixSim
    },
    NUMBERS("Click in order!", 36, NumbersGui) {
        override fun getSimulator() = NumbersSim
    },
    STARTS_WITH("What starts with:", 45, StartsWithGui) {
        override fun getSimulator() = StartsWithSim()
    },
    SELECT("Select all the", 54, SelectAllGui) {
        override fun getSimulator() = SelectAllSim()
    },
    MELODY("Click the button on time!", 54, MelodyGui) {
        override fun getSimulator() = MelodySim
    },
}

private interface Type {
    fun getSimulator(): TermSimGUI
}
