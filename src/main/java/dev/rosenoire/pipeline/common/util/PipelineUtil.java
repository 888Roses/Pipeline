package dev.rosenoire.pipeline.common.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

@SuppressWarnings("unused")
public interface PipelineUtil {
    static boolean isContainer(WorldView world, BlockPos pos) {
        return world.getBlockState(pos).hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory;
    }
}
