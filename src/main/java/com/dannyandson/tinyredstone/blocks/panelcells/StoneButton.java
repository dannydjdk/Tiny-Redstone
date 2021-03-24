package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.PanelCellSegment;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class StoneButton extends Button{
    public static ResourceLocation TEXTURE_OAK_PLANKS = new ResourceLocation("minecraft","block/stone");

    @Override
    public boolean onBlockActivated(PanelTile panelTile, Integer cellIndex, PanelCellSegment segmentClicked) {
        if (!active)
        {
            this.active=true;
            this.ticksRemaining =20;
            return true;
        }
        return false;
    }

    protected TextureAtlasSprite getSprite()
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_OAK_PLANKS);
    }


}
