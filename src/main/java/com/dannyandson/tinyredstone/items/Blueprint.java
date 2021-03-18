package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blueprint extends Item {

    public Blueprint() {
        super(new Item.Properties().group(ModSetup.ITEM_GROUP));
    }

    @Override
    public  void  addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags)
    {
        if (stack.getTag() !=null && stack.getTag().contains("blueprint"))
        {
            list.add(new TranslationTextComponent("message.item.blueprint.full"));
            Map<Item,Integer> blueprintItems = getRequiredComponents(stack.getChildTag("blueprint"));
            for (Item item : blueprintItems.keySet())
            {
                list.add(ITextComponent.getTextComponentOrEmpty(item.getName().getString() + " : " + blueprintItems.get(item)));
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
    public ActionResultType onItemUse(ItemUseContext context) {
        TileEntity te = context.getWorld().getTileEntity(context.getPos());
        if (te instanceof PanelTile)
        {
            PanelTile panelTile = (PanelTile)te;
            if (context.getItem().getTag() !=null && context.getItem().getTag().contains("blueprint"))
            {
                PlayerEntity player = context.getPlayer();
                if (panelTile.cells.size()==0 && player!=null)
                {
                    CompoundNBT blueprintNBT = context.getItem().getChildTag("blueprint");
                    Map<Item,Integer> items = getRequiredComponents(blueprintNBT);
                    if (player.isCreative() || playerHasSufficientComponents(items, player)) {
                        panelTile.loadCellsFromNBT(blueprintNBT);
                        if (!player.isCreative())
                        {
                            for (Item item : items.keySet())
                            {
                                int itemsToRemove = items.get(item);
                                for(ItemStack invStack : player.inventory.mainInventory)
                                {
                                    if (invStack.getItem().equals(item))
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

                context.getItem().setTag(nbt);

            }
        }

        return ActionResultType.SUCCESS;
    }

    //called when item is right clicked in the air
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(worldIn.isRemote)
            BlueprintGUI.open(playerIn.getHeldItem(handIn));
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    private static Map<Item,Integer> getRequiredComponents(CompoundNBT blueprintNBT)
    {
        Map<Item,Integer> items = new HashMap<>();

        if (blueprintNBT.contains("cells"))
        {
            CompoundNBT cellsNBT = blueprintNBT.getCompound("cells");
            for (String key : cellsNBT.keySet())
            {
                try {
                    Class iPanelCellClass = Class.forName(cellsNBT.getCompound(key).getString("class"));
                    if (IPanelCell.class.isAssignableFrom(iPanelCellClass))
                    {
                        Item item = PanelBlock.getPanelCellItemFromClass((Class<? extends IPanelCell>) iPanelCellClass);
                        if (items.containsKey(item))
                            items.put(item,items.get(item)+1);
                        else
                            items.put(item,1);
                    }

                }catch (ClassNotFoundException e)
                {
                    TinyRedstone.LOGGER.error("Class not found exception while attempting to read components from blueprint NBT: " + e.getLocalizedMessage());
                }

            }
        }
        return items;
    }
    private static boolean playerHasSufficientComponents(Map<Item,Integer> items, PlayerEntity player)
    {
        for (Item item : items.keySet())
        {
            ItemStack itemStack = new ItemStack(item);
            int count = 0;
            for(ItemStack invStack : player.inventory.mainInventory)
            {
                if (invStack.getItem().equals(itemStack.getItem()))
                {
                    count+= invStack.getCount();
                }
            }
            if (count<items.get(item))
                return false;
        }
        return true;
    }

}
