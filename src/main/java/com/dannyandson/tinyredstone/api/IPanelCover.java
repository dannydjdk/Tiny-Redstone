package com.dannyandson.tinyredstone.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IPanelCover {

    /**
     * Drawing the cover on the panel
     */
    void render(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, int color);

    /**
     * Does this cover allows light output?
     * @return true if cells can output light, false if not.
     */
    boolean allowsLightOutput();


}
