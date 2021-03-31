package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelCellSegment;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.gui.RepeaterCellGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class SuperRepeater extends Repeater {

    public static ResourceLocation TEXTURE_SUPER_REPEATER_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_super_repeater_on");
    public static ResourceLocation TEXTURE_SUPER_REPEATER_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_super_repeater_off");

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked) {
        PanelTile panelTile = cellPos.getPanelTile();
        if (panelTile.getWorld().isRemote)
            RepeaterCellGUI.open(panelTile, cellPos.getIndex(), this);
        return false;
    }

    @Override
    protected TextureAtlasSprite getRepeaterTexture()
    {
        if (this.output)
            return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_SUPER_REPEATER_ON);
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE_SUPER_REPEATER_OFF);

    }

}
