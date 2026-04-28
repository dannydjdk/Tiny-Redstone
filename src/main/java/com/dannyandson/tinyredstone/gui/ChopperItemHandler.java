package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.setup.ModRegistration;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 2-slot capability view for the Chopper: slot 0 = input (insert-only),
 * slot 1 = output (extract-only). Output has two states: DISPLAY (count
 * == yield, no input consumed yet) and COMMITTED (count &lt; yield, input
 * already paid). The first extract from a display consumes 1 input, or is
 * refused if input is empty; subsequent extracts just drain.
 */
public class ChopperItemHandler implements ResourceHandler<ItemResource> {

    private static final int INPUT = 0;
    private static final int OUTPUT = 1;

    private final ChopperBlockEntity chopper;
    private final ResourceHandler<ItemResource> inputHandler;
    private final ResourceHandler<ItemResource> outputHandler;
    private final ResourceHandler<ItemResource> combined;

    public ChopperItemHandler(ChopperBlockEntity chopper) {
        this.chopper = chopper;
        this.inputHandler = VanillaContainerWrapper.of(chopper);
        this.outputHandler = VanillaContainerWrapper.of(chopper.getResultContainer());
        this.combined = new CombinedResourceHandler<>(inputHandler, outputHandler);
    }

    @Override public int  size()                         { return combined.size(); }
    @Override public ItemResource getResource(int index) { return combined.getResource(index); }
    @Override public int  getAmountAsInt(int index)      { return combined.getAmountAsInt(index); }
    @Override public long getAmountAsLong(int index)     { return combined.getAmountAsLong(index); }

    @Override
    public int getCapacityAsInt(int index, ItemResource resource) {
        return combined.getCapacityAsInt(index, resource);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return combined.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return index == INPUT && combined.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (index != INPUT) return 0;
        return combined.insert(index, resource, amount, tx);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext tx) {
        return inputHandler.insert(resource, amount, tx);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (index != OUTPUT) return 0;

        int before = outputHandler.getAmountAsInt(0);
        if (before == 0) return 0;

        int yield = fullYieldForOutputItem();
        boolean wasFreshDisplay = (yield > 0 && before == yield);

        // Refuse a display extract with no input to pay with.
        if (wasFreshDisplay && inputHandler.getResource(0).isEmpty()) {
            return 0;
        }

        // Output before input: input-consume triggers setChanged → refresh,
        // and refresh would clear a still-full display if input were gone first.
        int extracted = outputHandler.extract(0, resource, amount, tx);

        if (extracted > 0 && wasFreshDisplay) {
            ItemResource inputResource = inputHandler.getResource(0);
            if (!inputResource.isEmpty()) {
                inputHandler.extract(0, inputResource, 1, tx);
            }
        }

        if (extracted > 0) {
            // Refills next batch's display via BE.refreshOutputSlot, and dirties the chunk.
            chopper.setChanged();
        }

        return extracted;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext tx) {
        return extract(OUTPUT, resource, amount, tx);
    }

    /** Yield of the item currently in the output slot (0 if empty). Item-derived, not itemType-derived. */
    private int fullYieldForOutputItem() {
        ItemResource outputResource = outputHandler.getResource(0);
        if (outputResource.isEmpty()) return 0;
        Item item = outputResource.toStack().getItem();
        if (item == ModRegistration.PANEL_COVER_DARK.get()
                || item == ModRegistration.PANEL_COVER_LIGHT.get()) {
            return 2;
        }
        return 8;
    }
}