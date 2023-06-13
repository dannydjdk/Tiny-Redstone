package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.ChopperBlock;
import com.dannyandson.tinyredstone.blocks.ChopperBlockEntity;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.blocks.panelcells.*;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.TrimCover;
import com.dannyandson.tinyredstone.codec.CodecTinyBlockOverrides;
import com.dannyandson.tinyredstone.codec.TinyBlockData;
import com.dannyandson.tinyredstone.gui.ChopperMenu;
import com.dannyandson.tinyredstone.items.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TinyRedstone.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TinyRedstone.MODID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TinyRedstone.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES,TinyRedstone.MODID);
    private static final DeferredRegister<CreativeModeTab> TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinyRedstone.MODID);

    //called from main mod constructor
    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        TAB.register(FMLJavaModLoadingContext.get().getModEventBus());
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
        TinyRedstone.registerPanelCover(TrimCover.class,PANEL_COVER_TRIM.get());
    }

    public static final RegistryObject<PanelBlock> REDSTONE_PANEL_BLOCK = BLOCKS.register("redstone_panel", PanelBlock::new);
    public static final RegistryObject<BlockEntityType<PanelTile>> REDSTONE_PANEL_TILE =
            TILES.register("redstone_panel", () -> BlockEntityType.Builder.of(PanelTile::new, REDSTONE_PANEL_BLOCK.get()).build(null));
    public static final RegistryObject<Item> REDSTONE_PANEL_ITEM = ITEMS.register("redstone_panel",PanelItem::new);

    public static final RegistryObject<ChopperBlock> CUTTER_BLOCK = BLOCKS.register("block_chopper", ChopperBlock::new);
    public static final RegistryObject<BlockEntityType<ChopperBlockEntity>> CUTTER_BLOCK_ENTITY =
            TILES.register("block_chopper", ()->BlockEntityType.Builder.of(ChopperBlockEntity::new,CUTTER_BLOCK.get()).build(null));
    public static final RegistryObject<Item> CUTTER_BLOCK_ITEM = ITEMS.register("block_chopper", ChopperBlockItem::new);
    public static final RegistryObject<MenuType<ChopperMenu>> CUTTER_MENU_TYPE = MENU_TYPES.register("block_chopper", () -> new MenuType<>(ChopperMenu::createChopperMenu, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistryObject<Item> TINY_SOLID_BLOCK = ITEMS.register("tiny_solid_block",TinyBlockItem::new);
    public static final RegistryObject<Item> TINY_TRANSPARENT_BLOCK = ITEMS.register("tiny_transparent_block",TinyBlockItem::new);

    public static final RegistryObject<Item> TINY_REDSTONE_ITEM = ITEMS.register("tiny_redstone",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_TORCH = ITEMS.register("tiny_redstone_torch",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REPEATER = ITEMS.register("tiny_repeater",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_BLOCK = ITEMS.register("tiny_redstone_block",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_COMPARATOR = ITEMS.register("tiny_comparator",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_PISTON = ITEMS.register("tiny_piston",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_STICKY_PISTON = ITEMS.register("tiny_sticky_piston",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_LAMP = ITEMS.register("tiny_redstone_lamp",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_BUTTON = ITEMS.register("tiny_button",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_STONE_BUTTON = ITEMS.register("tiny_stone_button",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_OBSERVER = ITEMS.register("tiny_observer",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_SUPER_REPEATER = ITEMS.register("tiny_super_repeater",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_LEVER = ITEMS.register("tiny_lever",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_REDSTONE_BRIDGE = ITEMS.register("tiny_redstone_bridge",PanelCellItem::new);
    public static final RegistryObject<Item> TINY_NOTE_BLOCK = ITEMS.register("tiny_note_block",PanelCellItem::new);

    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint", Blueprint::new);

    public static final RegistryObject<Item> SILICON = ITEMS.register("silicon",()->new Item(new Item.Properties()
    ));
    public static final RegistryObject<Item> SILICON_COMPOUND = ITEMS.register("silicon_compound",()->new Item(new Item.Properties()
    ));

    public static final RegistryObject<RedstoneWrench> REDSTONE_WRENCH = ITEMS.register("redstone_wrench", RedstoneWrench::new);
    public static final RegistryObject<RedstoneWrench> TINY_COLOR_SELECTOR = ITEMS.register("tiny_color_selector", TinyColorSelector::new);

    public static final RegistryObject<Item> PANEL_COVER_DARK = ITEMS.register("dark_panel_cover",PanelCoverItem::new);
    public static final RegistryObject<Item> PANEL_COVER_LIGHT = ITEMS.register("light_panel_cover",PanelCoverItem::new);
    public static final RegistryObject<Item> PANEL_COVER_TRIM = ITEMS.register("trim_panel_cover",PanelCellItem::new);

    public static final CodecTinyBlockOverrides TINY_BLOCK_OVERRIDES = new CodecTinyBlockOverrides("tiny_block_overrides", TinyBlockData.CODEC);

    public static final BooleanProperty HAS_PANEL_BASE = BooleanProperty.create("has_panel_base");


    public static RegistryObject<CreativeModeTab> CREATIVE_TAB = TAB.register("tinyredstonetab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tinyredstone"))
                    .icon(() -> new ItemStack(Registration.REDSTONE_PANEL_BLOCK.get()))
                    .displayItems((parameters,output) -> Registration.ITEMS.getEntries().forEach(o -> output.accept(o.get())))
                    .build());
}
