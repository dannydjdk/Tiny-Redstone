package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.panelcells.GhostRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.blocks.panelcells.TransparentBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.CheckForNull;
import java.lang.reflect.InvocationTargetException;
import org.joml.Vector3f;

public class PanelTileRenderer implements BlockEntityRenderer<PanelTile> {

    public static ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0001"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0010"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0011"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0100"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0101"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0110"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0111"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1000"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1001"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1010"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1011"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1100"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1101"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1110"),
            ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1111")
    };
    public static ResourceLocation TEXTURE = TEXTURES[0];
    public static ResourceLocation TEXTURE_BORDER = ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_border");
    public static ResourceLocation TEXTURE_CRASHED = ResourceLocation.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_crashed");

    private static final float SCALE = 0.125f;
    private static final float T2X = 0.0f;
    private static final float T2Y = -1.0f;
    private static final float T2Z = 0.0f;
    private static final float ROTATION1 = 270f;
    private static final double CELL_SIZE = 1d/8d;

    public PanelTileRenderer(BlockEntityRendererProvider.Context context){
    }

    @Override
    public void render(PanelTile tileEntity, float p_112308_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        CachedPanelRenderer cache = tileEntity.getCachedRenderer();

        // Rebuild cache if dirty or if lighting changed
        if (cache.isDirty() || cache.lightChanged(combinedLight)) {
            PoseStack buildStack = new PoseStack();
            cache.rebuild(tileEntity, buildStack, combinedLight, combinedOverlay);
        }

        matrixStack.pushPose();

        // Camouflage covers render in block-local space (tesselateBlock output),
        // so they must NOT have the panel-facing rotation applied.
        // Everything else (cells, manual covers, panel base) is captured in panel-space
        // and needs the facing rotation during replay.
        if (!cache.isCamouflageCache()) {
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
        }

        // Replay cached geometry
        cache.replay(matrixStack, buffer, combinedLight);

        // Ghost preview is always dynamic — changes with mouse position every frame
        if (!tileEntity.isCovered() && tileEntity.panelCellGhostPos != null) {
            renderCellStatic(matrixStack, tileEntity.panelCellGhostPos, buffer, combinedLight, combinedOverlay, 0.5f, tileEntity.hasBase());
        }

        matrixStack.popPose();

    }

    /**
     * Render a single cell. This is a static method so it can be called both from the
     * live render path (ghost preview) and from CachedPanelRenderer during cache rebuilds.
     */
    static void renderCellStatic(PoseStack matrixStack, PanelCellPos pos, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha, boolean hasBase)
    {
        alpha = (Minecraft.useShaderTransparency())?1.0f:alpha;

        matrixStack.pushPose();

        matrixStack.translate(CELL_SIZE*(double)pos.getRow(), ((hasBase)?0.125:0)+(pos.getLevel()*0.125), CELL_SIZE*(pos.getColumn()));

        IPanelCell cell = pos.getIPanelCell();

        // For TinyBlock/TransparentBlock with a madeFrom block, use the block's actual
        // BakedModel instead of the sprite-guessing manual draw path.
        // This must happen BEFORE the X-270 rotation below, because BakedModel quads expect
        // standard Y-up orientation — which is exactly what panel space provides at this point.
        if (cell instanceof TinyBlock tinyBlock && tinyBlock.getMadeFrom() != null) {
            ResourceLocation madeFrom = tinyBlock.getMadeFrom();
            BlockState blockState = BuiltInRegistries.BLOCK.get(madeFrom).defaultBlockState();
            PanelTile panelTile = pos.getPanelTile();
            if (blockState != null && !blockState.isAir() && panelTile.getLevel() != null) {
                matrixStack.scale(SCALE, SCALE, SCALE);
                var blockRenderer = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = blockRenderer.getBlockModel(blockState);
                VertexConsumer builder = buffer.getBuffer(
                        (cell instanceof TransparentBlock || alpha < 1.0f) ? RenderType.translucent() : RenderType.solid());
                // Render each face direction with Minecraft's standard directional shading,
                // computed via the PoseStack normal matrix so it accounts for panel facing.
                // This avoids AO neighbor sampling (which causes seams between tiny blocks)
                // while still matching the shading of other tiny components.
                RandomSource randomSource = RandomSource.create();
                for (Direction direction : Direction.values()) {
                    Vector3f normal = matrixStack.last().normal().transform(
                            new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
                    normal.normalize();
                    float shade = RenderHelper.getShadeFromNormal(normal.x(), normal.y(), normal.z());
                    for (BakedQuad quad : model.getQuads(blockState, direction, randomSource)) {
                        builder.putBulkData(matrixStack.last(), quad, shade, shade, shade, alpha, combinedLight, combinedOverlay);
                    }
                }
                // Unculled quads (direction = null) get no directional shading
                for (BakedQuad quad : model.getQuads(blockState, null, randomSource)) {
                    builder.putBulkData(matrixStack.last(), quad, 1.0f, 1.0f, 1.0f, alpha, combinedLight, combinedOverlay);
                }
                matrixStack.popPose();
                return;
            }
        }

        matrixStack.mulPose(Axis.XP.rotationDegrees(ROTATION1));

        Side facing = pos.getCellFacing();

        if (facing == Side.LEFT)
        {
            matrixStack.translate(0,-CELL_SIZE,0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(90));
        }
        else if (facing == Side.BACK)
        {
            matrixStack.translate(CELL_SIZE,-CELL_SIZE,0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        }
        else if (facing == Side.RIGHT)
        {
            matrixStack.translate(CELL_SIZE,0,0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(270));
        }
        else if (pos.getCellFacing()==Side.BOTTOM)
        {
            matrixStack.translate(0,-CELL_SIZE,0);
            matrixStack.mulPose(Axis.XP.rotationDegrees(-90));
        }
        else if (pos.getCellFacing()==Side.TOP)
        {
            matrixStack.translate(0,0,CELL_SIZE);
            matrixStack.mulPose(Axis.XP.rotationDegrees(90));
        }

        matrixStack.scale(SCALE, SCALE, SCALE);

        // Default path: use cell's own render method
        matrixStack.translate(T2X, T2Y, T2Z);
        cell.render(matrixStack, buffer, combinedLight, combinedOverlay, alpha);

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