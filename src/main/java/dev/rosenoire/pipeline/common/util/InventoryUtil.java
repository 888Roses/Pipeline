package dev.rosenoire.pipeline.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class InventoryUtil {
    private static final int[][] AVAILABLE_SLOTS_CACHE = new int[54][];

    public static @Nullable Inventory getBlockInventoryAt(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof InventoryProvider) {
            return ((InventoryProvider)block).getInventory(state, world, pos);
        } else if (state.hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory inventory) {
            if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                inventory = ChestBlock.getInventory((ChestBlock)block, state, world, pos, true);
            }

            return inventory;
        } else {
            return null;
        }
    }

    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory sidedInventory && side != null) {
            int[] is = sidedInventory.getAvailableSlots(side);

            for (int i = 0; i < is.length && !stack.isEmpty(); i++) {
                stack = transfer(from, to, stack, is[i], side);
            }
        } else {
            int j = to.size();

            for (int i = 0; i < j && !stack.isEmpty(); i++) {
                stack = transfer(from, to, stack, i, side);
            }
        }

        return stack;
    }

    public static boolean extract(Inventory fromInventory, Inventory toInventory, int slot, Direction side) {
        ItemStack itemStack = fromInventory.getStack(slot);
        if (!itemStack.isEmpty() && canExtract(toInventory, fromInventory, itemStack, slot, side)) {
            int i = itemStack.getCount();
            ItemStack itemStack2 = transfer(fromInventory, toInventory, fromInventory.removeStack(slot, 1), null);
            if (itemStack2.isEmpty()) {
                fromInventory.markDirty();
                return true;
            }

            itemStack.setCount(i);
            if (i == 1) {
                fromInventory.setStack(slot, itemStack);
            }
        }

        return false;
    }

    private static ItemStack transfer(@Nullable Inventory fromInventory,
                                      Inventory toInventory,
                                      ItemStack itemStack,
                                      int slotIndex,
                                      @Nullable Direction side) {

        ItemStack itemStackInSlot = toInventory.getStack(slotIndex);
        if (!canInsert(toInventory, itemStack, slotIndex, side)) {
            return itemStack;
        }

        if (itemStackInSlot.isEmpty()) {
            toInventory.setStack(slotIndex, itemStack);
            itemStack = ItemStack.EMPTY;
        } else if (canMergeItems(itemStackInSlot, itemStack)) {
            int i = itemStack.getMaxCount() - itemStackInSlot.getCount();
            int j = Math.min(itemStack.getCount(), i);
            itemStack.decrement(j);
            itemStackInSlot.increment(j);
        }

        return itemStack;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        return first.getCount() <= first.getMaxCount() && ItemStack.areItemsAndComponentsEqual(first, second);
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        return inventory.isValid(slot, stack) && !(inventory instanceof SidedInventory sidedInventory && !sidedInventory.canInsert(slot, stack, side));
    }

    private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
        return fromInventory.canTransferTo(hopperInventory, slot, stack) && !(fromInventory instanceof SidedInventory sidedInventory && !sidedInventory.canExtract(slot, stack, facing));
    }

    public static int[] getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory sidedInventory) {
            return sidedInventory.getAvailableSlots(side);
        } else {
            int i = inventory.size();
            if (i < AVAILABLE_SLOTS_CACHE.length) {
                int[] is = AVAILABLE_SLOTS_CACHE[i];
                if (is != null) {
                    return is;
                } else {
                    int[] js = indexArray(i);
                    AVAILABLE_SLOTS_CACHE[i] = js;
                    return js;
                }
            } else {
                return indexArray(i);
            }
        }
    }

    private static int[] indexArray(int size) {
        int[] is = new int[size];
        int i = 0;

        while (i < is.length) {
            is[i] = i++;
        }

        return is;
    }

    public static boolean isInventoryFull(Inventory inventory, Direction direction) {
        int[] is = getAvailableSlots(inventory, direction);

        for (int i : is) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.getCount() < itemStack.getMaxCount()) {
                return false;
            }
        }

        return true;
    }
}
