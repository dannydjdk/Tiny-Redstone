package com.dannyandson.tinyredstone.blocks.panelcovers;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCover;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class LightCover  extends DarkCover {

    public static ResourceLocation TEXTURE_LIGHT_COVER = new ResourceLocation(TinyRedstone.MODID,"block/light_cover");

    @Override
    protected ResourceLocation getDefaultResourceLocation() {
        return TEXTURE_LIGHT_COVER;
    }

    /**
     * Does this cover allows light output?
     *
     * @return true if cells can output light, false if not.
     */
    @Override
    public boolean allowsLightOutput() {
        return true;
    }

}
