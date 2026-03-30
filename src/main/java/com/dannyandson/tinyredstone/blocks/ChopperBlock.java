package com.dannyandson.tinyredstone.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.Nullable;

public class ChopperBlock extends BaseEntityBlock {

    // Fix for 1.21: BaseEntityBlock now requires codec() to be implemented.
    // A simple no-data codec is sufficient for blocks that don't serialize extra properties.
    public static final MapCodec<ChopperBlock> CODEC = simpleCodec(ChopperBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ChopperBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChopperBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    // 1.21.5: onRemove split into BlockEntity#preRemoveSideEffects (for dropping contents)
    // and Block#affectNeighborsAfterRemoval (for neighbor updates only).
    // Container dropping now happens in ChopperBlockEntity#preRemoveSideEffects.


    @SuppressWarnings("deprecation")
    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos blockPos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(blockState, level, blockPos, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    // Fix for 1.21: use() is renamed to useWithoutItem() / the interaction pipeline changed.
    // Use useWithoutItem for right-click with empty hand (no item context).
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            MenuProvider menuProvider = this.getMenuProvider(blockState, level, blockPos);

            if (menuProvider != null) {
                player.openMenu(menuProvider);
            }

            return InteractionResult.CONSUME;
        }
    }
}