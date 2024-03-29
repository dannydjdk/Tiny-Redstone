package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.panelcells.GhostRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.CheckForNull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PanelTileRenderer implements BlockEntityRenderer<PanelTile> {

    public static ResourceLocation[] TEXTURES = {
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0001"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0010"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0011"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0100"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0101"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0110"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_0111"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1000"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1001"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1010"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1011"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1100"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1101"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1110"),
            new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_1111")
    };
    public static ResourceLocation TEXTURE = TEXTURES[0];
    public static ResourceLocation TEXTURE_BORDER = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_border");
    public static ResourceLocation TEXTURE_CRASHED = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel_crashed");

    private float scale = 0.125f;
    private float t2X = 0.0f;
    private float t2Y = -1.0f;
    private float t2Z = 0.0f;
    private float rotation1 = 270f;


    private double cellSize = 1d/8d;

    public PanelTileRenderer(BlockEntityRendererProvider.Context context){
    }

    @Override
    public void render(PanelTile tileEntity, float p_112308_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        Boolean hasBase = tileEntity.hasBase();
        matrixStack.pushPose();

        switch (tileEntity.getBlockState().getValue(BlockStateProperties.FACING))
        {
            case UP:
                matrixStack.mulPose(Axis.XP.XP.rotationDegrees(180));
                matrixStack.translate(0,-1,-1);
                break;
            case NORTH:
                matrixStack.mulPose(Axis.XP.XP.rotationDegrees(90));
                matrixStack.translate(0,0,-1);
                break;
            case EAST:
                matrixStack.mulPose(Axis.XP.ZP.rotationDegrees(90));
                matrixStack.translate(0,-1,0);
                break;
            case SOUTH:
                matrixStack.mulPose(Axis.XP.XP.rotationDegrees(-90));
                matrixStack.translate(0,-1,0);
                break;
            case WEST:
                matrixStack.mulPose(Axis.XP.ZP.rotationDegrees(-90));
                matrixStack.translate(-1,0,0);
                break;
        }

        TextureAtlasSprite sprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE);
        TextureAtlasSprite borderSprite = RenderHelper.getSprite(PanelTileRenderer.TEXTURE_BORDER);

        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        if (tileEntity.isCovered())
        {
            matrixStack.pushPose();
            tileEntity.panelCover.render(matrixStack,buffer,combinedLight,combinedOverlay, tileEntity.getColor());
            matrixStack.popPose();
        }
        else {
            if (hasBase) {
                int topTextureIndex =
                        ((tileEntity.getConnectedPanelNeighbor(Side.FRONT))?2:0)
                                + ((tileEntity.getConnectedPanelNeighbor(Side.RIGHT))?1:0)
                                + ((tileEntity.getConnectedPanelNeighbor(Side.BACK))?8:0)
                                + ((tileEntity.getConnectedPanelNeighbor(Side.LEFT))?4:0);

                TextureAtlasSprite topSprite = (topTextureIndex==0)?sprite:RenderHelper.getSprite(TEXTURES[topTextureIndex]);

                int color = tileEntity.getColor();
                matrixStack.pushPose();
                matrixStack.mulPose(Axis.XP.rotationDegrees(270));
                matrixStack.translate(0, -1, 0.125);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, 1, topSprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Axis.XP.rotationDegrees(90));
                matrixStack.translate(0, -0.125, 0);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Axis.YP.rotationDegrees(90));
                matrixStack.translate(0, 0, 1);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Axis.YP.rotationDegrees(90));
                matrixStack.translate(0, 0, 1);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Axis.YP.rotationDegrees(90));
                matrixStack.translate(0, 0, 1);
                RenderHelper.drawRectangle(builder, matrixStack, 0, 1, 0, .125f, sprite, combinedLight, color, 1.0f);

                matrixStack.mulPose(Axis.XP.rotationDegrees(90));
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
            matrixStack.mulPose(Axis.XP.rotationDegrees(rotation1));

            sprite = RenderHelper.getSprite(TEXTURE_CRASHED);
            RenderHelper.drawRectangle(buffer.getBuffer((Minecraft.useShaderTransparency())?RenderType.solid():RenderType.translucent()),matrixStack,0,1,0,1,sprite,combinedLight,0.9f);
            matrixStack.popPose();
        }

        matrixStack.popPose();

    }

    private void renderCell(PoseStack matrixStack, PanelCellPos pos, MultiBufferSource buffer, int combinedLight, int combinedOverlay,float alpha,boolean hasBase)
    {
        alpha = (Minecraft.useShaderTransparency())?1.0f:alpha;

        matrixStack.pushPose();

        matrixStack.translate(cellSize*(double)pos.getRow(), ((hasBase)?0.125:0)+(pos.getLevel()*0.125), cellSize*(pos.getColumn()));
        matrixStack.mulPose(Axis.XP.rotationDegrees(rotation1));

        Side facing = pos.getCellFacing();

        if (facing == Side.LEFT)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(90));
        }
        else if (facing == Side.BACK)
        {
            matrixStack.translate(cellSize,-cellSize,0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        }
        else if (facing == Side.RIGHT)
        {
            matrixStack.translate(cellSize,0,0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(270));
        }
        else if (pos.getCellFacing()==Side.BOTTOM)
        {
            matrixStack.translate(0,-cellSize,0);
            matrixStack.mulPose(Axis.XP.rotationDegrees(-90));
        }
        else if (pos.getCellFacing()==Side.TOP)
        {
            matrixStack.translate(0,0,cellSize);
            matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        }

        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(t2X,t2Y,t2Z);

        pos.getIPanelCell().render(matrixStack, buffer, combinedLight, combinedOverlay,alpha);

        matrixStack.popPose();

    }

    @CheckForNull
    public static PanelCellGhostPos getPlayerLookingAtCell(PanelTile panelTile) {
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos blockPos = panelTile.getBlockPos();
        if (player != null) {
            double distance = panelTile.getBlockPos().distToCenterSqr(player.position());
            if (distance < 6.0d) {
                BlockHitResult blockHitResult = panelTile.getPlayerCollisionHitResult(player);
                PanelCellPos cellPos1 = PosInPanelCell.fromHitVec(panelTile, panelTile.getBlockPos(), blockHitResult);
                panelTile.panelCellHovering = cellPos1;

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
        return null;
    }


}
