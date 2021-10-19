package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.ClientBinding;
import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.*;
import com.dannyandson.tinyredstone.blocks.panelcovers.DarkCover;
import com.dannyandson.tinyredstone.blocks.panelcovers.LightCover;
import com.dannyandson.tinyredstone.gui.ChopperScreen;
import com.dannyandson.tinyredstone.gui.ToolbarOverlay;
import com.dannyandson.tinyredstone.items.PanelItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TinyRedstone.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(final FMLClientSetupEvent event)
    {
        PanelTileRenderer.register();
        Minecraft.getInstance().getBlockColors().register(new PanelTileColor(), Registration.REDSTONE_PANEL_BLOCK.get());
        Minecraft.getInstance().getItemColors().register(new PanelItemColor(),Registration.REDSTONE_PANEL_ITEM.get());
        ClientBinding.registerKeyBindings();
        ScreenManager.register(Registration.CUTTER_MENU_TYPE.get(), ChopperScreen::new);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
            return;
        }

        event.addSprite(PanelTileRenderer.TEXTURE);
        event.addSprite(PanelTileRenderer.TEXTURE_CRASHED);

        event.addSprite(Repeater.TEXTURE_REPEATER_ON);
        event.addSprite(Repeater.TEXTURE_REPEATER_OFF);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_OFF);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT_ON);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT_OFF);
        event.addSprite(Torch.TEXTURE_TORCH_ON);
        event.addSprite(Torch.TEXTURE_TORCH_OFF);
        event.addSprite(Torch.TEXTURE_TORCH_TOP_ON);
        event.addSprite(Torch.TEXTURE_TORCH_TOP_OFF);
        event.addSprite(Piston.TEXTURE_PISTON_SIDE_TOP);
        event.addSprite(Piston.TEXTURE_PISTON_SIDE_BOTTOM);
        event.addSprite(SuperRepeater.TEXTURE_SUPER_REPEATER_ON);
        event.addSprite(SuperRepeater.TEXTURE_SUPER_REPEATER_OFF);
        event.addSprite(Lever.TEXTURE_LEVER);
        event.addSprite(Lever.TEXTURE_LEVER_TOP);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_OFF);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_ON);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_SUBTRACT_OFF);
        event.addSprite(Comparator.TEXTURE_Comparator_SUBTRACT_ON);
        event.addSprite(Comparator.TEXTURE_Comparator_SUBTRACT_ON);
        event.addSprite(TransparentBlock.TEXTURE_TRANSPARENT_BLOCK);
        event.addSprite(GhostRenderer.TEXTURE_REDSTONE_DUST_SEGMENT_GHOST);

        event.addSprite(LightCover.TEXTURE_LIGHT_COVER);
        event.addSprite(DarkCover.TEXTURE_DARK_COVER);

        event.addSprite(ToolbarOverlay.TEXTURE_ROTATION_LOCK);
    }


}