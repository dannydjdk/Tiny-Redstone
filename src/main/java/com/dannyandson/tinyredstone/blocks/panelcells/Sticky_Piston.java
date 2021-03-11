package com.dannyandson.tinyredstone.blocks.panelcells;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class Sticky_Piston extends Piston {

    public static ResourceLocation TEXTURE_PISTON_TOP = new ResourceLocation("minecraft","block/piston_top_sticky");

    protected TextureAtlasSprite sprite_top = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_PISTON_TOP);

    @Override
    protected TextureAtlasSprite getSprite_top()
    {
        return this.sprite_top;
    }

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


}
