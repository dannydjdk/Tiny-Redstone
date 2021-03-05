package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.*;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.dannyandson.tinyredstone.items.PanelItem;
import com.dannyandson.tinyredstone.items.RedstoneWrench;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TinyRedstone.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TinyRedstone.MODID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, TinyRedstone.MODID);

    //called from main mod constructor
    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //called at FMLCommonSetupEvent in ModSetup
    public static void registerPanelCells(){
        TinyRedstone.registerPanelCell(RedstoneDust.class,TINY_REDSTONE_ITEM.get());
        TinyRedstone.registerPanelCell(Repeater.class, TINY_REPEATER.get());
        TinyRedstone.registerPanelCell(Torch.class,TINY_REDSTONE_TORCH.get());
        TinyRedstone.registerPanelCell(Comparator.class,TINY_COMPARATOR.get());
        TinyRedstone.registerPanelCell(RedstoneBlock.class,TINY_REDSTONE_BLOCK.get());
    }

    public static final RegistryObject<PanelBlock> REDSTONE_PANEL_BLOCK = BLOCKS.register("redstone_panel", PanelBlock::new);
    public static final RegistryObject<TileEntityType<PanelTile>> REDSTONE_PANEL_TILE =
            TILES.register("redstone_panel", () -> TileEntityType.Builder.create(PanelTile::new, REDSTONE_PANEL_BLOCK.get()).build(null));
    public static final RegistryObject<Item> REDSTONE_PANEL_ITEM = ITEMS.register("redstone_panel",PanelItem::new);

    public static final RegistryObject<Item> TINY_REDSTONE_ITEM = ITEMS.register("tiny_redstone",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_TORCH = ITEMS.register("tiny_redstone_torch",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REPEATER = ITEMS.register("tiny_repeater",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_BLOCK = ITEMS.register("tiny_redstone_block",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_COMPARATOR = ITEMS.register("tiny_comparator",PanelCellItem::new);

    public static final RegistryObject<RedstoneWrench> REDSTONE_WRENCH = ITEMS.register("redstone_wrench", RedstoneWrench::new);

}
