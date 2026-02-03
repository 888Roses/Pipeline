package dev.rosenoire.pipeline.common.block;

import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CopperPipeBlockEntity extends BlockEntity {
    public CopperPipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.COPPER_PIPE, blockPos, blockState);
    }
}
