package com.dannyandson.tinyredstone.blocks;

import com.dannyandson.tinyredstone.gui.ChopperMenu;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


public class ChopperBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

    private static final int[] INPUT_SLOT_ONLY = new int[] { 0 };

    private NonNullList<ItemStack> items;
    private ResultContainer resultContainer = new ResultContainer();
    private ChopperMenu chopperMenu;

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
        refreshOutputSlot();  // keep output state correct without a menu open
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

    // --- WorldlyContainer: restrict what vanilla automation can do via the
    //     Container fallback path. Output/extract access for automation goes
    //     through the ChopperItemHandler capability; Container is insert-only. ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        return INPUT_SLOT_ONLY;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        // don't pull items from the input slot.
        return false;
    }

    // --- output refresh ---

    /**
     * Display/committed decision for the output slot:
     *   empty            → fill with computed display
     *   count >= yield   → DISPLAY: replace or clear as recipe dictates
     *   count <  yield   → COMMITTED: leave alone
     * Idempotent. Invoked from setChanged so it runs without a menu open.
     */
    public void refreshOutputSlot() {
        // Skip during chunk load — would clear a just-loaded display.
        if (this.level == null) return;

        ItemStack currentOutput = this.resultContainer.getItem(0);
        ItemStack computedOutput = computeRecipeOutput();

        if (currentOutput.isEmpty()) {
            if (!computedOutput.isEmpty()) {
                this.resultContainer.setItem(0, computedOutput);
            }
        } else {
            int outputYield = fullYieldForItem(currentOutput);
            if (currentOutput.getCount() >= outputYield) {
                if (computedOutput.isEmpty()) {
                    this.resultContainer.setItem(0, ItemStack.EMPTY);
                } else if (!ItemStack.isSameItemSameComponents(currentOutput, computedOutput)) {
                    this.resultContainer.setItem(0, computedOutput);
                }
                // else: display already matches; no change.
            }
            // else: committed, leave alone.
        }
    }

    /**
     * Compute the fresh-display output for the current input + itemType.
     * Returns EMPTY for non-BlockItem, non-full-block, signal source,
     * has-block-entity, or override-disabled inputs.
     */
    private ItemStack computeRecipeOutput() {
        if (this.level == null) return ItemStack.EMPTY;

        ItemStack inputStack = this.getItem(0);
        if (!(inputStack.getItem() instanceof BlockItem blockItem)) return ItemStack.EMPTY;

        Block inputBlock = blockItem.getBlock();
        BlockState inputBlockState = inputBlock.defaultBlockState();

        boolean isFullBlock = inputBlockState.isCollisionShapeFullBlock(this.level, this.getBlockPos());
        if (!isFullBlock || inputBlockState.isSignalSource() || inputBlockState.hasBlockEntity()) {
            return ItemStack.EMPTY;
        }

        Identifier inputRegistryName = BuiltInRegistries.BLOCK.getKey(inputBlock);
        if (inputRegistryName == null
                || ModRegistration.TINY_BLOCK_OVERRIDES.isDisabled(inputRegistryName)) {
            return ItemStack.EMPTY;
        }

        CompoundTag madeFromTag = new CompoundTag();
        madeFromTag.putString("namespace", inputRegistryName.getNamespace());
        madeFromTag.putString("path", inputRegistryName.getPath());

        ItemStack output;
        if ("Dark Cover".equals(itemType)) {
            output = ModRegistration.PANEL_COVER_DARK.get().getDefaultInstance();
            output.setCount(2);
            ItemStackHelper.addTagElement(output, "made_from", madeFromTag);
        } else if ("Light Cover".equals(itemType)) {
            output = ModRegistration.PANEL_COVER_LIGHT.get().getDefaultInstance();
            output.setCount(2);
            ItemStackHelper.addTagElement(output, "made_from", madeFromTag);
        } else {
            // GlassBlock class removed in 1.21 — use block tag.
            boolean isGlass = inputBlockState.is(BlockTags.IMPERMEABLE);
            if (isGlass) {
                output = ModRegistration.TINY_TRANSPARENT_BLOCK.get().getDefaultInstance();
                output.setCount(8);
                if (!inputRegistryName.toString().equals("minecraft:glass"))
                    ItemStackHelper.addTagElement(output, "made_from", madeFromTag);
            } else {
                output = ModRegistration.TINY_SOLID_BLOCK.get().getDefaultInstance();
                output.setCount(8);
                if (!inputRegistryName.toString().equals("minecraft:white_wool"))
                    ItemStackHelper.addTagElement(output, "made_from", madeFromTag);
            }
        }
        return output;
    }

    /** Recipe yield: 2 for panel covers, 8 for tiny blocks. */
    private static int fullYieldForItem(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        if (item == ModRegistration.PANEL_COVER_DARK.get()
                || item == ModRegistration.PANEL_COVER_LIGHT.get()) {
            return 2;
        }
        return 8;
    }

    // --- persistence ---

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