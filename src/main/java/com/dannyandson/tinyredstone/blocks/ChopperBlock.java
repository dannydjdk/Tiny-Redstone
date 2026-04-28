package com.dannyandson.tinyredstone.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class ChopperBlock extends BaseEntityBlock implements WorldlyContainerHolder {

    // Fix for 1.21: BaseEntityBlock now requires codec() to be implemented.
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

    // 1.21.5: container dropping moved to ChopperBlockEntity#preRemoveSideEffects.

    @SuppressWarnings("deprecation")
    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos blockPos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(blockState, level, blockPos, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    // Fix for 1.21: use() renamed to useWithoutItem().
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

    /**
     * Returning null forces vanilla's getBlockContainer to skip the BE-as-Container
     * branch and fall through to the capability path, where ChopperItemHandler's
     * display/committed rules apply. Vanilla's only caller null-checks the return.
     */
    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        return null;
    }
}