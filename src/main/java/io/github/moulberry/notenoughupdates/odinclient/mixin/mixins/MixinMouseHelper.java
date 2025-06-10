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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.features.impl.skyblock.NoCursorReset;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHelper.class, priority = 999)
public class MixinMouseHelper {
    // This mixin is used to prevent the mouse from being reset when the player opens a gui
    @Inject(method = "ungrabMouseCursor", at = @At("HEAD"), cancellable = true)
    private void ungrabMouseCursor(CallbackInfo ci) {
        if (NoCursorReset.shouldHookMouse()) {
            ci.cancel();
            Mouse.setGrabbed(false);
        }
    }
}
