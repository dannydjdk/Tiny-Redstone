package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.setup.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.CheckForNull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PanelTileRenderer implements BlockEntityRenderer<PanelTile> {

    public static ResourceLocation TEXTURE = new ResourceLocation(TinyRedstone.MODID,"block/redstone_panel");
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
        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        int color = tileEntity.getColor();
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(270));
        matrixStack.translate(0,-1,0.125);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,1.0f);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-0.125,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color,1.0f);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color,1.0f);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color,1.0f);

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        matrixStack.translate(0,0,1);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,.125f,sprite,combinedLight,color,1.0f);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.translate(0,-1,0);
        RenderHelper.drawRectangle(builder,matrixStack,0,1,0,1,sprite,combinedLight,color,1.0f);

        matrixStack.popPose();

        if (tileEntity.isCovered())
        {
            matrixStack.pushPose();
            tileEntity.panelCover.render(matrixStack,buffer,combinedLight,combinedOverlay, tileEntity.getColor());
            matrixStack.popPose();
        }
        else {
            List<PanelCellPos> positions = tileEntity.getCellPositions();
            for (PanelCellPos pos : positions) {
                IPanelCell panelCell = pos.getIPanelCell();
                if (panelCell!=null) {
                    renderCell(matrixStack, pos, buffer, (tileEntity.isCrashed()) ? 0 : combinedLight, combinedOverlay, (tileEntity.isCrashed()) ? 0.5f : 1.0f);
                }
            }

            if (tileEntity.panelCellGhostPos != null) {
                renderCell(matrixStack, tileEntity.panelCellGhostPos, buffer, combinedLight, combinedOverlay, 0.5f);
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

    private void renderCell(PoseStack matrixStack, PanelCellPos pos, MultiBufferSource buffer, int combinedLight, int combinedOverlay,float alpha)
    {
        alpha = (Minecraft.useShaderTransparency())?1.0f:alpha;

        matrixStack.pushPose();

        matrixStack.translate(cellSize*(double)pos.getRow(), 0.125+(pos.getLevel()*0.125), cellSize*(pos.getColumn()));
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

    @CheckForNull
    public static PanelCellGhostPos getPlayerLookingAtCell(PanelTile panelTile)
    {
        Level world = panelTile.getLevel();
        LocalPlayer player = Minecraft.getInstance().player;

        if (player!=null && PanelBlock.isPanelCellItem(player.getMainHandItem().getItem())) {

            HitResult lookingAt = Minecraft.getInstance().hitResult;

            if (lookingAt != null && lookingAt.getType() == HitResult.Type.BLOCK) {

                BlockHitResult blockHitResult = (BlockHitResult)lookingAt;
                Vec3 lookVector = Minecraft.getInstance().hitResult.getLocation();
                BlockPos blockPos = new BlockPos(lookVector);
                BlockEntity te = world.getBlockEntity(blockPos);

                if (te == panelTile) {

                    PanelCellPos cellPos1 =  PosInPanelCell.fromHitVec(panelTile,panelTile.getBlockPos(),blockHitResult);
                    if (cellPos1!=null) {
                        PanelCellPos cellPos = cellPos1;
                        if (cellPos.getIPanelCell()!=null && (!cellPos.getIPanelCell().hasActivation() || player.isCrouching()))
                        {
                            cellPos = cellPos.offset(panelTile.getSideFromDirection(blockHitResult.getDirection()));
                        }
                        if (cellPos!=null && cellPos.getIPanelCell()==null) {
                            try {
                                IPanelCell panelCell = (IPanelCell) PanelBlock.getPanelCellClassFromItem(player.getMainHandItem().getItem()).getConstructors()[0].newInstance();
                                Side rotationLock = RotationLock.getRotationLock();
                                Side cellFacing = rotationLock == null ?
                                        panelTile.getSideFromDirection(panelTile.getPlayerDirectionFacing(player, panelCell.canPlaceVertical()))
                                        : rotationLock;

                                if (panelCell.needsSolidBase())
                                {
                                    Side attachingSideDir = panelTile.getSideFromDirection(blockHitResult.getDirection()).getOpposite();
                                    Side attachingSideRel = (attachingSideDir==Side.TOP || attachingSideDir==Side.BOTTOM)?attachingSideDir:Side.FRONT;

                                    if (
                                            !cellPos1.equals(cellPos)
                                                    && (
                                                    cellPos1.getIPanelCell() == null
                                                            || !cellPos1.getIPanelCell().isPushable()
                                                            //check if the cell can attach to the side of the block facing
                                                            || !cellPos1.getIPanelCell().canAttachToBaseOnSide(attachingSideRel)
                                            )
                                    )
                                    {
                                        return null;
                                    }else{
                                        panelCell.setBaseSide(attachingSideRel);
                                        if (attachingSideRel==Side.FRONT)
                                            cellFacing=attachingSideDir;
                                    }
                                }

                                panelCell.onPlace(cellPos,player);
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
            }
        }
        return null;
    }


}
