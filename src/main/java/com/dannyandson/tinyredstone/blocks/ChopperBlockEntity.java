package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.gui.ChopperMenu;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChopperBlockEntity extends RandomizableContainerBlockEntity {

    private NonNullList<ItemStack> items;
    private ResultContainer resultContainer = new ResultContainer();
    private ChopperMenu chopperMenu;


    public ChopperBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.CUTTER_BLOCK_ENTITY.get(), pos, state);
        this.items = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
    }

    public void setCutterMenu(ChopperMenu chopperMenu) {
        this.chopperMenu = chopperMenu;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.chopperMenu != null)
            this.chopperMenu.slotsChanged(this);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.tinyredstone.block_chopper");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return ChopperMenu.createChopperMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public ResultContainer getResultContainer() {
        return resultContainer;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        this.items.set(0, ItemStack.of(compoundTag.getCompound("input_container")));
        this.resultContainer.setItem(0, ItemStack.of(compoundTag.getCompound("output_container")));

    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);

        compoundTag.put("input_container", this.items.get(0).serializeNBT());
        compoundTag.put("output_container", resultContainer.getItem(0).serializeNBT());

        return compoundTag;
    }
}
