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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.PostEntityMetadata;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.Utils.postAndCatch;

@Mixin({S1CPacketEntityMetadata.class})
public class MixinS1CPacketEntityMetadata {

    @Redirect(method = {"processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/INetHandlerPlayClient;handleEntityMetadata(Lnet/minecraft/network/play/server/S1CPacketEntityMetadata;)V"))
    private void redirectProcessPacket(INetHandlerPlayClient instance, S1CPacketEntityMetadata packet) {
        instance.handleEntityMetadata(packet);
        postAndCatch(new PostEntityMetadata(packet));
    }
}
