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

import io.github.moulberry.notenoughupdates.odinclient.odinmain.events.impl.RenderEntityModelEvent;
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.Color;
import io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.render.HighlightRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

import static io.github.moulberry.notenoughupdates.odinclient.odinmain.utils.Utils.postAndCatch;
import static org.lwjgl.opengl.GL11.*;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Shadow
    protected ModelBase mainModel;

    @Shadow
    protected FloatBuffer brightnessBuffer;

    @Final
    @Shadow
    private static DynamicTexture textureBrightness;

    @Unique
    private HighlightRenderer.HighlightEntity odinMod$getHighlightEntity(EntityLivingBase entity) {
        return HighlightRenderer.INSTANCE.getEntities().get(HighlightRenderer.HighlightType.Overlay).stream().filter(e -> e.getEntity().equals(entity)).findFirst().orElse(null);
    }

    @Inject(method = "setBrightness", at = @At(value = "HEAD"), cancellable = true)
    private  <T extends EntityLivingBase> void setBrightness(T entity, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {
        HighlightRenderer.HighlightEntity highlightEntity = odinMod$getHighlightEntity(entity);
        if (highlightEntity != null) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
            this.brightnessBuffer.position(0);
            Color color = highlightEntity.getColor();
            brightnessBuffer.put(color.getRed() / 255f);
            brightnessBuffer.put(color.getGreen() / 255f);
            brightnessBuffer.put(color.getBlue() / 255f);
            brightnessBuffer.put(color.getAlpha() / 255f);
            this.brightnessBuffer.flip();
            GL11.glTexEnv(8960, 8705, this.brightnessBuffer);
            GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(textureBrightness.getGlTextureId());
            GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"))
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        HighlightRenderer.HighlightEntity highlightEntity = odinMod$getHighlightEntity(entity);
        if (highlightEntity != null && !highlightEntity.getDepth()) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1f, -1000000F);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        HighlightRenderer.HighlightEntity highlightEntity = odinMod$getHighlightEntity(entity);
        if (highlightEntity != null && !highlightEntity.getDepth()) {
            glPolygonOffset(1f, 1000000F);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }

    @Inject(method = "renderLayers", at = @At("RETURN"), cancellable = true)
    private void onRenderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_, CallbackInfo ci) {
        if (postAndCatch(new RenderEntityModelEvent(
                entitylivingbaseIn, p_177093_2_, p_177093_3_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_, mainModel
        ))) {
            ci.cancel();
        }
    }
}

