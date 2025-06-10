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

package io.github.moulberry.notenoughupdates.odinclient.mixin.mixins;

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.GuiEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.Utils.postAndCatch;

@Mixin(value = GuiContainer.class, priority = 1)
public abstract class MixinGuiContainer {

    @Unique
    private final GuiContainer odinMod$gui = (GuiContainer) (Object) this;

    @Shadow
    public Container inventorySlots;

    @Shadow protected int xSize;

    @Shadow protected int ySize;

    @Shadow protected int guiLeft;

    @Shadow protected int guiTop;

    @Shadow private Slot theSlot;

    @Shadow protected abstract boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY);

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slotIn, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawSlot(odinMod$gui, slotIn, slotIn.xDisplayPosition, slotIn.yDisplayPosition))) ci.cancel();
    }

    @Inject(method = "drawScreen", at = @At(value = "HEAD"), cancellable = true)
    private void startDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawGuiBackground(odinMod$gui, this.xSize, this.ySize, guiLeft, guiTop))) {
            ci.cancel();

            this.theSlot = null;
            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
                Slot slot = this.inventorySlots.inventorySlots.get(i);
                if (!this.isMouseOverSlot(slot, mouseX, mouseY) || !slot.canBeHovered()) continue;
                this.theSlot = slot;
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"), cancellable = true)
    private void onEndDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawGuiForeground(odinMod$gui, this.xSize, this.ySize, guiLeft, guiTop, mouseX, mouseY))) ci.cancel();
    }
}
