package dev.rosenoire.pipeline.common.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.WorldView;

@SuppressWarnings("unused")
public interface PipelineUtil {
    static boolean isContainer(WorldView world, BlockPos pos) {
        return world.getBlockState(pos).hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory;
    }

    static void insertStackInInventory(Inventory inventory, int index, ItemStack stack, int count) {
        if (!stack.isEmpty()) {
            ItemStack itemStack = inventory.getStack(index);
            int itemCount = Math.min(count, stack.getCount());

            if (itemCount > 0) {
                if (itemStack.isEmpty()) {
                    inventory.setStack(index, stack.split(itemCount));
                    return;
                }

                if (ItemStack.areItemsAndComponentsEqual(itemStack, stack)) {
                    stack.decrement(itemCount);
                    itemStack.increment(itemCount);
                    inventory.setStack(index, itemStack);
                }
            }
        }
    }

    static VoxelShape cube(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        float minX = (x) / 16f;
        float minY = (y) / 16f;
        float minZ = (z) / 16f;

        float maxX = (x + sizeX) / 16f;
        float maxY = (y + sizeY) / 16f;
        float maxZ = (z + sizeZ) / 16f;

        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    static VoxelShape addTo(VoxelShape shape, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        return VoxelShapes.combine(shape, cube(x, y, z, sizeX, sizeY, sizeZ), BooleanBiFunction.OR);
    }
}
