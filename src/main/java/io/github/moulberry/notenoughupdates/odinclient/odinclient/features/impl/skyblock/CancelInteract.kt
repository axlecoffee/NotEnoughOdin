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

package io.github.moulberry.notenoughupdates.odinclient.odinclient.features.impl.skyblock

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.Module
import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.settings.impl.BooleanSetting
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.containsOneOf
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.hasAbility
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.isHolding
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.skyblock.lore
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos

/**
 * Cancels block interactions to allow for items to be used.
 *
 * Modified from: https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/module/impl/misc/CancelInteract.kt
 *
 * @author Aton
 */
object CancelInteract : Module(
    name = "Cancel Interact",
    desc = "Cancels the interaction with certain blocks, so that the item can be used instead."
){
    private val cancelInteract by BooleanSetting("Cancel Interact", true, desc = "Cancels the interaction with certain blocks, so that the item can be used instead.")
    private val onlyWithAbility by BooleanSetting("Only Ability", false, desc = "Check whether the item has an ability before cancelling interactions.")
    private val noBreakReset by BooleanSetting("No Break Reset", false, desc = "Prevents lore updates from resetting your breaking progress.")

    /**
     * Block which should always be interacted with.
     */
    private val interactionWhitelist = setOf<Block>(
        Blocks.lever, Blocks.chest, Blocks.trapped_chest,
        Blocks.stone_button, Blocks.wooden_button
    )

    /**
     * Set containing all the block which interactions should be canceled with.
     */
    private val interactionBlacklist = setOf<Block>(
        Blocks.cobblestone_wall, Blocks.oak_fence, Blocks.dark_oak_fence,
        Blocks.acacia_fence, Blocks.birch_fence, Blocks.jungle_fence,
        Blocks.nether_brick_fence, Blocks.spruce_fence, Blocks.birch_fence_gate,
        Blocks.acacia_fence_gate, Blocks.dark_oak_fence_gate, Blocks.oak_fence_gate,
        Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.hopper,
    )

    /**
     * Redirected to by the MinecraftMixin. Replaces the check for whether the targeted block is air.
     * @return true when the item's ability should be used.
     */
    @JvmStatic
    fun cancelInteractHook(instance: WorldClient, blockPos: BlockPos): Boolean {
        // When the module is not enabled preform the vanilla action.
        if (cancelInteract && enabled) {
            if (interactionWhitelist.contains(instance.getBlockState(blockPos).block)) return false
            if (isHolding("ENDER_PEARL")) return true
            if (!onlyWithAbility || Minecraft.getMinecraft().thePlayer?.heldItem?.hasAbility == true)
                return interactionBlacklist.contains(instance.getBlockState(blockPos).block) || instance.isAirBlock(blockPos)
        }
        return instance.isAirBlock(blockPos)
    }

    /**
     * @see io.github.moulberry.notenoughupdates.odinclient.mixin.mixins.MixinPlayerControllerMP
     */
    @JvmStatic
    fun isHittingPositionHook(blockPos: BlockPos, currentItemHittingBlock: ItemStack?, currentBlock: BlockPos): Boolean {
        val itemStack: ItemStack? = Minecraft.getMinecraft().thePlayer?.heldItem
        var flag = currentItemHittingBlock == null && itemStack == null
        if (currentItemHittingBlock != null && itemStack != null) {
            if (noBreakReset && enabled && itemStack.tagCompound != null)
                if (itemStack.lore.toString().containsOneOf("GAUNTLET", "DRILL", "PICKAXE"))
                    return blockPos == currentBlock && itemStack.item === currentItemHittingBlock.item

            flag = itemStack.item === currentItemHittingBlock.item && ItemStack.areItemStackTagsEqual(itemStack, currentItemHittingBlock)
                    && (itemStack.isItemStackDamageable || itemStack.metadata == currentItemHittingBlock.metadata)
        }
        return blockPos == currentBlock && flag
    }
}
