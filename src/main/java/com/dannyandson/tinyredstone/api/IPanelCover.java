package com.dannyandson.tinyredstone.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public interface IPanelCover {

    /**
     * Drawing the cover on the panel
     */
    void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, int color);

    /**
     * Does this cover allows light output?
     * @return true if cells can output light, false if not.
     */
    boolean allowsLightOutput();


}
