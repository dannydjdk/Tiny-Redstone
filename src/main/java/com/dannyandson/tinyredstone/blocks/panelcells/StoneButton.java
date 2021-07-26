package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelCellSegment;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class StoneButton extends Button {
    public static ResourceLocation TEXTURE_OAK_PLANKS = new ResourceLocation("minecraft","block/stone");

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @param player player who activated (right-clicked) the cell
     * @return true if a change was made to the cell output
     */
    @Override
    public boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked, Player player){
        if (!active)
        {
            PanelTile panelTile = cellPos.getPanelTile();
            panelTile.getLevel().playLocalSound(
                    panelTile.getBlockPos().getX(), panelTile.getBlockPos().getY(), panelTile.getBlockPos().getZ(),
                    SoundEvents.STONE_BUTTON_CLICK_ON,
                    SoundCategory.BLOCKS, 0.25f, 2f, false
            );
            this.active=true;
            this.ticksRemaining =20;
            return true;
        }
        return false;
    }

    protected TextureAtlasSprite getSprite()
    {
        return RenderHelper.getSprite(TEXTURE_OAK_PLANKS);
    }


}
