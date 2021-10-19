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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Blueprint extends Item {

    public Blueprint() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags)
    {
        if (stack.getTag() !=null && stack.getTag().contains("blueprint"))
        {
            list.add(new TranslationTextComponent("message.item.blueprint.full"));
            List<ItemStack> blueprintItems = getRequiredItemStacks(stack.getTagElement("blueprint"));
            for (ItemStack item : blueprintItems)
            {
                ITextComponent itemNameComponent = item.getHoverName();// new TranslatableComponent(item.getDescriptionId());
                String itemName = itemNameComponent.getString();
                list.add(ITextComponent.nullToEmpty(itemName + " : " + item.getCount()));
            }
        }
        else
        {
            list.add(new TranslationTextComponent("message.item.blueprint.empty"));
        }
    }


    //called when item is used on a block
    @Override
    @Nonnull
    public ActionResultType useOn(ItemUseContext context) {
        TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
        if (te instanceof PanelTile)
        {
            PanelTile panelTile = (PanelTile)te;
            if (context.getItemInHand().getTag() !=null && context.getItemInHand().getTag().contains("blueprint"))
            {
                PlayerEntity player = context.getPlayer();
                if (panelTile.getCellCount()==0 && player!=null)
                {
                    CompoundNBT blueprintNBT = context.getItemInHand().getTagElement("blueprint");
                    List<ItemStack> items = getRequiredItemStacks(blueprintNBT);
                    if (player.isCreative() || playerHasSufficientComponents(items, player)) {

                        panelTile.loadCellsFromNBT(blueprintNBT,false);
                        panelTile.updateSide(Side.FRONT);
                        panelTile.updateSide(Side.RIGHT);
                        panelTile.updateSide(Side.BACK);
                        panelTile.updateSide(Side.LEFT);
                        panelTile.updateSide(Side.TOP);
                        panelTile.setChanged();

                        if (!player.isCreative())
                        {
                            for (ItemStack item : items)
                            {
                                int itemsToRemove = item.getCount();
                                for(ItemStack invStack : player.inventory.items)
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
                CompoundNBT nbt = new CompoundNBT();
                CompoundNBT blueprintNBT = panelTile.saveToNbt(new CompoundNBT());
                nbt.putInt("CustomModelData",1);
                nbt.put("blueprint",blueprintNBT);

                context.getItemInHand().setTag(nbt);

            }
        }

        return ActionResultType.SUCCESS;
    }

    //called when item is right clicked in the air
    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(worldIn.isClientSide && Config.JSON_BLUEPRINT.get())
            BlueprintGUI.open(playerIn.getItemInHand(handIn));
        return super.use(worldIn, playerIn, handIn);
    }

    private static List<ItemStack> getRequiredItemStacks(CompoundNBT blueprintNBT) {
        List<ItemStack> itemStacks = new ArrayList<>();

        if (blueprintNBT.contains("cells")) {
            CompoundNBT cellsNBT = blueprintNBT.getCompound("cells");
            for (String key : cellsNBT.getAllKeys()) {
                try {
                    Class iPanelCellClass = Class.forName(cellsNBT.getCompound(key).getString("class"));
                    if (IPanelCell.class.isAssignableFrom(iPanelCellClass)) {
                        Item item = PanelBlock.getPanelCellItemFromClass((Class<? extends IPanelCell>) iPanelCellClass);
                        ItemStack itemStack = item.getDefaultInstance();
                        if (iPanelCellClass == TinyBlock.class || iPanelCellClass == TransparentBlock.class) {
                            CompoundNBT cellDataNBT = cellsNBT.getCompound(key).getCompound("data");
                            if (cellDataNBT.contains("made_from_namespace")) {
                                CompoundNBT madeFromTag = new CompoundNBT();
                                madeFromTag.putString("namespace", cellDataNBT.getString("made_from_namespace"));
                                madeFromTag.putString("path", cellDataNBT.getString("made_from_path"));
                                CompoundNBT itemTag = new CompoundNBT();
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

    private static boolean playerHasSufficientComponents(List<ItemStack> itemStacks, PlayerEntity player)
    {
        for (ItemStack itemStack : itemStacks)
        {
            int count = 0;
            for(ItemStack invStack : player.inventory.items)
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
     * @param nbt CompoundNBT to be cleaned up - usually acquired untrusted source such as a json file or network
     * @return cleaned up NBT with any irrelevant date removed. NULL if no valid data found.
     */
    @Nullable
    public static CompoundNBT cleanUpBlueprintNBT(CompoundNBT nbt)
    {
        if (nbt.contains("blueprint"))
        {
            CompoundNBT blueprintNBT = nbt.getCompound("blueprint");
            if (blueprintNBT.contains("cells"))
            {
                CompoundNBT newCellsNBT = new CompoundNBT();

                CompoundNBT cellsNBT = blueprintNBT.getCompound("cells");
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

                CompoundNBT newNBT = new CompoundNBT();
                CompoundNBT newBlueprintNBT = new CompoundNBT();
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
