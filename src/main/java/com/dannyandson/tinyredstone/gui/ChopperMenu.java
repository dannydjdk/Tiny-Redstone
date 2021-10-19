package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.ValidTinyBlockCacheSync;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class ChopperMenu extends Container {
    public static ChopperMenu createChopperMenu(int containerId, PlayerInventory playerInventory) {
        return createChopperMenu(containerId, playerInventory, new Inventory(1));
    }

    public static ChopperMenu createChopperMenu(int containerId, PlayerInventory playerInventory, IInventory inventory) {
        return new ChopperMenu(containerId, playerInventory, inventory);
    }

    public static List<Material> solidMaterials = Arrays.asList(
            Material.WOOD,
            Material.STONE,
            Material.METAL,
            Material.ICE_SOLID,
            Material.CLAY,
            Material.SAND,
            Material.DIRT,
            Material.GRASS,
            Material.SNOW,
            Material.WOOL,
            Material.NETHER_WOOD,
            Material.SPONGE
    );

    public static List<Material> transparentMaterials = Arrays.asList(
            Material.GLASS,
            Material.ICE
    );

    private final IInventory container;
    private final CraftResultInventory resultContainer;
    private Slot inputSlot;
    private Slot outputSlot;

    protected ChopperMenu(int containerId, PlayerInventory playerInventory, IInventory container) {
        super(Registration.CUTTER_MENU_TYPE.get(), containerId);
        checkContainerSize(container, 1);

        this.container = container;
        if (container instanceof ChopperBlockEntity ) {
            ChopperBlockEntity chopperBlockEntity = (ChopperBlockEntity)container;
            chopperBlockEntity.setCutterMenu(this);
            this.resultContainer = chopperBlockEntity.getResultContainer();
        } else
            this.resultContainer = new CraftResultInventory();

        container.startOpen(playerInventory.player);

        int leftCol = 12;
        int ySize = 184;

        this.inputSlot = this.addSlot(new Slot(container, 0, 30, 44));
        this.outputSlot = this.addSlot(new Slot(resultContainer, 1, 12 + (7 * 18), 44) {

            public boolean mayPlace(ItemStack p_40362_) {
                return false;
            }

            public ItemStack onTake(PlayerEntity player, ItemStack itemStack) {
                itemStack.onCraftedBy(player.level, player, itemStack.getCount());
                ChopperMenu.this.inputSlot.remove(1);
                return super.onTake(player, itemStack);
            }
        });

        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
                this.addSlot(new Slot(playerInventory, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, ySize - (4 - playerInvRow) * 18 - 10));
            }

        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(playerInventory, hotbarSlot, leftCol + hotbarSlot * 18, ySize - 24));
        }

    }

    void setupResultSlot() {
        ItemStack inputStack = this.inputSlot.getItem();
        ItemStack outputStack = ItemStack.EMPTY;
        Item item = inputStack.getItem();

        if (inputStack.getItem() instanceof BlockItem) {
            Block inputBlock = ((BlockItem)item).getBlock();
            BlockState inputBlockState = inputBlock.defaultBlockState();
            Material material = inputBlock.defaultBlockState().getMaterial();

            if (container instanceof ChopperBlockEntity) {
                ChopperBlockEntity chopperBlockEntity = (ChopperBlockEntity) container;
                boolean isFullBlock = inputBlockState.isCollisionShapeFullBlock(chopperBlockEntity.getLevel(), chopperBlockEntity.getBlockPos());
                if (isFullBlock && !inputBlockState.isSignalSource() && !inputBlockState.hasTileEntity()) {
                    ResourceLocation inputRegistryName = inputBlock.getRegistryName();
                    if (!Registration.TINY_BLOCK_OVERRIDES.hasUsableTexture(inputRegistryName)){
                        if (!chopperBlockEntity.getLevel().isClientSide)
                            ModNetworkHandler.sendToNearestClient(new ValidTinyBlockCacheSync(chopperBlockEntity.getBlockPos(),inputRegistryName),chopperBlockEntity.getLevel(),chopperBlockEntity.getBlockPos());
                    }
                    else if (inputBlock.getRegistryName()!=null){
                        CompoundNBT madeFromTag = new CompoundNBT();
                        madeFromTag.putString("namespace", inputBlock.getRegistryName().getNamespace());
                        madeFromTag.putString("path", inputBlock.getRegistryName().getPath());
                        if (solidMaterials.contains(material)) {
                            outputStack = Registration.TINY_SOLID_BLOCK.get().getDefaultInstance();
                            outputStack.setCount(8);
                            if(!inputBlock.getRegistryName().toString().equals("minecraft:white_wool"))
                                outputStack.addTagElement("made_from", madeFromTag);
                        } else if (transparentMaterials.contains(material)) {
                            outputStack = Registration.TINY_TRANSPARENT_BLOCK.get().getDefaultInstance();
                            outputStack.setCount(8);
                            if(!inputBlock.getRegistryName().toString().equals("minecraft:glass"))
                                outputStack.addTagElement("made_from", madeFromTag);
                        }
                    }
                }
            }
        }

        this.outputSlot.set(outputStack);
        this.broadcastChanges();
    }

    @Override
    public void slotsChanged(IInventory container) {
        ChopperMenu.this.setupResultSlot();
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if(index==1)
                    this.outputSlot.onTake(player,itemstack);
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            setupResultSlot();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.container.stillValid(player);
    }

}
