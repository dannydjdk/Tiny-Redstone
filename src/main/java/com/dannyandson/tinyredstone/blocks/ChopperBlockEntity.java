package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.gui.ChopperItemHandler;
import com.dannyandson.tinyredstone.gui.ChopperMenu;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandler;


public class ChopperBlockEntity extends RandomizableContainerBlockEntity {

    private NonNullList<ItemStack> items;
    private ResultContainer resultContainer = new ResultContainer();
    private ChopperMenu chopperMenu;

    // Fix for 1.21: LazyOptional wrapper is gone. Keep just the handler itself.
    // The capability is now registered externally via RegisterCapabilitiesEvent in TinyRedstone.java.
    private final ChopperItemHandler itemHandler = createHandler();

    private String itemType = "Tiny Block";

    public ChopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistration.CUTTER_BLOCK_ENTITY.get(), pos, state);
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
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        // TODO: ItemStack serialization changed in 26.1.
        // ItemStack.parseOptional/saveOptional removed. Need to use codec or SNBT approach.
        // For now, store item IDs as strings. Full item NBT serialization needs codec-based approach.
        String inputItemId = input.getStringOr("input_item", "");
        int inputCount = input.getIntOr("input_count", 0);
        if (!inputItemId.isEmpty() && inputCount > 0) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(inputItemId));
            if (item != null) {
                this.items.set(0, new ItemStack(item, inputCount));
            }
        }
        String outputItemId = input.getStringOr("output_item", "");
        int outputCount = input.getIntOr("output_count", 0);
        if (!outputItemId.isEmpty() && outputCount > 0) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(outputItemId));
            if (item != null) {
                this.resultContainer.setItem(0, new ItemStack(item, outputCount));
            }
        }
        this.itemType = input.getStringOr("output_type", "");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        // TODO: Full ItemStack serialization needs codec approach for components/NBT.
        // For now, store item ID + count as primitives.
        ItemStack inputStack = this.items.get(0);
        if (!inputStack.isEmpty()) {
            output.putString("input_item", BuiltInRegistries.ITEM.getKey(inputStack.getItem()).toString());
            output.putInt("input_count", inputStack.getCount());
        }
        ItemStack outputStack = resultContainer.getItem(0);
        if (!outputStack.isEmpty()) {
            output.putString("output_item", BuiltInRegistries.ITEM.getKey(outputStack.getItem()).toString());
            output.putInt("output_count", outputStack.getCount());
        }
        output.putString("output_type", itemType);
    }

    private ChopperItemHandler createHandler() {
        return new ChopperItemHandler(this);
    }

    /**
     * Fix for 1.21: expose the handler via a public getter so it can be returned
     * from the RegisterCapabilitiesEvent registration in TinyRedstone.java.
     * The old getCapability() override on BlockEntity is gone entirely.
     */
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    // Fix for 1.21: setRemoved() no longer needs to invalidate a LazyOptional.
    // NeoForge's capability cache is automatically invalidated when the block entity is removed.
    // The override is no longer needed unless you have other cleanup to do here.

    // 1.21.5: Container dropping moved from Block#onRemove to BlockEntity#preRemoveSideEffects
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) {
            net.minecraft.world.Containers.dropContents(this.level, pos, this);
            this.level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.preRemoveSideEffects(pos, state);
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
        if (this.chopperMenu != null)
            this.chopperMenu.slotsChanged(null);
    }
}