package com.dannyandson.tinyredstone.compat.theoneprobe;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Function;

public class PanelProvider implements IBlockDisplayOverride, Function<ITheOneProbe, Void>, IProbeInfoProvider, IProbeConfigProvider {

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath( TinyRedstone.MODID,"panel");
    }

    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        theOneProbe.registerBlockDisplayOverride(this);
        theOneProbe.registerProvider(this);
        theOneProbe.registerProbeConfigProvider(this);
        return null;
    }

    private boolean show(ProbeMode probeMode, Player playerEntity) {
        switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
            case 0:
                return probeMode == ProbeMode.DEBUG;
            case 1:
                break;
            case 2:
                if(probeMode == ProbeMode.NORMAL) return false;
                break;
            case 3:
                if(probeMode != ProbeMode.DEBUG) return CompatHandler.isMeasuringDevice(playerEntity.getMainHandItem().getItem());
                break;
            case 4:
                if(probeMode != ProbeMode.DEBUG) return CompatHandler.isTinyComponent(playerEntity.getMainHandItem().getItem());
                break;
        }
        return true;
    }

    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player playerEntity, Level world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        BlockEntity tileEntity = world.getBlockEntity(pos);

        if (tileEntity instanceof PanelTile panelTile && show(probeMode, playerEntity)) {

            Block block = blockState.getBlock();

            if(!panelTile.isCovered() && block instanceof PanelBlock) {
                BlockHitResult result = new BlockHitResult(probeHitData.getHitVec(),probeHitData.getSideHit(),pos,true);
                PanelCellPos panelCellPos = PanelCellPos.fromHitVec(panelTile,blockState.getValue(BlockStateProperties.FACING),result);

                if(panelCellPos!=null) {
                    IPanelCell panelCell = panelCellPos.getIPanelCell();
                    if(panelCell != null) {
                        String modName = Tools.getModName(block);

                        Item item = PanelBlock.getItemByIPanelCell(panelCell.getClass());
                        ItemStack itemStack = item.getDefaultInstance();
                        CompoundTag itemTag = panelCell.getItemTag();
                        if (itemTag!=null){
                            for (String key : itemTag.getAllKeys()){
                                ItemStackHelper.addTagElement(itemStack, key, itemTag.get(key));
                            }
                        }

                        IProbeConfig config = Config.getRealConfig();
                        if (Tools.show(probeMode, config.getShowModName())) {
                            probeInfo.horizontal()
                                    .item(itemStack)
                                    .vertical()
                                    .itemLabel(itemStack)
                                    .text(CompoundText.create().style(TextStyleClass.MODNAME).text(modName));
                        } else {
                            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                                    .item(itemStack)
                                    .itemLabel(itemStack);
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player playerEntity, Level world, BlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();
        BlockEntity tileEntity = world.getBlockEntity(pos);

        if (tileEntity instanceof PanelTile panelTile && show(probeMode, playerEntity)) {

            if (!panelTile.isCovered()) {

                BlockHitResult rtr = new BlockHitResult(probeHitData.getHitVec(),probeHitData.getSideHit(),pos,true);
                PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, rtr);

                if (posInPanelCell != null) {

                    if (probeMode == ProbeMode.DEBUG) {
                        int cellIndex = posInPanelCell.getIndex();
                        PanelCellSegment segment = posInPanelCell.getSegment();

                        probeInfo.vertical(new LayoutStyle().borderColor(0xff44ff44).spacing(2))
                                .text(CompoundText.createLabelInfo("X: ", posInPanelCell.getX()))
                                .text(CompoundText.createLabelInfo("Z: ", posInPanelCell.getZ()))
                                .text(CompoundText.createLabelInfo("Y: ", posInPanelCell.getY()))
                                .text(CompoundText.createLabelInfo("Row: ", posInPanelCell.getRow()))
                                .text(CompoundText.createLabelInfo("Column: ", posInPanelCell.getColumn()))
                                .text(CompoundText.createLabelInfo("Level: ", posInPanelCell.getLevel()))
                                .text(CompoundText.createLabelInfo("Index: ", cellIndex))
                                .text(CompoundText.createLabelInfo("Segment: ", segment))
                                .text(CompoundText.createLabelInfo("Facing: ", posInPanelCell.getCellFacing()));
                    }

                    IPanelCell panelCell = posInPanelCell.getIPanelCell();
                    if (panelCell != null) {
                        boolean handled = false;

                        if (panelCell instanceof IPanelCellInfoProvider) {
                            OverlayBlockInfo overlayBlockInfo = new OverlayBlockInfo(probeInfo, probeMode);
                            ((IPanelCellInfoProvider) panelCell).addInfo(overlayBlockInfo, panelTile, posInPanelCell);
                            if(overlayBlockInfo.power > -1) {
                                handled = true;
                                showRedstonePower(probeInfo, overlayBlockInfo.power);
                            }
                        }
                        if (!handled) {
                            Side sideHit = panelTile.getPanelCellSide(posInPanelCell,panelTile.getSideFromDirection(probeHitData.getSideHit()));
                            showRedstonePower(probeInfo, panelCell.getWeakRsOutput(sideHit));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void getProbeConfig(IProbeConfig config, Player player, Level world, BlockState blockState, IProbeHitData data) {
        // Suppress the default provider's redstone display ONLY when the player is hovering a placed component.
        BlockEntity tileEntity = world.getBlockEntity(data.getPos());
        if (tileEntity instanceof PanelTile panelTile && !panelTile.isCovered()) {
            BlockHitResult hit = new BlockHitResult(data.getHitVec(), data.getSideHit(), data.getPos(), true);
            PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, data.getPos(), hit);
            if (posInPanelCell != null && posInPanelCell.getIPanelCell() != null) {
                config.showRedstone(IProbeConfig.ConfigMode.NOT);
            }
        }
    }

    @Override
    public void getProbeConfig(IProbeConfig config, Player player, Level world, Entity entity, IProbeHitEntityData data) {
        // Tiny Redstone has no entity-level integration; leave the default config alone.
    }

    private static void showRedstonePower(IProbeInfo probeInfo, int power) {
        if (power > 0) {
            probeInfo.horizontal()
                    .item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(CompoundText.createLabelInfo("Power: ", power));
        }
    }
}