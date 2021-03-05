package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class PanelTileRenderer extends TileEntityRenderer<PanelTile> {

    public static ResourceLocation TEXTURE = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel");

    public PanelTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    private float scale = 0.125f;
    private float t2X = 0.0f;
    private float t2Y = -1.0f;
    private float t2Z = 0.0f;
    private float rotation1 = 270f;


    private double cellSize = 1d/8d;


    @Override
    public void render(PanelTile tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

         for(Integer i=0 ; i<64 ; i++)
        {
            if (tileEntity.cells.containsKey(i))
            {
                IPanelCell panelCell = tileEntity.cells.get(i);
                Direction cellDirection = tileEntity.cellDirections.get(i);
                int row = Math.round((i.floatValue()/8f)-0.5f);
                int cell = i%8;

                matrixStack.push();

                    matrixStack.translate(cellSize*(double)row, 0.125, cellSize*(cell));
                matrixStack.rotate(Vector3f.XP.rotationDegrees(rotation1));

                if (cellDirection== Direction.WEST)
                {
                    matrixStack.translate(0,-cellSize,0);
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
                }
                else if (cellDirection== Direction.SOUTH)
                {
                    matrixStack.translate(cellSize,-cellSize,0);
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
                }
                else if (cellDirection== Direction.EAST)
                {
                    matrixStack.translate(cellSize,0,0);
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(270));
                }

                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(t2X,t2Y,t2Z);

                panelCell.render(matrixStack, buffer, combinedLight, combinedOverlay);

                matrixStack.pop();

            }
        }


    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }


}
