package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.setup.Registration;
import com.dannyandson.tinyredstone.util.ItemStackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class TinyBlockItem extends PanelCellItem {

    @Override
    public Component getName(ItemStack stack) {
        String thisName = super.getName(stack).getString();
        String fromBlockName = null;
        if (ItemStackHelper.getCustomTag(stack) != null) {
            CompoundTag itemNBT = ItemStackHelper.getCustomTag(stack);
            CompoundTag madeFromTag = itemNBT.getCompound("made_from");
            if (madeFromTag.contains("namespace")) {
                fromBlockName = (Component.translatable("block." + madeFromTag.getString("namespace") + "." + madeFromTag.getString("path"))).getString();
            }
        }
        if (fromBlockName==null){
            if (stack.getItem()== Registration.TINY_SOLID_BLOCK.get()){
                    fromBlockName = Component.translatable("block.minecraft.white_wool").getString();
            } else if (stack.getItem()== Registration.TINY_TRANSPARENT_BLOCK.get()){
                fromBlockName = Component.translatable("block.minecraft.glass").getString();
            }
        }
        return Component.nullToEmpty(thisName + " (" + fromBlockName + ")");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
                            @Override
                            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                                return new TinyBlockItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                            }
                        }
        );
    }

}
