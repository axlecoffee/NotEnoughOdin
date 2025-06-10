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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.dungeon

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.dungeon.DungeonUtils.WITHER_ESSENCE_ID
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import java.util.*

/**
 * @see io.github.moulberry.notenoughupdates.odinclient.odinclient.mixin.mixins.block
 */
object SecretHitboxes : Module(
    name = "Secret Hitboxes",
    desc = "Extends the hitboxes of secret blocks to a full block."
) {
    val lever by BooleanSetting("Lever", false, desc = "Extends the lever hitbox.")
    val button by BooleanSetting("Button", false, desc = "Extends the button hitbox.")
    val essence by BooleanSetting("Essence", false, desc = "Extends the essence hitbox.")
    val chests by BooleanSetting("Chests", false, desc = "Extends the chest hitbox.")

    private val mostSignificantBits = UUID.fromString(WITHER_ESSENCE_ID).mostSignificantBits

    fun isEssence(blockPos: BlockPos): Boolean {
        return essence && (Minecraft.getMinecraft().theWorld?.getTileEntity(blockPos) as? TileEntitySkull)?.playerProfile?.id?.mostSignificantBits == mostSignificantBits
    }
}
