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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistration {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TinyRedstone.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TinyRedstone.MODID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TinyRedstone.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, TinyRedstone.MODID);
    private static final DeferredRegister<CreativeModeTab> TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinyRedstone.MODID);

    //called from main mod constructor
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        TAB.register(modEventBus);
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

    // --- Blocks ---
    public static final DeferredBlock<PanelBlock> REDSTONE_PANEL_BLOCK = BLOCKS.registerBlock("redstone_panel",
            PanelBlock::new,
            props -> props.sound(SoundType.STONE).strength(2.0f).forceSolidOn());

    public static final DeferredBlock<ChopperBlock> CUTTER_BLOCK = BLOCKS.registerBlock("block_chopper",
            ChopperBlock::new,
            props -> props.sound(SoundType.STONE).strength(2.0f));

    // --- Block Entity Types (1.21.2: use constructor instead of Builder) ---
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PanelTile>> REDSTONE_PANEL_TILE =
            TILES.register("redstone_panel", () -> new BlockEntityType<>(PanelTile::new, REDSTONE_PANEL_BLOCK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChopperBlockEntity>> CUTTER_BLOCK_ENTITY =
            TILES.register("block_chopper", () -> new BlockEntityType<>(ChopperBlockEntity::new, CUTTER_BLOCK.get()));

    // --- Block Items ---
    public static final DeferredItem<Item> REDSTONE_PANEL_ITEM = ITEMS.registerItem("redstone_panel", props -> new PanelItem(props.useBlockDescriptionPrefix()));
    public static final DeferredItem<Item> CUTTER_BLOCK_ITEM = ITEMS.registerItem("block_chopper", props -> new ChopperBlockItem(props.useBlockDescriptionPrefix()));

    // --- Menu ---
    public static final DeferredHolder<MenuType<?>, MenuType<ChopperMenu>> CUTTER_MENU_TYPE = MENU_TYPES.register("block_chopper", () -> new MenuType<>(ChopperMenu::createChopperMenu, FeatureFlags.DEFAULT_FLAGS));

    // --- Panel Cell Items ---
    public static final DeferredItem<Item> TINY_SOLID_BLOCK = ITEMS.registerItem("tiny_solid_block", TinyBlockItem::new);
    public static final DeferredItem<Item> TINY_TRANSPARENT_BLOCK = ITEMS.registerItem("tiny_transparent_block", TinyBlockItem::new);

    public static final DeferredItem<Item> TINY_REDSTONE_ITEM = ITEMS.registerItem("tiny_redstone", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_REDSTONE_TORCH = ITEMS.registerItem("tiny_redstone_torch", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_REPEATER = ITEMS.registerItem("tiny_repeater", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_REDSTONE_BLOCK = ITEMS.registerItem("tiny_redstone_block", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_COMPARATOR = ITEMS.registerItem("tiny_comparator", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_PISTON = ITEMS.registerItem("tiny_piston", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_STICKY_PISTON = ITEMS.registerItem("tiny_sticky_piston", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_REDSTONE_LAMP = ITEMS.registerItem("tiny_redstone_lamp", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_BUTTON = ITEMS.registerItem("tiny_button", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_STONE_BUTTON = ITEMS.registerItem("tiny_stone_button", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_OBSERVER = ITEMS.registerItem("tiny_observer", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_SUPER_REPEATER = ITEMS.registerItem("tiny_super_repeater", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_LEVER = ITEMS.registerItem("tiny_lever", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_REDSTONE_BRIDGE = ITEMS.registerItem("tiny_redstone_bridge", PanelCellItem::new);
    public static final DeferredItem<Item> TINY_NOTE_BLOCK = ITEMS.registerItem("tiny_note_block", PanelCellItem::new);

    public static final DeferredItem<Item> BLUEPRINT = ITEMS.registerItem("blueprint", Blueprint::new);

    public static final DeferredItem<Item> SILICON = ITEMS.registerSimpleItem("silicon");
    public static final DeferredItem<Item> SILICON_COMPOUND = ITEMS.registerSimpleItem("silicon_compound");

    public static final DeferredItem<RedstoneWrench> REDSTONE_WRENCH = ITEMS.registerItem("redstone_wrench",
            props -> new RedstoneWrench(props.stacksTo(1)));
    public static final DeferredItem<RedstoneWrench> TINY_COLOR_SELECTOR = ITEMS.registerItem("tiny_color_selector",
            props -> new TinyColorSelector(props.stacksTo(1)));

    public static final DeferredItem<Item> PANEL_COVER_DARK = ITEMS.registerItem("dark_panel_cover", PanelCoverItem::new);
    public static final DeferredItem<Item> PANEL_COVER_LIGHT = ITEMS.registerItem("light_panel_cover", PanelCoverItem::new);
    public static final DeferredItem<Item> PANEL_COVER_TRIM = ITEMS.registerItem("trim_panel_cover", PanelCoverItem::new);

    public static final CodecTinyBlockOverrides TINY_BLOCK_OVERRIDES = new CodecTinyBlockOverrides("tiny_block_overrides", TinyBlockData.CODEC);

    public static final BooleanProperty HAS_PANEL_BASE = BooleanProperty.create("has_panel_base");


    public static DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TAB.register("tinyredstonetab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tinyredstone"))
                    .icon(() -> new ItemStack(ModRegistration.REDSTONE_PANEL_BLOCK.get()))
                    .displayItems((parameters,output) -> ModRegistration.ITEMS.getEntries().forEach(o -> output.accept(o.get())))
                    .build());
}