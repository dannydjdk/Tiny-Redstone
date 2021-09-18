package com.dannyandson.tinyredstone.compat.hwyla;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.api.IPanelCellInfoProvider;
import com.dannyandson.tinyredstone.blocks.*;
import com.dannyandson.tinyredstone.compat.CompatHandler;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin(TinyRedstone.MODID)
public class PanelProvider implements IWailaPlugin, IComponentProvider {
    static final ResourceLocation RENDER_STRING = new ResourceLocation("string");
    static final ResourceLocation RENDER_ITEM_INLINE = new ResourceLocation("item_inline");
    static final ResourceLocation RENDER_INFO_STRING = new ResourceLocation("info_string");

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(this, TooltipPosition.BODY, PanelBlock.class);
    }

    private boolean show(Player playerEntity) {
        switch (com.dannyandson.tinyredstone.Config.DISPLAY_MODE.get()) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return playerEntity.isCrouching();
            case 3:
                return CompatHandler.isMeasuringDevice(playerEntity.getMainHandItem().getItem());
            case 4:
                return CompatHandler.isTinyComponent(playerEntity.getMainHandItem().getItem());
        }
        return true;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
         if(accessor.getBlock() != null) {
            BlockPos pos = accessor.getPosition();
            BlockEntity tileEntity = accessor.getLevel().getBlockEntity(pos);

            if (tileEntity instanceof PanelTile panelTile && show(accessor.getPlayer())) {
                IElementHelper helper = tooltip.getElementHelper();

                if (!panelTile.isCovered()) {

                    BlockHitResult rtr = new BlockHitResult(accessor.getHitResult().getLocation(),accessor.getSide(),pos,true);
                    PosInPanelCell posInPanelCell = PosInPanelCell.fromHitVec(panelTile, pos, rtr);

                    if (posInPanelCell != null) {
                        IPanelCell panelCell = posInPanelCell.getIPanelCell();
                        if (panelCell != null) {
                            boolean handled = false;

                            Item item = PanelBlock.getItemByIPanelCell(panelCell.getClass());
                            List<IElement> elements = new ArrayList<>();
                            elements.add(helper.text(new TranslatableComponent(item.getDescriptionId())).tag(PanelProvider.RENDER_STRING));//.translate(new Vec2(-6,0)));
                            tooltip.add(elements);

                            if (panelCell instanceof IPanelCellInfoProvider) {
                                OverlayBlockInfo overlayBlockInfo = new OverlayBlockInfo(tooltip, accessor.getPlayer().isCrouching());
                                ((IPanelCellInfoProvider) panelCell).addInfo(overlayBlockInfo, panelTile, posInPanelCell);
                                if(overlayBlockInfo.power > -1) {
                                    handled = true;
                                    if(overlayBlockInfo.power > 0) {
                                        showRedstonePower(tooltip, helper, overlayBlockInfo.power);
                                    }
                                }
                            }
                            if (!handled) {
                                Side sideHit = panelTile.getPanelCellSide(posInPanelCell,panelTile.getSideFromDirection(accessor.getSide()));
                                int power = panelCell.getWeakRsOutput(sideHit);
                                if(power > 0)
                                    showRedstonePower(tooltip, helper, power);
                            }
                        }
                    } else {
                        showBlockRedstonePower(tooltip, accessor.getLevel(), pos, accessor.getSide(), helper);
                    }
                } else {
                    showBlockRedstonePower(tooltip, accessor.getLevel(), pos, accessor.getSide(), helper);
                }
            }
        }
    }

    private static void showBlockRedstonePower(ITooltip tooltip, Level world, BlockPos pos, Direction sideHit, IElementHelper helper) {
        showRedstonePower(tooltip, helper, world.getSignal(pos, sideHit.getOpposite()));
    }

    private static void showRedstonePower(ITooltip tooltip, IElementHelper helper, int power) {
        if(power > 0) {
            List<IElement> elements = new ArrayList<>();
            elements.add(helper.text(Component.nullToEmpty("Power:")).tag(PanelProvider.RENDER_STRING));
            elements.add(helper.text(Component.nullToEmpty(" " + power)).tag(PanelProvider.RENDER_INFO_STRING));
            tooltip.add(elements);
        }
    }

}