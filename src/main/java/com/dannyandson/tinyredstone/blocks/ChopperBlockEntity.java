package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.gui.ChopperItemHandler;
import com.dannyandson.tinyredstone.gui.ChopperMenu;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChopperBlockEntity extends LockableLootTileEntity {

    private NonNullList<ItemStack> items;
    private CraftResultInventory resultContainer = new CraftResultInventory();
    private ChopperMenu chopperMenu;

    private final ChopperItemHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private String itemType = "Tiny Block";


    public ChopperBlockEntity() {
        super(Registration.CUTTER_BLOCK_ENTITY.get());
        this.items = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
    }

    public void setCutterMenu(ChopperMenu chopperMenu) {
        this.chopperMenu = chopperMenu;
    }

    @Override
    public void setChanged() {
        if (this.chopperMenu != null && !level.isClientSide)
            this.chopperMenu.slotsChanged(this);
        super.setChanged();
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.tinyredstone.block_chopper");
    }

    @Nullable
    @Override
    public Container createMenu(int containerId, PlayerInventory playerInventory) {
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

    public CraftResultInventory getResultContainer() {
        return resultContainer;
    }

    @Override
    public void load(BlockState blockState, CompoundNBT compoundTag) {
        super.load(blockState, compoundTag);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        this.items.set(0, ItemStack.of(compoundTag.getCompound("input_container")));
        this.resultContainer.setItem(0, ItemStack.of(compoundTag.getCompound("output_container")));
        this.itemType = compoundTag.getString("output_type");
    }

    @Override
    public CompoundNBT save(CompoundNBT compoundTag) {
        super.save(compoundTag);

        compoundTag.put("input_container", this.items.get(0).serializeNBT());
        compoundTag.put("output_container", resultContainer.getItem(0).serializeNBT());
        compoundTag.putString("output_type", itemType);

        return compoundTag;
    }

    private ChopperItemHandler createHandler() {
        return new ChopperItemHandler(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        handler.invalidate();
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
        if(this.chopperMenu != null)
            this.chopperMenu.slotsChanged(null);
    }
}
