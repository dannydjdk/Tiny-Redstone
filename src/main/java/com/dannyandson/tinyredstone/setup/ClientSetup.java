package com.dannyandson.tinyredstone.setup;

import com.dannyandson.tinyredstone.TinyRedstone;
import com.dannyandson.tinyredstone.blocks.PanelTileColor;
import com.dannyandson.tinyredstone.blocks.PanelTileRenderer;
import com.dannyandson.tinyredstone.blocks.panelcells.Comparator;
import com.dannyandson.tinyredstone.blocks.panelcells.RedstoneDust;
import com.dannyandson.tinyredstone.blocks.panelcells.Repeater;
import com.dannyandson.tinyredstone.blocks.panelcells.Torch;
import com.dannyandson.tinyredstone.items.PanelItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
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
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }

        event.addSprite(PanelTileRenderer.TEXTURE);
        event.addSprite(Repeater.TEXTURE_REPEATER_ON);
        event.addSprite(Repeater.TEXTURE_REPEATER_OFF);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_OFF);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT);
        event.addSprite(RedstoneDust.TEXTURE_REDSTONE_DUST_SEGMENT_OFF);
        event.addSprite(Torch.TEXTURE_TORCH_ON);
        event.addSprite(Torch.TEXTURE_TORCH_OFF);

        event.addSprite(Comparator.TEXTURE_COMPARATOR_OFF);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_ON);
        event.addSprite(Comparator.TEXTURE_COMPARATOR_SUBTRACT_OFF);
        event.addSprite(Comparator.TEXTURE_Comparator_SUBTRACT_ON);
    }


}