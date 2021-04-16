package com.dannyandson.tinyredstone.blocks.panelcells;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.gui.RepeaterCellGUI;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SuperRepeater extends Repeater {

    public static ResourceLocation TEXTURE_SUPER_REPEATER_ON = new ResourceLocation(TinyRedstone.MODID,"block/panel_super_repeater_on");
    public static ResourceLocation TEXTURE_SUPER_REPEATER_OFF = new ResourceLocation(TinyRedstone.MODID,"block/panel_super_repeater_off");

    @Override
    public boolean neighborChanged(@Nullable PanelCellNeighbor frontNeighbor,@Nullable PanelCellNeighbor rightNeighbor,@Nullable  PanelCellNeighbor backNeighbor,@Nullable  PanelCellNeighbor leftNeighbor)
    {
        boolean changed=false;
        if (backNeighbor!=null && backNeighbor.getWeakRsOutput() >0 && !input)
        {
            input=true;
        }
        else if ((backNeighbor==null || backNeighbor.getWeakRsOutput() ==0 ) && input)
        {
            input=false;
        }

        this.locked= (leftNeighbor != null && leftNeighbor.getStrongRsOutput() > 0 &&
                (leftNeighbor.getNeighborIPanelCell() instanceof Repeater || leftNeighbor.getNeighborIPanelCell() instanceof Comparator ||
                        (leftNeighbor.getNeighborBlockState() != null && (leftNeighbor.getNeighborBlockState().getBlock() == Blocks.REPEATER || leftNeighbor.getNeighborBlockState().getBlock() == Blocks.COMPARATOR))
                ))
                ||
                (rightNeighbor != null && rightNeighbor.getStrongRsOutput() > 0 &&
                        (rightNeighbor.getNeighborIPanelCell() instanceof Repeater || rightNeighbor.getNeighborIPanelCell() instanceof Comparator ||
                                (rightNeighbor.getNeighborBlockState() != null && (rightNeighbor.getNeighborBlockState().getBlock() == Blocks.REPEATER || rightNeighbor.getNeighborBlockState().getBlock() == Blocks.COMPARATOR))
                        ));

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
            return RenderHelper.getSprite(TEXTURE_SUPER_REPEATER_ON);
        return RenderHelper.getSprite(TEXTURE_SUPER_REPEATER_OFF);

    }

}
