package dev.rosenoire.pipeline.common.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

@SuppressWarnings("unused")
public interface PipelineUtil {
    static boolean isContainer(WorldView world, BlockPos pos) {
        return world.getBlockState(pos).hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory;
    }

    static ItemStack insertStackInInventory(Inventory inventory, int index, ItemStack stack, int count) {
        if (!stack.isEmpty()) {
            ItemStack itemStack = inventory.getStack(index);
            int itemCount = Math.min(count, stack.getCount());

            if (itemCount > 0) {
                if (itemStack.isEmpty()) {
                    inventory.setStack(index, stack.split(itemCount));
                    return stack;
                }

                if (ItemStack.areItemsAndComponentsEqual(itemStack, stack)) {
                    stack.decrement(itemCount);
                    itemStack.increment(itemCount);
                    inventory.setStack(index, itemStack);
                }
            }
        }

        return stack;
    }
}
