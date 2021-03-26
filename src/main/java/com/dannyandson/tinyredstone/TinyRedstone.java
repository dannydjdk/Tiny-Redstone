package com.dannyandson.tinyredstone;

import com.dannyandson.tinyredstone.blocks.IPanelCell;
import com.dannyandson.tinyredstone.blocks.IPanelCover;
import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.items.PanelCellItem;
import com.dannyandson.tinyredstone.setup.ClientSetup;
import com.dannyandson.tinyredstone.setup.ModSetup;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(TinyRedstone.MODID)
public class TinyRedstone {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tinyredstone";


    public TinyRedstone() {

        Registration.register();
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        if(FMLEnvironment.dist.isClient())
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);


        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //load configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    }

    public static void registerPanelCell(Class<? extends IPanelCell> iPanelCellClass, Item correspondingItem)
    {
        PanelBlock.registerPanelCell(iPanelCellClass,correspondingItem);
    }

    public static void registerPanelCover(Class<? extends IPanelCover> iPanelCoverClass, Item correspondingItem)
    {
        PanelBlock.registerPanelCover(iPanelCoverClass,correspondingItem);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Binding Renderer for Redstone Panel tile entity.", Registration.REDSTONE_PANEL_BLOCK.get());
        ClientRegistry.bindTileEntityRenderer(Registration.REDSTONE_PANEL_TILE.get(), PanelTileRenderer::new);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        //allow creative players to remove cells by left clicking with wrench or cell item
        if (event.getPlayer().isCreative() &&
                event.getWorld().getBlockState(event.getPos()).getBlock() instanceof PanelBlock &&
                (
                    event.getPlayer().getHeldItemMainhand().getItem()==Registration.REDSTONE_WRENCH.get() ||
                            event.getPlayer().getHeldItemMainhand().getItem() instanceof PanelCellItem
                )) {
                BlockState blockState = event.getWorld().getBlockState(event.getPos());
                PanelBlock panelBlock = (PanelBlock)blockState.getBlock();
                panelBlock.onBlockClicked(blockState,event.getWorld(),event.getPos(), event.getPlayer());
                event.setCanceled(true);
        }
    }

}
