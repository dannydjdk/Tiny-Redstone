package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.gui.ChopperItemHandler;
import com.dannyandson.tinyredstone.gui.ChopperMenu;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
// Fix for 1.21: removed Capability, ForgeCapabilities, LazyOptional imports entirely.
// IItemHandler is kept as it's still used.
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ChopperBlockEntity extends RandomizableContainerBlockEntity {

    private NonNullList<ItemStack> items;
    private ResultContainer resultContainer = new ResultContainer();
    private ChopperMenu chopperMenu;

    // Fix for 1.21: LazyOptional wrapper is gone. Keep just the handler itself.
    // The capability is now registered externally via RegisterCapabilitiesEvent in TinyRedstone.java.
    private final ChopperItemHandler itemHandler = createHandler();

    private String itemType = "Tiny Block";

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
        return Component.translatable("block.tinyredstone.block_chopper");
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
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.items.set(0, ItemStack.parseOptional(registries, compoundTag.getCompound("input_container")));
        this.resultContainer.setItem(0, ItemStack.parseOptional(registries, compoundTag.getCompound("output_container")));
        this.itemType = compoundTag.getString("output_type");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        compoundTag.put("input_container", this.items.get(0).saveOptional(registries));
        compoundTag.put("output_container", resultContainer.getItem(0).saveOptional(registries));
        compoundTag.putString("output_type", itemType);
    }

    private ChopperItemHandler createHandler() {
        return new ChopperItemHandler(this);
    }

    /**
     * Fix for 1.21: expose the handler via a public getter so it can be returned
     * from the RegisterCapabilitiesEvent registration in TinyRedstone.java.
     * The old getCapability() override on BlockEntity is gone entirely.
     */
    @Nonnull
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    // Fix for 1.21: setRemoved() no longer needs to invalidate a LazyOptional.
    // NeoForge's capability cache is automatically invalidated when the block entity is removed.
    // The override is no longer needed unless you have other cleanup to do here.

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
        if (this.chopperMenu != null)
            this.chopperMenu.slotsChanged(null);
    }
}
