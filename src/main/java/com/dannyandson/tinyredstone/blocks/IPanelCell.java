package com.dannyandson.tinyredstone.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public interface IPanelCell {

    /**
     * Drawing the cell on the panel
     * @param matrixStack positioned for this cell
     *                    scaled to 1/8 block size such that length and width of cell are 1.0
     *                    starting point is (0,0,0)
     */
    void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, float alpha);

    /**
     * Called when neighboring redstone signal output changes.
     * This can be called multiple times in a tick.
     * Passes PanelCellNeighbor objects - an object wrapping another IPanelCell or a BlockState
     * WARNING! Check for null values!
     * @param frontNeighbor object to access info about front neighbor or NULL if no neighbor exists
     * @param rightNeighbor object to access info about right neighbor or NULL if no neighbor exists
     * @param backNeighbor object to access info about back neighbor or NULL if no neighbor exists
     * @param leftNeighbor object to access info about left neighbor or NULL if no neighbor exists
     * @return boolean indicating whether redstone output of this cell has changed
     */
    boolean neighborChanged(@Nullable PanelCellNeighbor frontNeighbor, @Nullable PanelCellNeighbor rightNeighbor, @Nullable  PanelCellNeighbor backNeighbor, @Nullable  PanelCellNeighbor leftNeighbor);

    /**
     * Gets redstone output of the given side of the cell
     * @param outputDirection (1=Front,2=Right,3=Back,4=Left)
     * @return integer 0-15 indicating the strength of redstone signal
     */
    int getWeakRsOutput(Side outputDirection);
    int getStrongRsOutput(Side outputDirection);

    /**
     * Does the power level drop when transmitting between these cells (such as with redstone dust)?
     * @return true if power level should drop, false if not
     */
    default boolean powerDrops(){return false;}

    /**
     * Is this a component that does not change state based on neighbors (such as a redstone block, or potentiometer)?
     * @return true if this cell's state is unaffected by neighbors
     */
    default boolean isIndependentState(){return false;}

    /**
     * Can this cell be pushed by a piston?
     * @return true if a piston can push this block
     */
    default boolean isPushable(){return false;}

    /**
     * If this cell outputs light, return the level here. Otherwise, return 0.
     * @return Light level to output 0-15
     */
    default int lightOutput(){return 0;}

    /**
     * Called each each tick.
     * @return boolean indicating whether redstone output of this cell has changed
     */
    default boolean tick(){return false;}

    /**
     * Called when the cell is activated. i.e. player right clicked on the cell of the panel tile.
     *
     * @param cellPos The position of the clicked IPanelCell within the panel (this IPanelCell)
     * @param segmentClicked Which of nine segment within the cell were clicked.
     * @return true if a change was made to the cell output
     */
    default boolean onBlockActivated(PanelCellPos cellPos, PanelCellSegment segmentClicked){return false;}

    default boolean hasActivation(){return false;}

    CompoundNBT writeNBT();

    void readNBT(CompoundNBT compoundNBT);

    /**
     * Get the shape of the cell for defining the hit box
     * @return PanelCellVoxelShape object defining the cell shape
     */
    default PanelCellVoxelShape getShape() {return PanelCellVoxelShape.FULLCELL;}

}
