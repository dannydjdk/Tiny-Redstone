package com.dannyandson.tinyredstone.compat.jade;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

public enum JadePanelComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final Identifier UID = Identifier.fromNamespaceAndPath(TinyRedstone.MODID, "panel");

    @Override
    public Identifier getUid() {
        return UID;
    }

    /**
     * Determines whether the overlay should be shown based on the mod's
     * DISPLAY_MODE config setting and what the player is holding.
     */
    private boolean shouldShow(BlockAccessor accessor) {
        var player = accessor.getPlayer();
        switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                // Extended/debug — treat Jade's "show details" (sneak) as extended.
                return accessor.showDetails();
            case 3:
                return CompatHandler.isMeasuringDevice(player.getMainHandItem().getItem());
            case 4:
                return CompatHandler.isTinyComponent(player.getMainHandItem().getItem());
            default:
                return true;
        }
    }

    /**
     * Resolves the PanelCellPos the player is looking at, or null if they
     * aren't hovering over a cell.
     */
    @Nullable
    private PanelCellPos getCellPos(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof PanelTile panelTile)) return null;
        if (panelTile.isCovered()) return null;
        if (!(accessor.getBlock() instanceof PanelBlock)) return null;

        BlockHitResult hitResult = accessor.getHitResult();
        return PanelCellPos.fromHitVec(
                panelTile,
                accessor.getBlockState().getValue(BlockStateProperties.FACING),
                hitResult
        );
    }

    /**
     * Builds an ItemStack representing the hovered cell, including any
     * custom item tag data (e.g. TinyBlock's 'made_from' tag).
     */
    @Nullable
    private ItemStack getCellItemStack(IPanelCell panelCell) {
        Item item = PanelBlock.getItemByIPanelCell(panelCell.getClass());
        if (item == null) return null;

        ItemStack itemStack = item.getDefaultInstance();
        CompoundTag itemTag = panelCell.getItemTag();
        if (itemTag != null) {
            for (String key : itemTag.keySet()) {
                ItemStackHelper.addTagElement(itemStack, key, itemTag.get(key));
            }
        }
        return itemStack;
    }

    // ── Icon override ────────────────────────────────────────────────────

    @Override
    public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, @Nullable Element currentIcon) {
        if (!shouldShow(accessor)) return null;

        PanelCellPos cellPos = getCellPos(accessor);
        if (cellPos == null) return null;

        IPanelCell panelCell = cellPos.getIPanelCell();
        if (panelCell == null) return null;

        ItemStack cellStack = getCellItemStack(panelCell);
        if (cellStack == null) return null;

        return JadeUI.item(cellStack);
    }

    // ── Tooltip body ─────────────────────────────────────────────────────

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof PanelTile panelTile)) return;
        if (!shouldShow(accessor)) return;

        BlockHitResult hitResult = accessor.getHitResult();

        if (!panelTile.isCovered() && accessor.getBlock() instanceof PanelBlock) {
            PanelCellPos panelCellPos = PanelCellPos.fromHitVec(
                    panelTile,
                    accessor.getBlockState().getValue(BlockStateProperties.FACING),
                    hitResult
            );

            if (panelCellPos != null) {
                IPanelCell panelCell = panelCellPos.getIPanelCell();

                // Replace Jade's default "Redstone Panel" header with the cell's item name
                if (panelCell != null) {
                    ItemStack cellStack = getCellItemStack(panelCell);
                    if (cellStack != null) {
                        tooltip.replace(JadeIds.CORE_OBJECT_NAME, cellStack.getHoverName());
                    }
                }

                if (panelCell != null) {
                    PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(
                            panelTile, accessor.getPosition(), hitResult
                    );

                    if (posInPanelCell != null) {
                        boolean handled = false;

                        // Delegate to the cell's own info provider if it implements one
                        if (panelCell instanceof IPanelCellInfoProvider infoProvider) {
                            JadeOverlayBlockInfo overlayBlockInfo = new JadeOverlayBlockInfo(tooltip);
                            infoProvider.addInfo(overlayBlockInfo, panelTile, posInPanelCell);
                            if (overlayBlockInfo.getPower() > -1) {
                                handled = true;
                                showRedstonePower(tooltip, overlayBlockInfo.getPower());
                            }
                        }

                        // Fall back to showing the generic weak signal output
                        if (!handled) {
                            Side sideHit = panelTile.getPanelCellSide(
                                    posInPanelCell,
                                    panelTile.getSideFromDirection(hitResult.getDirection())
                            );
                            showRedstonePower(tooltip, panelCell.getWeakRsOutput(sideHit));
                        }
                    }
                }
            } else {
                // Hovering over the panel base (not a cell)
                showBlockRedstonePower(tooltip, accessor);
            }
        } else {
            // Covered panel — show block-level redstone output
            showBlockRedstonePower(tooltip, accessor);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static void showBlockRedstonePower(ITooltip tooltip, BlockAccessor accessor) {
        int power = accessor.getLevel().getSignal(
                accessor.getPosition(),
                accessor.getHitResult().getDirection().getOpposite()
        );
        showRedstonePower(tooltip, power);
    }

    private static void showRedstonePower(ITooltip tooltip, int power) {
        if (power > 0) {
            tooltip.add(JadeUI.smallItem(new ItemStack(Items.REDSTONE)));
            tooltip.append(Component.translatable("jade.tinyredstone.power", power));
        }
    }
}