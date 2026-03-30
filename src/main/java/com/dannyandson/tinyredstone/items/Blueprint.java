package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.Config;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.Side;
import com.dannyandson.tinyredstone.blocks.panelcells.TinyBlock;
import com.dannyandson.tinyredstone.blocks.panelcells.TransparentBlock;
import com.dannyandson.tinyredstone.gui.BlueprintGUI;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Blueprint extends Item {

    public Blueprint(Item.Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags) {
        CompoundTag customTag = ItemStackHelper.getCustomTag(stack);
        if (customTag != null && customTag.contains("blueprint")) {
            list.add(Component.translatable("message.item.blueprint.full"));
            List<ItemStack> blueprintItems = getRequiredItemStacks(customTag.getCompound("blueprint").orElseGet(CompoundTag::new));
            for (ItemStack item : blueprintItems) {
                Component itemNameComponent = item.getHoverName();
                String itemName = itemNameComponent.getString();
                list.add(Component.nullToEmpty(itemName + " : " + item.getCount()));
            }
            list.add(Component.literal("CMD: " + stack.get(DataComponents.CUSTOM_MODEL_DATA)));
        } else {
            list.add(Component.translatable("message.item.blueprint.empty"));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
        CompoundTag itemCustomTag = ItemStackHelper.getCustomTag(context.getItemInHand());
        PanelTile panelTile = null;
        if (te instanceof PanelTile) {
            panelTile = (PanelTile) te;
        } else if (Config.ALLOW_WORLD_PLACEMENT.get()) {
            ItemStack itemStackCopy = context.getItemInHand().copy();
            BlockPlaceContext bpContext = new BlockPlaceContext(context);
            ((BlockItem) ModRegistration.REDSTONE_PANEL_ITEM.get()).place(bpContext);
            context.getPlayer().setItemInHand(context.getHand(), itemStackCopy);
            te = context.getLevel().getBlockEntity(bpContext.getClickedPos());
            if (te instanceof PanelTile) {
                panelTile = (PanelTile) te;
            }
        }
        if (panelTile != null) {
            if (itemCustomTag != null && itemCustomTag.contains("blueprint")) {
                Player player = context.getPlayer();
                if (panelTile.getCellCount() == 0 && player != null) {
                    CompoundTag blueprintNBT = itemCustomTag.getCompound("blueprint").orElseGet(CompoundTag::new);
                    List<ItemStack> items = getRequiredItemStacks(blueprintNBT);
                    if (player.isCreative() || playerHasSufficientComponents(items, player)) {
                        try {
                            panelTile.loadCellsFromNBT(blueprintNBT.getCompound("cells").orElseGet(CompoundTag::new));
                            panelTile.updateSide(Side.FRONT);
                            panelTile.updateSide(Side.RIGHT);
                            panelTile.updateSide(Side.BACK);
                            panelTile.updateSide(Side.LEFT);
                            panelTile.updateSide(Side.TOP);
                            panelTile.setChanged();
                        } catch (Exception e) {
                            panelTile.handleCrash(e);
                        }

                        if (!player.isCreative()) {
                            for (ItemStack item : items) {
                                int itemsToRemove = item.getCount();
                                for (ItemStack invStack : player.getInventory().getContents()) {
                                    if (stacksAreMatchingItem(invStack, item)) {
                                        int removeCt = Math.min(invStack.getCount(), itemsToRemove);
                                        invStack.setCount(invStack.getCount() - removeCt);
                                        itemsToRemove -= removeCt;
                                    }
                                    if (itemsToRemove == 0)
                                        break;
                                }
                            }
                        }
                    }
                    panelTile.removeOutOfRange(player);
                }
            } else {
                CompoundTag nbt = new CompoundTag();
                CompoundTag blueprintNBT = panelTile.saveToNbt(new CompoundTag());
                nbt.put("blueprint", blueprintNBT);
                ItemStackHelper.setCustomTag(context.getItemInHand(), nbt);
                context.getItemInHand().set(DataComponents.CUSTOM_MODEL_DATA,
                        new CustomModelData(1));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (worldIn.isClientSide() && Config.JSON_BLUEPRINT.get())
            BlueprintGUI.open(playerIn.getItemInHand(handIn));
        return super.use(worldIn, playerIn, handIn);
    }

    private static List<ItemStack> getRequiredItemStacks(CompoundTag blueprintNBT) {
        List<ItemStack> itemStacks = new ArrayList<>();

        if (blueprintNBT.contains("cells")) {
            CompoundTag cellsNBT = blueprintNBT.getCompound("cells").orElseGet(CompoundTag::new);
            for (String key : cellsNBT.keySet()) {
                try {
                    Class iPanelCellClass = Class.forName(cellsNBT.getCompound(key).orElseGet(CompoundTag::new).getStringOr("class", ""));
                    if (IPanelCell.class.isAssignableFrom(iPanelCellClass)) {
                        Item item = PanelBlock.getPanelCellItemFromClass((Class<? extends IPanelCell>) iPanelCellClass);
                        ItemStack itemStack = item.getDefaultInstance();
                        if (iPanelCellClass == TinyBlock.class || iPanelCellClass == TransparentBlock.class) {
                            CompoundTag cellDataNBT = cellsNBT.getCompound(key).orElseGet(CompoundTag::new).getCompound("data").orElseGet(CompoundTag::new);
                            if (cellDataNBT.contains("made_from_namespace")) {
                                CompoundTag madeFromTag = new CompoundTag();
                                madeFromTag.putString("namespace", cellDataNBT.getStringOr("made_from_namespace", ""));
                                madeFromTag.putString("path", cellDataNBT.getStringOr("made_from_path", ""));
                                CompoundTag itemTag = new CompoundTag();
                                itemTag.put("made_from", madeFromTag);
                                ItemStackHelper.setCustomTag(itemStack, itemTag);
                            }
                        }

                        boolean addNeeded = true;
                        for (ItemStack stack : itemStacks) {
                            if (stacksAreMatchingItem(stack, itemStack)) {
                                stack.setCount(stack.getCount() + 1);
                                addNeeded = false;
                                break;
                            }
                        }
                        if (addNeeded)
                            itemStacks.add(itemStack);
                    }
                } catch (ClassNotFoundException e) {
                    TinyRedstone.LOGGER.error("Class not found exception while attempting to read components from blueprint NBT: " + e.getLocalizedMessage());
                }
            }
        }
        return itemStacks;
    }

    private static boolean playerHasSufficientComponents(List<ItemStack> itemStacks, Player player) {
        for (ItemStack itemStack : itemStacks) {
            int count = 0;
            for (ItemStack invStack : player.getInventory().getContents()) {
                if (stacksAreMatchingItem(invStack, itemStack)) {
                    count += invStack.getCount();
                }
            }
            if (count < itemStack.getCount())
                return false;
        }
        return true;
    }

    private static boolean stacksAreMatchingItem(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) return false;
        CompoundTag tag1 = ItemStackHelper.getCustomTag(stack1);
        CompoundTag tag2 = ItemStackHelper.getCustomTag(stack2);
        return (tag1 == null && tag2 == null) || (tag1 != null && tag1.equals(tag2));
    }

    @Nullable
    public static CompoundTag cleanUpBlueprintNBT(CompoundTag nbt) {
        if (nbt.contains("blueprint")) {
            CompoundTag blueprintNBT = nbt.getCompound("blueprint").orElseGet(CompoundTag::new);
            if (blueprintNBT.contains("cells")) {
                CompoundTag newCellsNBT = new CompoundTag();
                CompoundTag cellsNBT = blueprintNBT.getCompound("cells").orElseGet(CompoundTag::new);
                for (String key : cellsNBT.keySet()) {
                    try {
                        if (IPanelCell.class.isAssignableFrom(Class.forName(cellsNBT.getCompound(key).orElseGet(CompoundTag::new).getStringOr("class", "")))) {
                            newCellsNBT.put(key, cellsNBT.getCompound(key).orElseGet(CompoundTag::new));
                        }
                    } catch (ClassNotFoundException e) {
                        TinyRedstone.LOGGER.error("Class not found exception while attempting to read components from blueprint NBT: " + e.getLocalizedMessage());
                    }
                }

                CompoundTag newNBT = new CompoundTag();
                CompoundTag newBlueprintNBT = new CompoundTag();
                newBlueprintNBT.put("cells", newCellsNBT);
                newNBT.put("blueprint", newBlueprintNBT);
                if (nbt.contains("display"))
                    newNBT.put("display", nbt.getCompound("display").orElseGet(CompoundTag::new));
                return newNBT;
            }
        }
        return null;
    }
}
