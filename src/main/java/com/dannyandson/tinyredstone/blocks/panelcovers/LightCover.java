package com.dannyandson.tinyredstone.blocks.panelcovers;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.resources.ResourceLocation;

public class LightCover  extends DarkCover {

    public static ResourceLocation TEXTURE_LIGHT_COVER = new ResourceLocation(TinyRedstone.MODID,"block/light_cover");


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
