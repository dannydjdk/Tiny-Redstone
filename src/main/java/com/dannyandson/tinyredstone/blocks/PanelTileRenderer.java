package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IRenderTarget;
import com.dannyandson.tinyredstone.blocks.panelcells.GhostRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.InvocationTargetException;

/**
 * PanelTile renderer for NeoForge 26.2.
 *
 * In 1.21.9+, BlockEntityRenderer uses a render state system with three methods:
 * - createRenderState(): creates a new render state instance
 * - extractRenderState(): copies data from the block entity into the render state
 * - submit(): uses the render state data to emit geometry
 *
 * The second type parameter is the render state class.
 *
 * 26.2 removed MultiBufferSource, Sheets.cutout/translucentBlockSheet() and
 * RenderBuffers.bufferSource(). Custom geometry is now emitted through the feature
 * submit pipeline (SubmitNodeCollector#submitCustomGeometry); see submitLayer().
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

        // Get light from the block position.
        int combinedLight = 0;
        if (tileEntity.getLevel() != null) {
            combinedLight = LightCoordsUtil.getLightCoords(tileEntity.getLevel(), tileEntity.getBlockPos());
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
        // Custom BER geometry flows through the feature submit pipeline via
        // SubmitNodeCollector#submitCustomGeometry (see submitLayer()). Cached vertices are
        // captured in panel-local space; the facing/world transform below is applied once
        // per vertex during replay.

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

        // Replay cached geometry (facing transform applied once, at submit time)
        submitCachedVertices(matrixStack, submitNodeCollector,
                renderState.solidVertices, renderState.translucentVertices);

        // Ghost preview is always dynamic. Capture it into a fresh panel-local buffer using a
        // fresh PoseStack (mirroring the BER cache path), then submit through the same pipeline
        // so the facing transform above is applied exactly once.
        if (renderState.ghostPos != null) {
            List<CachedPanelRenderer.CachedVertex> ghostSolid = new ArrayList<>();
            List<CachedPanelRenderer.CachedVertex> ghostTranslucent = new ArrayList<>();
            CachedPanelRenderer.VertexCapture ghostCapture =
                    new CachedPanelRenderer.VertexCapture(ghostSolid, ghostTranslucent);

            renderCellStatic(new PoseStack(), renderState.ghostPos, ghostCapture,
                    renderState.lightCoords,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    0.5f, renderState.hasBase);
            ghostCapture.flush();

            submitCachedVertices(matrixStack, submitNodeCollector, ghostSolid, ghostTranslucent);
        }

        matrixStack.popPose();
    }

    /**
     * Submit cached (panel-local) vertex data through the feature submit pipeline.
     * The given PoseStack supplies the world/facing transform, applied once per vertex during
     * replay. Solid geometry goes to the cutout layer, translucent to the translucent layer.
     */
    public static void submitCachedVertices(PoseStack matrixStack, SubmitNodeCollector collector,
                                            List<CachedPanelRenderer.CachedVertex> solid,
                                            List<CachedPanelRenderer.CachedVertex> translucent) {
        if (!solid.isEmpty()) {
            submitLayer(matrixStack, collector, RenderHelper.cutoutBlockRenderType(), solid);
        }
        if (!translucent.isEmpty()) {
            submitLayer(matrixStack, collector, RenderHelper.translucentBlockRenderType(), translucent);
        }
    }

    /**
     * Submit one render layer's worth of cached vertices.
     */
    private static void submitLayer(PoseStack matrixStack, SubmitNodeCollector collector,
                                    RenderType renderType, List<CachedPanelRenderer.CachedVertex> verts) {
        collector.submitCustomGeometry(matrixStack, renderType,
                (pose, consumer) -> CachedPanelRenderer.replayVerticesStatic(consumer, pose.pose(), verts));
    }

    /**
     * Render a single cell. This is a static method so it can be called both from the
     * live render path (ghost preview) and from CachedPanelRenderer during cache rebuilds.
     */
    static void renderCellStatic(PoseStack matrixStack, PanelCellPos pos, IRenderTarget target, int combinedLight, int combinedOverlay, float alpha, boolean hasBase)
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
        cell.render(matrixStack, target, combinedLight, combinedOverlay, alpha);

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