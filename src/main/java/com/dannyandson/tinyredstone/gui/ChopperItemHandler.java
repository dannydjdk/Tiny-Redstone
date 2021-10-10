package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class ChopperItemHandler implements IItemHandler {
    private ChopperBlockEntity innerHandler;

    public ChopperItemHandler(ChopperBlockEntity innerHandler){
        this.innerHandler=innerHandler;
    }

    @Override
    public int getSlots() {
        return 2;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return (slot==0)?innerHandler.getItem(slot):innerHandler.getResultContainer().getItem(0);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (slot>0 || !isItemValid(slot, stack))
            return stack;

        ItemStack existing = getStackInSlot(slot);

        int limit = stack.getMaxStackSize();

        if (!existing.isEmpty())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate)
        {
            if (existing.isEmpty())
            {
                innerHandler.setItem(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            }
            else
            {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            innerHandler.setChanged();
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || slot != 1)
            return ItemStack.EMPTY;

        ItemStack existing = getStackInSlot(slot);

        if (existing.isEmpty() || amount < existing.getCount())
            return ItemStack.EMPTY;

        if (!simulate) {
            innerHandler.getResultContainer().setItem(0, ItemStack.EMPTY);
            innerHandler.getItem(0).setCount(innerHandler.getItem(0).getCount()-1);
            innerHandler.setChanged();
            return existing;
        } else {
            return existing.copy();
        }

    }

    @Override
    public int getSlotLimit(int slot) {
        return getStackInSlot(slot).getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

}
