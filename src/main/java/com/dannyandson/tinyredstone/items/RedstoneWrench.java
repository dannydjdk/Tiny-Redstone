package com.dannyandson.tinyredstone.items;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.Nullable;
import java.util.List;

public class RedstoneWrench extends Item {
    public RedstoneWrench(Item.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide()) {
            Player player = context.getPlayer();
            InteractionHand hand = context.getHand();
            BlockPos pos = context.getClickedPos();

            if (player != null && player.isCrouching()) {
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof PanelBlock) {
                    // Fix: BlockState.use() removed in 1.21 - use useWithoutItem() instead
                    return state.useWithoutItem(world, player,
                            new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    // Fix: appendHoverText signature changed in 1.21 - Level param removed, use Item.TooltipContext
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags)
    {
        list.add(Component.translatable("message.item.redstone_wrench"));
    }

    public boolean canPlayerBreakBlockWhileHolding(BlockState state, Level worldIn, BlockPos pos, Player player) {
        return !(state.getBlock() instanceof PanelBlock);
    }

    public BlockHitResult getHitResult(Level world, Player player)
    {
        return Item.getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);
    }
}
