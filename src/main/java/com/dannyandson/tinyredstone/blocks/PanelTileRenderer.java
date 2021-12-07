package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.panelcells.GhostRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import javax.annotation.CheckForNull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PanelTileRenderer extends TileEntityRenderer<PanelTile> {

    public static ResourceLocation TEXTURE = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel");
    public static ResourceLocation TEXTURE_CRASHED = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_crashed");

    public PanelTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    private static final float scale = 0.125f;
    private static final float t2X = 0.0f;
    private static final float t2Y = -1.0f;
    private static final float t2Z = 0.0f;
    private static final float rotation1 = 270f;


    private double cellSize = 1d/8d;


    @Override
    public void render(PanelTile tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        Boolean hasBase = tileEntity.hasBase();
        matrixStack.pushPose();

        switch (tileEntity.getBlockState().getValue(BlockStateProperties.FACING))
        {
            case UP:
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
                matrixStack.translate(0,-1,-1);
                break;
            case NORTH:
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
                matrixStack.translate(0,0,-1);
                break;
            case EAST:
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
                matrixStack.translate(0,-1,0);
                break;
            case SOUTH:
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
                matrixStack.translate(0,-1,0);
                break;
            case WEST:
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90));
                matrixStack.translate(-1,0,0);
                break;
        }
        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        IVertexBuilder builder = buffer.getBuffer(RenderType.solid());
        if (tileEntity.isCovered())
        {
            matrixStack.pushPose();
            tileEntity.panelCover.render(matrixStack,buffer,combinedLight,combinedOverlay, tileEntity.getColor());
            matrixStack.popPose();
        }
        else {
            if (hasBase) {
                int color = tileEntity.getColor();
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(270));
                matrixStack.translate(0, -1, 0.125);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
                matrixStack.translate(0, -0.125, 0);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0, 0, 1);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0, 0, 1);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.translate(0, 0, 1);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
                matrixStack.translate(0, -1, 0);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, sprite, combinedLight, color, 1.0f);

                matrixStack.popPose();
            }

            List<PanelCellPos> positions = tileEntity.getCellPositions();
            for (PanelCellPos pos : positions) {
                IPanelCell panelCell = pos.getIPanelCell();
                if (panelCell != null) {
                    renderCell(matrixStack, pos, buffer, (tileEntity.isCrashed()) ? 0 : combinedLight, combinedOverlay, (tileEntity.isCrashed()) ? 0.5f : 1.0f,hasBase);
                }
            }

            if (tileEntity.panelCellGhostPos != null) {
                renderCell(matrixStack, tileEntity.panelCellGhostPos, buffer, combinedLight, combinedOverlay, 0.5f,hasBase);
            }
        }

        if (tileEntity.isCrashed() || tileEntity.isOverflown())
        {
            matrixStack.pushPose();
            matrixStack.translate(0, 0.126, 1);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotation1));

            sprite = RenderHelper.getSprite(TEXTURE_CRASHED);
            RenderHelper.drawRectangle(buffer.getBuffer((Minecraft.useShaderTransparency())?RenderType.solid():RenderType.translucent()),matrixStack,0,1,0,1,sprite,combinedLight,0.9f);
            matrixStack.popPose();
        }

        matrixStack.popPose();

    }

    private void renderCell(MatrixStack matrixStack, PanelCellPos pos, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay,float alpha,boolean hasBase)
    {
        alpha = (Minecraft.useShaderTransparency())?1.0f:alpha;

        matrixStack.pushPose();

        matrixStack.translate(cellSize*(double)pos.getRow(), ((hasBase)?0.125:0)+(pos.getLevel()*0.125), cellSize*(pos.getColumn()));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotation1));

        Side facing = pos.getCellFacing();

        if (facing == Side.LEFT)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
        }
        else if (facing == Side.BACK)
        {
            matrixStack.translate(cellSize,-cellSize,0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        }
        else if (facing == Side.RIGHT)
        {
            matrixStack.translate(cellSize,0,0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(270));
        }
        else if (pos.getCellFacing()==Side.BOTTOM)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        }
        else if (pos.getCellFacing()==Side.TOP)
        {
            matrixStack.translate(0,0,cellSize);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        }

        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(t2X,t2Y,t2Z);

        pos.getIPanelCell().render(matrixStack, buffer, combinedLight, combinedOverlay,alpha);

        matrixStack.popPose();

    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }

    @CheckForNull
    public static PanelCellGhostPos getPlayerLookingAtCell(PanelTile panelTile) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        BlockPos blockPos = panelTile.getBlockPos();
        if (player != null) {
            if (panelTile.getBlockPos().closerThan(player.position(), 4.0d)) {
                BlockRayTraceResult blockHitResult = panelTile.getPlayerCollisionHitResult(player);
                PanelCellPos cellPos1 = PosInPanelCell.fromHitVec(panelTile, panelTile.getBlockPos(), blockHitResult);
                panelTile.panelCellHovering = cellPos1;

                if (panelTile.getBlockPos().closerThan(player.position(), 3.0d)) {

                    if (PanelBlock.isPanelCellItem(player.getMainHandItem().getItem())) {
                        if (cellPos1 != null) {
                            PanelCellPos cellPos = cellPos1;
                            if (cellPos.getIPanelCell() != null && (!cellPos.getIPanelCell().hasActivation(player) || player.isCrouching())) {
                                cellPos = cellPos.offset(panelTile.getSideFromDirection(blockHitResult.getDirection()));
                            }
                            if (cellPos != null && cellPos.getIPanelCell() == null) {
                                try {
                                    IPanelCell panelCell = (IPanelCell) PanelBlock.getPanelCellClassFromItem(player.getMainHandItem().getItem()).getConstructors()[0].newInstance();
                                    Side rotationLock = RotationLock.getRotationLock();
                                    Side cellFacing = rotationLock == null ?
                                            panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(player, panelCell.canPlaceVertical()))
                                            : rotationLock;

                                    if (panelCell.needsSolidBase()) {
                                        Side attachingSideDir = panelTile.getSideFromDirection(blockHitResult.getDirection()).getOpposite();
                                        Side attachingSideRel = (attachingSideDir == Side.TOP || attachingSideDir == Side.BOTTOM) ? attachingSideDir : Side.FRONT;

                                        if (
                                            //check if the cell can attach to the side of the block facing
                                                !panelCell.canAttachToBaseOnSide(attachingSideRel) || (
                                                        //if so, check if it's being placed against a full block
                                                        !cellPos1.equals(cellPos) && (
                                                                cellPos1.getIPanelCell() == null
                                                                        || !cellPos1.getIPanelCell().isPushable()
                                                        )
                                                )
                                        ) {
                                            return null;
                                        } else {
                                            panelCell.setBaseSide(attachingSideRel);
                                            if (attachingSideRel == Side.FRONT)
                                                cellFacing = attachingSideDir;
                                        }
                                    }

                                    panelCell.onPlace(cellPos, player);
                                    return PanelCellGhostPos.fromPosInPanelCell(
                                            cellPos,
                                            panelCell,
                                            cellFacing
                                    );
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                    TinyRedstone.LOGGER.error("Exception thrown when attempting to draw ghost cell: " + e.getMessage());
                                }
                            }
                        }
                    }

                    //if we are not rendering a ghost component, check if we are hovering over a tiny redstone dust
                    PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, blockPos, blockHitResult);
                    if (posInPanelCell != null && posInPanelCell.getIPanelCell() instanceof RedstoneDust) {
                        PanelCellSegment segmentHovering = posInPanelCell.getSegment();
                        return PanelCellGhostPos.fromPosInPanelCell(
                                posInPanelCell,
                                new GhostRenderer(segmentHovering),
                                posInPanelCell.getCellFacing()
                        );

                    }
                }
            }
        }
        return null;
    }

}
