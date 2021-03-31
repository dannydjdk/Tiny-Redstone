package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class PanelTileRenderer extends TileEntityRenderer<PanelTile> {

    public static ResourceLocation TEXTURE = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel");
    public static ResourceLocation TEXTURE_CRASHED = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_crashed");

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

        matrixStack.push();

        switch (tileEntity.getBlockState().get(BlockStateProperties.FACING))
        {
            case UP:
                matrixStack.rotate(Vector3f.XP.rotationDegrees(180));
                matrixStack.translate(0,-1,-1);
                break;
            case NORTH:
                matrixStack.rotate(Vector3f.XP.rotationDegrees(90));
                matrixStack.translate(0,0,-1);
                break;
            case EAST:
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
                matrixStack.translate(0,-1,0);
                break;
            case SOUTH:
                matrixStack.rotate(Vector3f.XP.rotationDegrees(-90));
                matrixStack.translate(0,-1,0);
                break;
            case WEST:
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(-90));
                matrixStack.translate(-1,0,0);
                break;
        }
        if (tileEntity.isCovered())
        {
            matrixStack.push();
            tileEntity.panelCover.render(matrixStack,buffer,combinedLight,combinedOverlay, tileEntity.getColor());
            matrixStack.pop();
        }
        else {
            for (Integer i = 0; i < 64; i++) {
                PanelCellPos pos = PanelCellPos.fromIndex(tileEntity,i);
                IPanelCell panelCell = pos.getIPanelCell();
                if (panelCell!=null) {
                    Side cellDirection = pos.getCellFacing();

                    renderCell(matrixStack, i, panelCell, cellDirection, buffer, (tileEntity.isCrashed()) ? 0 : combinedLight, combinedOverlay, (tileEntity.isCrashed()) ? 0.5f : 1.0f);
                }
            }

            if (tileEntity.lookingAtCell != null) {
                renderCell(matrixStack, tileEntity.lookingAtCell, tileEntity.lookingAtWith, tileEntity.lookingAtDirection, buffer, combinedLight, combinedOverlay, 0.5f);
            }
        }

        if (tileEntity.isCrashed())
        {
            matrixStack.push();
            matrixStack.translate(0, 0.126, 1);
            matrixStack.rotate(Vector3f.XP.rotationDegrees(rotation1));

            TextureAtlasSprite sprite = RenderHelper.getSprite(TEXTURE_CRASHED);
            RenderHelper.drawRectangle(buffer.getBuffer(RenderType.getTranslucent()),matrixStack,0,1,0,1,sprite,combinedLight,0.9f);
            matrixStack.pop();
        }

        matrixStack.pop();

    }

    private void renderCell(MatrixStack matrixStack, Integer index, IPanelCell panelCell, Side cellDirection, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay,float alpha)
    {
        int row = Math.round((index.floatValue()/8f)-0.5f);
        int cell = index%8;

        matrixStack.push();

        matrixStack.translate(cellSize*(double)row, 0.125, cellSize*(cell));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(rotation1));

        if (cellDirection== Side.LEFT)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));
        }
        else if (cellDirection== Side.BACK)
        {
            matrixStack.translate(cellSize,-cellSize,0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        }
        else if (cellDirection== Side.RIGHT)
        {
            matrixStack.translate(cellSize,0,0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(270));
        }

        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(t2X,t2Y,t2Z);

        panelCell.render(matrixStack, buffer, combinedLight, combinedOverlay,alpha);

        matrixStack.pop();

    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }


}
