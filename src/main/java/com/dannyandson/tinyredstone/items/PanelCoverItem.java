package com.dannyandson.tinyredstone.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class PanelCoverItem extends PanelCellItem{

    @Override
    public InteractionResult useOn(UseOnContext context) {
       return InteractionResult.PASS;
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
                            /**
                             * @return This Item's renderer, or the default instance if it does not have
                             * one.
                             */
                            @Override
                            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                                return new PanelCoverItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                            }
                        }
        );
    }


    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag()) {
            String thisName = super.getName(stack).getString();
            String fromBlockName = null;
            CompoundTag itemNBT = stack.getTag();
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                fromBlockName = (new TranslatableComponent("block." + madeFromTag.getString("namespace") + "." + madeFromTag.getString("path"))).getString();
            }
            return Component.nullToEmpty(thisName + " (" + fromBlockName + ")");
        }

        return super.getName(stack);
    }


}
