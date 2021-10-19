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
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Blueprint extends Item {

    public Blueprint() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags)
    {
        if (stack.getTag() !=null && stack.getTag().contains("blueprint"))
        {
            list.add(new TranslatableComponent("message.item.blueprint.full"));
            List<ItemStack> blueprintItems = getRequiredItemStacks(stack.getTagElement("blueprint"));
            for (ItemStack item : blueprintItems)
            {
                Component itemNameComponent = item.getHoverName();// new TranslatableComponent(item.getDescriptionId());
                String itemName = itemNameComponent.getString();
                list.add(Component.nullToEmpty(itemName + " : " + item.getCount()));
            }
        }
        else
        {
            list.add(new TranslatableComponent("message.item.blueprint.empty"));
        }
    }


    //called when item is used on a block
    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
        if (te instanceof PanelTile)
        {
            PanelTile panelTile = (PanelTile)te;
            if (context.getItemInHand().getTag() !=null && context.getItemInHand().getTag().contains("blueprint"))
            {
                Player player = context.getPlayer();
                if (panelTile.getCellCount()==0 && player!=null)
                {
                    CompoundTag blueprintNBT = context.getItemInHand().getTagElement("blueprint");
                    List<ItemStack> items = getRequiredItemStacks(blueprintNBT);
                    if (player.isCreative() || playerHasSufficientComponents(items, player)) {

                        try {

                            panelTile.loadCellsFromNBT(blueprintNBT);
                            panelTile.updateSide(Side.FRONT);
                            panelTile.updateSide(Side.RIGHT);
                            panelTile.updateSide(Side.BACK);
                            panelTile.updateSide(Side.LEFT);
                            panelTile.updateSide(Side.TOP);
                            panelTile.setChanged();

                        }catch (Exception e){
                            panelTile.handleCrash(e);
                        }

                        if (!player.isCreative())
                        {
                            for (ItemStack item : items)
                            {
                                int itemsToRemove = item.getCount();
                                for(ItemStack invStack : player.getInventory().items)
                                {
                                    if (stacksAreMatchingItem(invStack,item))
                                    {
                                        int removeCt = Math.min(invStack.getCount(),itemsToRemove);
                                        invStack.setCount(invStack.getCount()-removeCt);
                                        itemsToRemove-=removeCt;
                                    }
                                    if (itemsToRemove==0)
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            else {
                CompoundTag nbt = new CompoundTag();
                CompoundTag blueprintNBT = panelTile.saveToNbt(new CompoundTag());
                nbt.putInt("CustomModelData",1);
                nbt.put("blueprint",blueprintNBT);

                context.getItemInHand().setTag(nbt);

            }
        }

        return InteractionResult.SUCCESS;
    }

    //called when item is right clicked in the air
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if(worldIn.isClientSide && Config.JSON_BLUEPRINT.get())
            BlueprintGUI.open(playerIn.getItemInHand(handIn));
        return super.use(worldIn, playerIn, handIn);
    }

    private static List<ItemStack> getRequiredItemStacks(CompoundTag blueprintNBT) {
        List<ItemStack> itemStacks = new ArrayList<>();

        if (blueprintNBT.contains("cells")) {
            CompoundTag cellsNBT = blueprintNBT.getCompound("cells");
            for (String key : cellsNBT.getAllKeys()) {
                try {
                    Class iPanelCellClass = Class.forName(cellsNBT.getCompound(key).getString("class"));
                    if (IPanelCell.class.isAssignableFrom(iPanelCellClass)) {
                        Item item = PanelBlock.getPanelCellItemFromClass((Class<? extends IPanelCell>) iPanelCellClass);
                        ItemStack itemStack = item.getDefaultInstance();
                        if (iPanelCellClass == TinyBlock.class || iPanelCellClass == TransparentBlock.class) {
                            CompoundTag cellDataNBT = cellsNBT.getCompound(key).getCompound("data");
                            if (cellDataNBT.contains("made_from_namespace")) {
                                CompoundTag madeFromTag = new CompoundTag();
                                madeFromTag.putString("namespace", cellDataNBT.getString("made_from_namespace"));
                                madeFromTag.putString("path", cellDataNBT.getString("made_from_path"));
                                CompoundTag itemTag = new CompoundTag();
                                itemTag.put("made_from", madeFromTag);
                                itemStack.setTag(itemTag);
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

    private static boolean playerHasSufficientComponents(List<ItemStack> itemStacks, Player player)
    {
        for (ItemStack itemStack : itemStacks)
        {
            int count = 0;
            for(ItemStack invStack : player.getInventory().items)
            {
                if (stacksAreMatchingItem(invStack,itemStack))
                {
                    count+= invStack.getCount();
                }
            }
            if (count<itemStack.getCount())
                return false;
        }
        return true;
    }

    private static boolean stacksAreMatchingItem(ItemStack stack1, ItemStack stack2){
        return stack1.getItem() == stack2.getItem() &&
                (
                        (!stack1.hasTag() && !stack2.hasTag()) ||
                                (stack1.hasTag() && stack1.getTag().equals(stack2.getTag()))
                );
    }

    /**
     * Checks nbt for valid blueprint data. Removes any irrelevant data or cells with no definitions installed.
     * RETURNS NULL if no valid data found.
     * @param nbt CompoundTag to be cleaned up - usually acquired untrusted source such as a json file or network
     * @return cleaned up NBT with any irrelevant date removed. NULL if no valid data found.
     */
    @Nullable
    public static CompoundTag cleanUpBlueprintNBT(CompoundTag nbt)
    {
        if (nbt.contains("blueprint"))
        {
            CompoundTag blueprintNBT = nbt.getCompound("blueprint");
            if (blueprintNBT.contains("cells"))
            {
                CompoundTag newCellsNBT = new CompoundTag();

                CompoundTag cellsNBT = blueprintNBT.getCompound("cells");
                for (String key : cellsNBT.getAllKeys())
                {
                    try {
                        if (IPanelCell.class.isAssignableFrom(Class.forName(cellsNBT.getCompound(key).getString("class"))))
                        {
                            newCellsNBT.put(key,cellsNBT.getCompound(key));
                        }
                    }catch (ClassNotFoundException e)
                    {
                        TinyRedstone.LOGGER.error("Class not found exception while attempting to read components from blueprint NBT: " + e.getLocalizedMessage());
                    }

                }

                CompoundTag newNBT = new CompoundTag();
                CompoundTag newBlueprintNBT = new CompoundTag();
                newNBT.putInt("CustomModelData",1);
                newBlueprintNBT.put("cells",newCellsNBT);
                newNBT.put("blueprint",newBlueprintNBT);
                if (nbt.contains("display"))
                    newNBT.put("display",nbt.getCompound("display"));

                return newNBT;
            }
        }
        return null;
    }

}
