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
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.Utils.postAndCatch;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Unique
    private final GuiScreen odin$gui = (GuiScreen) (Object) this;

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseReleased(III)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectMouseRelease(CallbackInfo ci, int mouseX, int mouseY, int mouseButton) {
        if (postAndCatch(new GuiEvent.MouseRelease(odin$gui, mouseButton, mouseX, mouseY))) ci.cancel();
    }

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseClicked(III)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectMouseClick(CallbackInfo ci, int mouseX, int mouseY, int mouseButton) {
        if (postAndCatch(new GuiEvent.MouseClick(odin$gui, mouseButton, mouseX, mouseY))) ci.cancel();
    }

    @Inject(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;keyTyped(CI)V"), cancellable = true)
    private void injectKeyboardClick(CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.KeyPress(odin$gui, Keyboard.getEventKey(), Keyboard.getEventCharacter()))) ci.cancel();
    }
}

