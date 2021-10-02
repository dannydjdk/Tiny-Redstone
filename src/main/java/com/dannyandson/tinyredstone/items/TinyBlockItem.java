package com.dannyandson.tinyredstone.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class TinyBlockItem extends PanelCellItem {

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag itemNBT = stack.getTag();
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                String thisName = super.getName(stack).getString();
                String fromBlockName = (new TranslatableComponent("block." + madeFromTag.getString("namespace") + "." + madeFromTag.getString("path"))).getString();
                return Component.nullToEmpty(thisName + " (" + fromBlockName + ")");
            }
        }
        return super.getName(stack);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
                            @Override
                            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                                return new TinyBlockItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                            }
                        }
        );
    }

}
