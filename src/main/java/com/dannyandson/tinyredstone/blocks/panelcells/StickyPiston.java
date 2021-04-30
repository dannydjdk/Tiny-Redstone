package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class StickyPiston extends Piston {

    public static ResourceLocation TEXTURE_PISTON_TOP = new ResourceLocation("minecraft","block/piston_top_sticky");

   /**
     * Called each each tick.
     *
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean tick() {
        if (changePending < 0)
            return false;
        if (changePending > 0) {
            changePending--;
            return false;
        }
        changePending--;
        return true;
    }

    @Override
    protected TextureAtlasSprite getSprite_top()
    {
        return RenderHelper.getSprite(TEXTURE_PISTON_TOP);
    }


}
