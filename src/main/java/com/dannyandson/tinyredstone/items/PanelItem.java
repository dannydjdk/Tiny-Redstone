package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.ModSetup;
import com.dannyandson.tinyredstone.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class PanelItem extends BlockItem {



    public PanelItem()
    {
        super(Registration.REDSTONE_PANEL_BLOCK.get(),new Item.Properties()
                .tab(ModSetup.ITEM_GROUP)
        );

    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
                            /**
                             * @return This Item's renderer, or the default instance if it does not have
                             * one.
                             */
                            @Override
                            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                                return new PanelItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                            }
                        }
        );
    }

    @Override
    public  void  appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags)
    {
        list.add(Component.translatable("message.item.redstone_panel"));
    }


}
