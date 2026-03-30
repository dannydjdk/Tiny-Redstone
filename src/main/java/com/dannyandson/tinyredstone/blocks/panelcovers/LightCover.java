package com.dannyandson.tinyredstone.blocks.panelcovers;

import com.dannyandson.tinyredstone.TinyRedstone;
import net.minecraft.resources.Identifier;

public class LightCover  extends DarkCover {

    public static Identifier TEXTURE_LIGHT_COVER = Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/light_cover");


    protected Identifier getDefaultResourceLocation() {
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
