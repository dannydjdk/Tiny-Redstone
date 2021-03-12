package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.dannyandson.tinyredstone.setup.ModSetup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RedstoneWrench extends Item {
    public RedstoneWrench() {
        super(new Item.Properties().group(ModSetup.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (!world.isRemote) {
            PlayerEntity player = context.getPlayer();
            Hand hand = context.getHand();
            BlockPos pos = context.getPos();

            if (player != null && player.isSneaking()) {
                // Make sure the block get activated if it is a BaseBlockNew
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof PanelBlock) {
                    return state.onBlockActivated(world, player, hand, new BlockRayTraceResult(context.getHitVec(), context.getFace(), pos, context.isInside()));
                }
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public  void  addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flags)
    {
        list.add(new TranslationTextComponent("message.item.redstone_wrench"));
    }

    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        return !(state.getBlock() instanceof PanelBlock);
    }

    public BlockRayTraceResult getBlockRayTraceResult(World world, PlayerEntity player)
    {
        return Item.rayTrace(world,player,RayTraceContext.FluidMode.ANY);
    }
}
