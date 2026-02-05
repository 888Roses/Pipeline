package dev.rosenoire.pipeline.common.block;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.math.math;
import net.collectively.geode.types.double3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PipeBlockEntity extends LockableContainerBlockEntity {
    private int itemCyclingIndex;
    private int extractItemDelay;
    private int importItemDelay;

    // TODO: Temporary. Change for fields in the Block class directly so different variants of pipe can decide of their speed, etc.
    public static final int ITEM_DELAY = 1;
    public static final int BATCH_SIZE = 8;

    public PipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.PIPE, blockPos, blockState);
    }

    // region Serialization

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        heldStacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        itemCyclingIndex = view.getInt("itemCyclingIndex", 0);
        extractItemDelay = view.getInt("itemDelay", 0);
        importItemDelay = view.getInt("importItemDelay", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        Inventories.writeData(view, this.heldStacks);
        view.putInt("itemCyclingIndex", itemCyclingIndex);
        view.putInt("itemDelay", extractItemDelay);
        view.putInt("importItemDelay", importItemDelay);
    }

    // endregion

    // region Container

    private DefaultedList<ItemStack> heldStacks = DefaultedList.ofSize(5, ItemStack.EMPTY);

    @Override
    protected Text getContainerName() {
        return Text.literal("Pipe");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return heldStacks;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> heldStacks) {
        this.heldStacks = heldStacks;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;
    }

    @Override
    public int size() {
        return heldStacks.size();
    }

    // endregion

    // region Behavior

    public void serverTick(World world, BlockPos position, BlockState blockState) {
        ConnectionData connectionData = ConnectionData.get(world, position, blockState);

        if (connectionData.connections().length == 0) {
            return;
        }

        if (importItemDelay > 0) {
            importItemDelay--;
            markDirty();
        } else {
            retrieveItemsFromSourceContainer(world, position, blockState, connectionData);
        }

        if (extractItemDelay > 0) {
            extractItemDelay--;
            markDirty();
            return;
        }

        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);

            if (stack.isEmpty()) {
                continue;
            }

            extractItemDelay += ITEM_DELAY;
            markDirty();

            int connectionIndex = itemCyclingIndex % connectionData.connections().length;
            itemCyclingIndex++;
            markDirty();

            InventoryWithPosition connection = connectionData.connections()[connectionIndex];
            Inventory connectionInventory = getInventory(connection.inventory(), world, connection.position(), connection.getBlockState(world));

            // This should only happen when an error occurred so it doesn't matter to break out of the loop here.
            if (connectionInventory == null) {
                break;
            }

            Inventory sourceInventory = getInventory(this, world, position, blockState);
            boolean hasExtracted = false;

            final int batchSize = math.min(stack.getCount(), BATCH_SIZE);
            Direction direction = Direction.fromVector(connection.position().subtract(position), Direction.NORTH);

            for (int j = 0; j < batchSize; j++) {
                int v = Math.min(1, stack.getCount());
                ItemStack extractedStack = stack.copyWithCount(v);
                if (extractItems(sourceInventory, connectionInventory, extractedStack, i, direction.getOpposite())) {
                    stack.decrement(v);
                    hasExtracted = true;
                }

                if (stack.isEmpty()) {
                    break;
                }
            }

            if (hasExtracted) {
                Draw.text("Extracted!", new double3(position).add(0.5, 1, 0.5));
                break;
            }
        }
    }

    private void retrieveItemsFromSourceContainer(World world, BlockPos position, BlockState blockState, ConnectionData connectionData) {
        if (connectionData.source() == null) {
            return;
        }

        BlockPos sourcePosition = connectionData.source().position();
        BlockState sourceBlockState = connectionData.source().getBlockState(world);
        Inventory srcInv = getInventory(connectionData.source().inventory(), world, sourcePosition, sourceBlockState);
        Inventory dstInv = getInventory(this, world, position, blockState);

        if (srcInv == null) {
            return;
        }

        for (int i = 0; i < srcInv.size(); i++) {
            ItemStack stack = srcInv.getStack(i);

            if (stack.isEmpty()) {
                continue;
            }

            importItemDelay += ITEM_DELAY;
            markDirty();

            boolean hasExtracted = false;

            final int maxSize = math.min(stack.getCount(), BATCH_SIZE);
            for (int ignored = 0; ignored < maxSize; ignored++) {
                int v = Math.min(1, stack.getCount());
                ItemStack extractedStack = stack.copyWithCount(v);
                if (extractItems(srcInv, dstInv, extractedStack, i, PipeBlock.getForward(blockState))) {
                    stack.decrement(v);
                    hasExtracted = true;
                }

                if (stack.isEmpty()) {
                    break;
                }
            }

            if (hasExtracted) {
                break;
            }
        }
    }

    /// @return whether it was successful or not
    private boolean extractItems(Inventory sourceInventory, Inventory destinationInventory, ItemStack sourceStack, int sourceSlotIndex, Direction direction) {
        if (sourceInventory == null || destinationInventory == null) {
            return false;
        }

        // Iterating over the slots in this inventory.
        for (int destinationSlotIndex = 0; destinationSlotIndex < destinationInventory.size(); destinationSlotIndex++) {
            ItemStack destinationStack = destinationInventory.getStack(destinationSlotIndex);

            // Check if we can extract from the container's slot for blocks like Jukeboxes and chiseled bookshelves.
            if (!sourceInventory.canTransferTo(destinationInventory, sourceSlotIndex, sourceStack)) {
                continue;
            }

            // Makes sure we can actually input in it.
            if (sourceInventory instanceof SidedInventory sidedInventory) {
                boolean isAvailableSlot = false;
                for (int availableSlot : sidedInventory.getAvailableSlots(direction)) {
                    if (availableSlot == sourceSlotIndex) {
                        isAvailableSlot = true;
                        break;
                    }
                }

                boolean canExtract = sidedInventory.canExtract(sourceSlotIndex, sourceStack, direction);

                if (sourceInventory != this && Pipeline.DEBUG_PIPE_PERMS) {
                    Draw.text(
                            "(" + direction + ") " + sourceSlotIndex + " Is Available " + isAvailableSlot + " Can Extract: " + canExtract,
                            new double3(pos)
                                    .add(0.5, 1 + destinationSlotIndex * 0.25, 0.5)
                                    .add(direction.getAxis().isVertical()
                                            ? Direction.NORTH.getDoubleVector().multiply(0.5)
                                            : new Vec3d(0, 0, 0)
                                    ),
                            0xff55ff55
                    );
                }

                if (!isAvailableSlot || !canExtract) {
                    continue;
                }
            }

            if (destinationInventory instanceof SidedInventory sidedInventory) {
                boolean isAvailableSlot = false;
                for (int availableSlot : sidedInventory.getAvailableSlots(direction)) {
                    if (availableSlot == destinationSlotIndex) {
                        isAvailableSlot = true;
                        break;
                    }
                }

                boolean canInsert = sidedInventory.canInsert(destinationSlotIndex, sourceStack, direction);

                if (sourceInventory != this && Pipeline.DEBUG_PIPE_PERMS) {
                    var fuelRegistry = world == null ? null : world.getFuelRegistry();

                    Draw.text(
                            "Slot " + destinationSlotIndex + " (" + direction + ") "
                                    + "Is Available " + isAvailableSlot
                                    + " Can Insert: " + canInsert
                                    + " Stack: " + sourceStack
                                    + " Is Fuel: " + (fuelRegistry == null ? "null" : String.valueOf(fuelRegistry.isFuel(sourceStack))),
                            new double3(pos)
                                    .add(0.5, 2 + destinationSlotIndex * 0.25, 0.5)
                                    .add(direction.getAxis().isVertical()
                                            ? Direction.NORTH.getDoubleVector().multiply(0.5)
                                            : new Vec3d(0, 0, 0)
                                    ),
                            0xffffff55
                    );
                }

                if (!isAvailableSlot || !canInsert) {
                    continue;
                }
            }

            // If the destination stack is empty, we can just set its content to the item stack in the source container.
            if (destinationStack.isEmpty()) {
                // Important to copy otherwise its count will also be set to 0.
                destinationInventory.setStack(destinationSlotIndex, sourceStack.copy());
                sourceStack.setCount(0);
                return true;
            }

            // If the stacks are the same but not full we can stack them.
            if (ItemStack.areItemsAndComponentsEqual(sourceStack, destinationStack) && destinationStack.getCount() < destinationStack.getMaxCount()) {
                int destinationCount = destinationStack.getCount() + sourceStack.getCount();
                int remainderCount = destinationCount - destinationStack.getMaxCount();
                destinationCount = math.min(destinationCount, destinationStack.getMaxCount());

                destinationStack.setCount(destinationCount);
                sourceStack.setCount(remainderCount);

                if (remainderCount <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public static @Nullable Inventory getInventory(Inventory container, World world, BlockPos position, BlockState blockState) {
        // If the block is a chest, we know it has a double inventory so we want to get it.
        if (blockState.getBlock() instanceof ChestBlock chestBlock) {
            container = ChestBlock.getInventory(chestBlock, blockState, world, position, true);

            // The container should never be null but in the case it is, we don't want to keep going.
            if (container == null) {
                Draw.text(
                        "Chest block container is null!",
                        new double3(position).add(0.5, 1.5, 0.5),
                        0xffff5555
                );

                return null;
            }
        }

        return container;
    }

    // endregion

    // region Structures

    public record ConnectionData(@Nullable InventoryWithPosition source, @NotNull InventoryWithPosition[] connections) {
        public static ConnectionData get(World world, BlockPos position, BlockState blockState) {
            List<InventoryWithPosition> connections = new ArrayList<>();
            InventoryWithPosition source = null;

            Direction forwardDirection = PipeBlock.getForward(blockState);
            for (Direction direction : Direction.values()) {
                if (!PipeBlock.canConnectToBlockInDirection(world, position, direction, forwardDirection)) {
                    continue;
                }

                BlockPos positionInDirection = position.offset(direction);
                Optional<InventoryWithPosition> inventory = InventoryWithPosition.get(world, positionInDirection);

                if (source == null && direction == forwardDirection.getOpposite() && inventory
                        .map(x -> !(world.getBlockEntity(x.position) instanceof PipeBlockEntity))
                        .orElse(true)
                ) {
                    source = inventory.orElse(null);
                    continue;
                }

                if (direction == forwardDirection.getOpposite()) {
                    continue;
                }

                inventory.map(connections::add);
            }

            return new ConnectionData(source, connections.toArray(InventoryWithPosition[]::new));
        }
    }

    /// An [Inventory] coupled with a [BlockPos].
    public record InventoryWithPosition(Inventory inventory, BlockPos position) {
        /// Attempts to retrieve and create an [InventoryWithPosition] based on the block at the given [BlockPos].
        /// Returns an [Optional] containing the created inventory with position, or empty if none were found at that block pos.
        public static Optional<InventoryWithPosition> get(World world, BlockPos position) {
            BlockState blockState = world.getBlockState(position);
            Block block = blockState.getBlock();

            if (block instanceof InventoryProvider inventoryProvider) {
                return Optional.of(new InventoryWithPosition(inventoryProvider.getInventory(blockState, world, position), position));
            }

            if (blockState.hasBlockEntity() && world.getBlockEntity(position) instanceof Inventory inventory) {
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, position, true);
                }

                return Optional.of(new InventoryWithPosition(inventory, position));
            }

            return Optional.empty();
        }

        /// Returns the [BlockState] at the position of this inventory with position.
        public BlockState getBlockState(WorldView world) {
            return world.getBlockState(position);
        }
    }

    // endregion
}