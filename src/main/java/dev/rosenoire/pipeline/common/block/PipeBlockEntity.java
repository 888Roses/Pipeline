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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PipeBlockEntity extends LockableContainerBlockEntity {
    private int itemCyclingIndex;

    public PipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.PIPE, blockPos, blockState);
    }

    // region Serialization

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        heldStacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        itemCyclingIndex = view.getInt("itemCyclingIndex", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        Inventories.writeData(view, this.heldStacks);
        view.putInt("itemCyclingIndex", itemCyclingIndex);
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
        drawConnections(world, position, blockState, connectionData);

        if (connectionData.connections().length == 0) {
            return;
        }

        retrieveItemsFromSourceContainer(world, position, blockState, connectionData);

        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);

            while (!stack.isEmpty()) {
                ItemStack previousStack = stack.copy();

                int connectionIndex = itemCyclingIndex % connectionData.connections().length;
                itemCyclingIndex++;

                InventoryWithPosition connection = connectionData.connections()[connectionIndex];
                Inventory connectionInventory = getInventory(connection.inventory(), world, connection.position(), connection.getBlockState(world));

                // This should only happen when an error occurred so it doesn't matter to break out of the loop here.
                if (connectionInventory == null) {
                    break;
                }

                Inventory sourceInventory = getInventory(this, world, position, blockState);
                extractItems(sourceInventory, connectionInventory, PipeBlock.getForward(blockState).getOpposite());

                // If the stack hasn't changed, this means that either it has finished being extracted, or it couldn't be
                // extracted at all, so we want to break out of the while loop.
                if (ItemStack.areEqual(getStack(i), previousStack)) {
                    break;
                }
            }

            // Don't forget to update this pipe's slot.
            setStack(i, stack);
        }
    }

    private void retrieveItemsFromSourceContainer(World world, BlockPos position, BlockState blockState, ConnectionData connectionData) {
        if (connectionData.source() == null) {
            return;
        }

        BlockPos sourcePosition = connectionData.source().position();
        BlockState sourceBlockState = connectionData.source().getBlockState(world);
        Inventory sourceInventoryData = getInventory(connectionData.source().inventory(), world, sourcePosition, sourceBlockState);
        Inventory destinationInventoryData = getInventory(this, world, position, blockState);

        extractItems(sourceInventoryData, destinationInventoryData, PipeBlock.getForward(blockState));
    }

    private void extractItems(Inventory sourceInventory, Inventory destinationInventory, Direction direction) {
        if (sourceInventory == null || destinationInventory == null) {
            return;
        }

        for (int sourceSlotIndex = 0; sourceSlotIndex < sourceInventory.size(); sourceSlotIndex++) {
            // Do not use unavailable slots.
            ItemStack sourceStack = sourceInventory.getStack(sourceSlotIndex);

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

                    if (sourceInventory == this && Pipeline.DEBUG_PIPE_PERMS) {
                        Draw.text(
                                "(" + direction + ") Is Available " + isAvailableSlot + " Can Extract: " + canExtract,
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

                    if (sourceInventory == this && Pipeline.DEBUG_PIPE_PERMS) {
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
                    sourceInventory.setStack(sourceSlotIndex, sourceStack);

                    // Next item in the source container.
                    break;
                }

                // If the stacks are the same stack but not full we can stack them.
                if (ItemStack.areItemsAndComponentsEqual(sourceStack, destinationStack) && destinationStack.getCount() < destinationStack.getMaxCount()) {
                    int destinationCount = sourceStack.getCount() + destinationStack.getCount();
                    int remainderCount = destinationCount - destinationStack.getMaxCount();
                    destinationCount = math.min(destinationCount, destinationStack.getMaxCount());

                    destinationStack.setCount(destinationCount);
                    sourceStack.setCount(remainderCount);

                    destinationInventory.setStack(destinationSlotIndex, destinationStack);
                    sourceInventory.setStack(sourceSlotIndex, sourceStack);

                    // In case the stack has been completely used we can break from it so we can check for the next stack in
                    // the source container.
                    if (remainderCount <= 0) {
                        break;
                    }
                }
            }
        }
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

    // region Debug

    private static void drawConnections(World world, BlockPos position, BlockState blockState, ConnectionData connectionData) {
        if (!Pipeline.DEBUG_PIPE_FLOW) {
            return;
        }

        if (connectionData.source() != null) {
            Draw.text(
                    "Has Source",
                    new double3(position).add(0.5, 1.5, 0.5)
            );

            Draw.arrow(
                    new double3(connectionData.source().position()).add(0.5, 1.05, 0.5),
                    new double3(position).add(0.5, 1.05, 0.5),
                    0xff55ff55
            );

            drawCollisionShape(world, connectionData.source(), 0xff55ff55);
        }

        for (InventoryWithPosition connection : connectionData.connections()) {
            int color = 0xffffaa55;

            // If the connection is a pipe we want to distinguish it from the "final destinations".
            // The color is also based on whether the axis of the connection is positive or not.
            if (!(connection.inventory() instanceof PipeBlockEntity)) {
                color = 0xff55aaff;
            }

            drawCollisionShape(world, connection, color);

            Direction direction = Direction.fromVector(connection.position().subtract(position), Direction.NORTH);
            color = direction.getOffsetX() > 0 || direction.getOffsetY() > 0 || direction.getOffsetZ() > 0 ? 0xff5555ff : 0xffff5555;

            Draw.arrow(
                    new double3(position).add(0.5, 1.05, 0.5),
                    new double3(position).add(0.5, 1.05, 0.5).add(direction.getDoubleVector().multiply(0.5)),
                    color
            );
        }
    }

    private static void drawCollisionShape(World world, InventoryWithPosition inventoryWithPosition, int color) {
        drawCollisionShape(world, inventoryWithPosition.position(), inventoryWithPosition.getBlockState(world), color);
    }

    // TODO: Use the default Geode#drawBlockState instead when it'll be added.
    private static void drawCollisionShape(World world, BlockPos position, BlockState blockState, int color) {
        VoxelShape voxelShape = blockState.getCollisionShape(world, position);

        if (voxelShape.isEmpty()) {
            return;
        }

        Box boundingBox = voxelShape.getBoundingBox();
        double3 boundingBoxSize = new double3(boundingBox.getLengthX(), boundingBox.getLengthY(), boundingBox.getLengthZ());
        Draw.box(new double3(position).add(boundingBox.getCenter()), boundingBoxSize.add(0.01), color);
    }

    // endregion
}