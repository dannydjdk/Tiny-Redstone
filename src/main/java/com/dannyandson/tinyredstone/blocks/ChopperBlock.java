package com.dannyandson.tinyredstone.blocks;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChopperBlock extends Block {
    public ChopperBlock() {
        super(
                Properties.of(Material.STONE)
                        .sound(SoundType.STONE)
                        .strength(2.0f)
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ChopperBlockEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState blockState, World level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (!blockState.is(newState.getBlock())) {
            TileEntity blockentity = level.getBlockEntity(blockPos);
            if (blockentity instanceof IInventory) {
                InventoryHelper.dropContents(level, blockPos, (IInventory) blockentity);
                level.updateNeighbourForOutputSignal(blockPos, this);
            }

            super.onRemove(blockState, level, blockPos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public INamedContainerProvider getMenuProvider(BlockState blockState, World world, BlockPos blockPos) {
        TileEntity te = world.getBlockEntity(blockPos);
        if (te instanceof ChopperBlockEntity)
            return (ChopperBlockEntity)te;
        return super.getMenuProvider(blockState, world, blockPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public ActionResultType use(BlockState blockState, World level, BlockPos blockPos, PlayerEntity player, Hand interactionHand, BlockRayTraceResult blockHitResult) {
        if (level.isClientSide) {
            return ActionResultType.SUCCESS;
        } else {
            INamedContainerProvider menuProvider = this.getMenuProvider(blockState, level, blockPos);

            if (menuProvider != null) {
                player.openMenu(menuProvider);
                //TODO stats?
            }

            return ActionResultType.CONSUME;
        }
    }
}
