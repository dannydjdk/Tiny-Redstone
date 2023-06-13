package com.dannyandson.tinyredstone.gui;

import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.network.ModNetworkHandler;
import com.dannyandson.tinyredstone.network.PushChopperOutputType;
import com.dannyandson.tinyredstone.network.ValidTinyBlockCacheSync;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class ChopperMenu extends AbstractContainerMenu {
    public static ChopperMenu createChopperMenu(int containerId, Inventory playerInventory) {
        return createChopperMenu(containerId, playerInventory, new SimpleContainer(1));
    }

    public static ChopperMenu createChopperMenu(int containerId, Inventory playerInventory, Container inventory) {
        return new ChopperMenu(containerId, playerInventory, inventory);
    }

/*    public static List<Material> solidMaterials = Arrays.asList(
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
            Blocks.GLASS
            Material.GLASS,
            Material.ICE
    );
*/
    private final Container container;
    private final ResultContainer resultContainer;
    private Slot inputSlot;
    private Slot outputSlot;
    private String itemType = "Tiny Block";

    protected ChopperMenu(int containerId, Inventory playerInventory, Container container) {
        super(Registration.CUTTER_MENU_TYPE.get(), containerId);
        checkContainerSize(container, 1);

        this.container = container;
        if (container instanceof ChopperBlockEntity chopperBlockEntity) {
            chopperBlockEntity.setCutterMenu(this);
            chopperBlockEntity.setItemType(itemType);
            this.resultContainer = chopperBlockEntity.getResultContainer();
        } else
            this.resultContainer = new ResultContainer();

        container.startOpen(playerInventory.player);

        int leftCol = 12;
        int ySize = 184;

        this.inputSlot = this.addSlot(new Slot(container, 0, 30, 44));
        this.outputSlot = this.addSlot(new Slot(resultContainer, 1, 12 + (7 * 18), 44) {

            public boolean mayPlace(ItemStack p_40362_) {
                return false;
            }

            public void onTake(Player player, ItemStack itemStack) {
                itemStack.onCraftedBy(player.level(), player, itemStack.getCount());
                ItemStack itemstack = ChopperMenu.this.inputSlot.remove(1);
                if (!itemstack.isEmpty()) {
                    ChopperMenu.this.setupResultSlot();
                }

                super.onTake(player, itemStack);
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

        setupResultSlot();

    }

    void setupResultSlot() {
        if (this.inputSlot != null) {
            ItemStack inputStack = this.inputSlot.getItem();
            ItemStack outputStack = ItemStack.EMPTY;
            Item item = inputStack.getItem();

            if (inputStack.getItem() instanceof BlockItem blockItem) {
                Block inputBlock = blockItem.getBlock();
                BlockState inputBlockState = inputBlock.defaultBlockState();
                //Material material = inputBlock.defaultBlockState().getMaterial();

                if (container instanceof ChopperBlockEntity chopperBlockEntity) {
                    boolean isFullBlock = inputBlockState.isCollisionShapeFullBlock(chopperBlockEntity.getLevel(), chopperBlockEntity.getBlockPos());
                    if (isFullBlock && !inputBlockState.isSignalSource() && !inputBlockState.hasBlockEntity()) {
                        ResourceLocation inputRegistryName = ForgeRegistries.BLOCKS.getKey(inputBlock);
                        if (!Registration.TINY_BLOCK_OVERRIDES.hasUsableTexture(inputRegistryName)) {
                            if (!chopperBlockEntity.getLevel().isClientSide)
                                ModNetworkHandler.sendToNearestClient(new ValidTinyBlockCacheSync(chopperBlockEntity.getBlockPos(), inputRegistryName), chopperBlockEntity.getLevel(), chopperBlockEntity.getBlockPos());
                        } else if (ForgeRegistries.BLOCKS.getKey(inputBlock) != null) {
                            CompoundTag madeFromTag = new CompoundTag();
                            madeFromTag.putString("namespace", ForgeRegistries.BLOCKS.getKey(inputBlock).getNamespace());
                            madeFromTag.putString("path", ForgeRegistries.BLOCKS.getKey(inputBlock).getPath());
                            if (getItemType().equals("Dark Cover")) {
                                outputStack = Registration.PANEL_COVER_DARK.get().getDefaultInstance();
                                outputStack.setCount(2);
                                outputStack.addTagElement("made_from", madeFromTag);
                            } else if (getItemType().equals("Light Cover")) {
                                outputStack = Registration.PANEL_COVER_LIGHT.get().getDefaultInstance();
                                outputStack.setCount(2);
                                outputStack.addTagElement("made_from", madeFromTag);
                            } else {
                                if (inputBlock instanceof GlassBlock) {
                                    outputStack = Registration.TINY_TRANSPARENT_BLOCK.get().getDefaultInstance();
                                    outputStack.setCount(8);
                                    if (!ForgeRegistries.BLOCKS.getKey(inputBlock).toString().equals("minecraft:glass"))
                                        outputStack.addTagElement("made_from", madeFromTag);
                                } else {
                                    outputStack = Registration.TINY_SOLID_BLOCK.get().getDefaultInstance();
                                    outputStack.setCount(8);
                                    if (!ForgeRegistries.BLOCKS.getKey(inputBlock).toString().equals("minecraft:white_wool"))
                                        outputStack.addTagElement("made_from", madeFromTag);
                                }
                            }
                        }
                    }
                }
            }

            this.outputSlot.set(outputStack);
            this.broadcastChanges();
        }
    }


    @Override
    public void slotsChanged(Container container) {
        if (this.inputSlot!=null) {
            ItemStack itemstack = this.inputSlot.getItem();
            ChopperMenu.this.setupResultSlot();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if (index == 1)
                    this.outputSlot.onTake(player, itemstack);
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
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public String getItemType() {
        if (this.container instanceof ChopperBlockEntity)
            return ((ChopperBlockEntity) this.container).getItemType();
        return itemType;
    }

    public void toggleItemType(BlockPos pos) {
        if (itemType == "Tiny Block")
            itemType = "Dark Cover";
        else if (itemType == "Dark Cover")
            itemType = "Light Cover";
        else if (itemType == "Light Cover")
            itemType = "Tiny Block";
        ModNetworkHandler.sendToServer(new PushChopperOutputType(itemType, pos));
    }

}
