package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.gui.RepeaterCellGUI;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SuperRepeater extends Repeater {

    public static ResourceLocation TEXTURE_SUPER_REPEATER_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_super_repeater_on");
    public static ResourceLocation TEXTURE_SUPER_REPEATER_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_super_repeater_off");

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellPos object for this cell which can be used to query PanelTile for PanelCellNeighbor objects - objects wrapping another IPanelCell or a BlockState
     * @param cellPos PanelCellPos object for this cell. Can be used to query paneltile about neighbors
     * @return boolean indicating whether redstone output of this cell has changed
     */
    @Override
    public boolean neighborChanged(PanelCellPos cellPos){

        boolean changed = super.neighborChanged(cellPos);

        if (this.ticks==0 && ! this.locked && this.input!=this.output)
        {
            this.output=this.input;
            changed=true;
        }

        return changed;
    }

        /**
         * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
         *
         * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
         * @param segmentClicked Which of nine segment within the cell were clicked.
         * @param player player who activated (right-clicked) the cell
         * @return true if a change was made to the cell output
         */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player) {
        PanelTile panelTile = cellPos.getPanelTile();
        if (panelTile.getLevel().isClientSide)
            RepeaterCellGUI.open(panelTile, cellPos.getIndex(), this);
        return false;
    }

    @Override
    protected TextureAtlasSprite getRepeaterTexture()
    {
        if (this.output)
            return RenderHelper.getSprite(TEXTURE_SUPER_REPEATER_ON);
        return RenderHelper.getSprite(TEXTURE_SUPER_REPEATER_OFF);

    }

}
