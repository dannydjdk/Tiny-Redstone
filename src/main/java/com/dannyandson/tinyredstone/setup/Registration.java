package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.*;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.items.*;
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
        TinyRedstone.registerPanelCell(TinyBlock.class, TINY_SOLID_BLOCK.get());
        TinyRedstone.registerPanelCell(Piston.class, TINY_PISTON.get());
        TinyRedstone.registerPanelCell(StickyPiston.class, TINY_STICKY_PISTON.get());
        TinyRedstone.registerPanelCell(RedstoneLamp.class,TINY_REDSTONE_LAMP.get());
        TinyRedstone.registerPanelCell(TransparentBlock.class,TINY_TRANSPARENT_BLOCK.get());
        TinyRedstone.registerPanelCell(Button.class,TINY_BUTTON.get());
        TinyRedstone.registerPanelCell(StoneButton.class,TINY_STONE_BUTTON.get());
        TinyRedstone.registerPanelCell(Observer.class,TINY_OBSERVER.get());
        TinyRedstone.registerPanelCell(SuperRepeater.class,TINY_SUPER_REPEATER.get());
        TinyRedstone.registerPanelCell(Lever.class,TINY_LEVER.get());
        TinyRedstone.registerPanelCell(RedstoneBridge.class,TINY_REDSTONE_BRIDGE.get());
        TinyRedstone.registerPanelCell(NoteBlock.class,TINY_NOTE_BLOCK.get());

        TinyRedstone.registerPanelCover(DarkCover.class,PANEL_COVER_DARK.get());
        TinyRedstone.registerPanelCover(LightCover.class,PANEL_COVER_LIGHT.get());
    }

    public static final RegistryObject<PanelBlock> REDSTONE_PANEL_BLOCK = BLOCKS.register("redstone_panel", PanelBlock::new);
    public static final RegistryObject<TileEntityType<PanelTile>> REDSTONE_PANEL_TILE =
            TILES.register("redstone_panel", () -> TileEntityType.Builder.of(PanelTile::new, REDSTONE_PANEL_BLOCK.get()).build(null));
    public static final RegistryObject<Item> REDSTONE_PANEL_ITEM = ITEMS.register("redstone_panel",PanelItem::new);

    public static final RegistryObject<Item> TINY_REDSTONE_ITEM = ITEMS.register("tiny_redstone",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_TORCH = ITEMS.register("tiny_redstone_torch",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REPEATER = ITEMS.register("tiny_repeater",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_BLOCK = ITEMS.register("tiny_redstone_block",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_COMPARATOR = ITEMS.register("tiny_comparator",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_SOLID_BLOCK = ITEMS.register("tiny_solid_block",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_PISTON = ITEMS.register("tiny_piston",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_STICKY_PISTON = ITEMS.register("tiny_sticky_piston",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_LAMP = ITEMS.register("tiny_redstone_lamp",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_TRANSPARENT_BLOCK = ITEMS.register("tiny_transparent_block",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_BUTTON = ITEMS.register("tiny_button",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_STONE_BUTTON = ITEMS.register("tiny_stone_button",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_OBSERVER = ITEMS.register("tiny_observer",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_SUPER_REPEATER = ITEMS.register("tiny_super_repeater",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_LEVER = ITEMS.register("tiny_lever",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_BRIDGE = ITEMS.register("tiny_redstone_bridge",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_NOTE_BLOCK = ITEMS.register("tiny_note_block",PanelCellItem::new);

    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint", Blueprint::new);

    public static final RegistryObject<Item> SILICON = ITEMS.register("silicon",()->new Item(new Item.Properties()
            .tab(ModSetup.ITEM_GROUP)
    ));
    public static final RegistryObject<Item> SILICON_COMPOUND = ITEMS.register("silicon_compound",()->new Item(new Item.Properties()
            .tab(ModSetup.ITEM_GROUP)));

    public static final RegistryObject<RedstoneWrench> REDSTONE_WRENCH = ITEMS.register("redstone_wrench", RedstoneWrench::new);
    public static final RegistryObject<RedstoneWrench> TINY_COLOR_SELECTOR = ITEMS.register("tiny_color_selector", TinyColorSelector::new);

    public static final RegistryObject<Item> PANEL_COVER_DARK = ITEMS.register("dark_panel_cover",PanelCellItem::new);
    public static final RegistryObject<Item> PANEL_COVER_LIGHT = ITEMS.register("light_panel_cover",PanelCellItem::new);

}
