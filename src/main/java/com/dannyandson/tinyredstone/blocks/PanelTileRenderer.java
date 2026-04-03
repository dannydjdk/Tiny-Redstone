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
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * PanelTile renderer for NeoForge 26.1.
 *
 * In 1.21.9+, BlockEntityRenderer uses a render state system with three methods:
 * - createRenderState(): creates a new render state instance
 * - extractRenderState(): copies data from the block entity into the render state
 * - submit(): uses the render state data to emit geometry
 *
 * The second type parameter is the render state class.
 *
 * NOTE: If SubmitNodeCollector doesn't provide getBuffer() for custom vertex
 * rendering, this may need adjustment. The structural pattern is correct.
 */
public class PanelTileRenderer implements BlockEntityRenderer<PanelTile, PanelTileRenderState> {

    public static Identifier[] TEXTURES = {
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0001"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0010"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0011"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0100"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0101"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0110"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_0111"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1000"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1001"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1010"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1011"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1100"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1101"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1110"),
            Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_1111")
    };
    public static Identifier TEXTURE = TEXTURES[0];
    public static Identifier TEXTURE_BORDER = Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_border");
    public static Identifier TEXTURE_CRASHED = Identifier.fromNamespaceAndPath(TinyRedstone.MODID,"block/redstone_panel_crashed");

    private static final float SCALE = 0.125f;
    private static final float T2X = 0.0f;
    private static final float T2Y = -1.0f;
    private static final float T2Z = 0.0f;
    private static final float ROTATION1 = 270f;
    private static final double CELL_SIZE = 1d/8d;

    public PanelTileRenderer(BlockEntityRendererProvider.Context context){
    }

    @Override
    public PanelTileRenderState createRenderState() {
        return new PanelTileRenderState();
    }

    @Override
    public void extractRenderState(PanelTile tileEntity, PanelTileRenderState renderState, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(tileEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        CachedPanelRenderer cache = tileEntity.getCachedRenderer();

        // Get light from the block position
        int combinedLight = 0;
        if (tileEntity.getLevel() != null) {
            combinedLight = net.minecraft.client.renderer.LevelRenderer.getLightCoords(tileEntity.getLevel(), tileEntity.getBlockPos());
        }
        int combinedOverlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // Rebuild cache if dirty or if lighting changed
        if (cache.isDirty() || cache.lightChanged(combinedLight)) {
            PoseStack buildStack = new PoseStack();
            cache.rebuild(tileEntity, buildStack, combinedLight, combinedOverlay);
        }

        // Copy cached data into render state
        renderState.solidVertices.clear();
        renderState.solidVertices.addAll(cache.getSolidVertices());
        renderState.translucentVertices.clear();
        renderState.translucentVertices.addAll(cache.getTranslucentVertices());
        renderState.isCamouflageCache = cache.isCamouflageCache();
        renderState.facing = tileEntity.getBlockState().getValue(BlockStateProperties.FACING);
        renderState.hasCover = tileEntity.isCovered();
        renderState.hasBase = tileEntity.hasBase();

        // Ghost preview
        if (!tileEntity.isCovered() && tileEntity.panelCellGhostPos != null) {
            renderState.ghostPos = tileEntity.panelCellGhostPos;
        } else {
            renderState.ghostPos = null;
        }
    }

    @Override
    public void submit(PanelTileRenderState renderState, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // 26.1: SubmitNodeCollector does NOT extend MultiBufferSource and does NOT have getBuffer().
        // We use the game's immediate buffer source for custom vertex rendering.
        // This is the standard approach for block entity renderers that need direct vertex writing.
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        matrixStack.pushPose();

        if (!renderState.isCamouflageCache) {
            switch (renderState.facing) {
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
        org.joml.Matrix4f transform = matrixStack.last().pose();

        if (!renderState.solidVertices.isEmpty()) {
            VertexConsumer solidBuilder = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
            CachedPanelRenderer.replayVerticesStatic(solidBuilder, transform, renderState.solidVertices);
        }

        if (!renderState.translucentVertices.isEmpty()) {
            VertexConsumer translucentBuilder = bufferSource.getBuffer(Sheets.translucentBlockSheet());
            CachedPanelRenderer.replayVerticesStatic(translucentBuilder, transform, renderState.translucentVertices);
        }

        // Ghost preview is always dynamic
        if (renderState.ghostPos != null) {
            renderCellStatic(matrixStack, renderState.ghostPos, bufferSource,
                    renderState.lightCoords,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    0.5f, renderState.hasBase);
        }

        matrixStack.popPose();
    }

    /**
     * Render a single cell. This is a static method so it can be called both from the
     * live render path (ghost preview) and from CachedPanelRenderer during cache rebuilds.
     */
    static void renderCellStatic(PoseStack matrixStack, PanelCellPos pos, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha, boolean hasBase)
    {
        // useShaderTransparency removed in 26.1; just use the passed-in alpha

        matrixStack.pushPose();

        matrixStack.translate(CELL_SIZE*(double)pos.getRow(), ((hasBase)?0.125:0)+(pos.getLevel()*0.125), CELL_SIZE*(pos.getColumn()));

        IPanelCell cell = pos.getIPanelCell();

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

    @Nullable
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
                                            !panelCell.canAttachToBaseOnSide(attachingSideRel) || (
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