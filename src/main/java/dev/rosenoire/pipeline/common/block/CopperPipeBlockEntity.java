package dev.rosenoire.pipeline.common.block;

import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import dev.rosenoire.pipeline.common.util.PipelineUtil;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.types.double3;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class CopperPipeBlockEntity extends LockableContainerBlockEntity {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int cycleIndex;

    public CopperPipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.COPPER_PIPE, blockPos, blockState);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        cycleIndex = view.getInt("cycleIndex", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory);
        view.putInt("cycleIndex", cycleIndex);
    }

    @Override
    protected Text getContainerName() {
        return Text.literal("Copper Pipe");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    private record PositionedInventory(BlockPos position, Inventory inventory) {
        public BlockState blockState(World world) {
            return world.getBlockState(position);
        }

        public static Optional<PositionedInventory> getPositionedInventory(World world, BlockPos position) {
            if (world.getBlockEntity(position) instanceof Inventory inventory) {
                return Optional.of(new PositionedInventory(position, inventory));
            }

            return Optional.empty();
        }
    }

    public void serverTick(World world, BlockPos blockPos, BlockState blockState) {
        List<PositionedInventory> inventories = new ArrayList<>();
        AtomicReference<PositionedInventory> sourceInventory = new AtomicReference<>();

        for (Direction direction : Direction.values()) {
            PositionedInventory.getPositionedInventory(world, blockPos.offset(direction)).ifPresent(positionedInventory -> {
                if (direction == blockState.get(CopperPipeBlock.FACING).getOpposite()) {
                    if (positionedInventory.inventory() instanceof CopperPipeBlockEntity) {
                        return;
                    }

                    sourceInventory.set(positionedInventory);
                    return;
                }

                inventories.add(positionedInventory);
            });
        }

        if (inventories.isEmpty()) {
            return;
        }

        if (sourceInventory.get() != null) {
            PositionedInventory source = sourceInventory.get();

            Draw.text("Has Source", new double3(blockPos).add(0.5, 1.5, 0.5));

            Draw.arrow(
                    new double3(source.position()).add(0.5, 1.1, 0.5),
                    new double3(blockPos).add(0.5, 1.1, 0.5),
                    0xff55ff55
            );

            for (int i = 0; i < source.inventory().size(); i++) {
                ItemStack stackInSlot = source.inventory().getStack(i);

                if (stackInSlot.isEmpty()) {
                    continue;
                }

                for (int j = 0; j < size(); j++) {
                    int count = stackInSlot.getCount();

                    PipelineUtil.insertStackInInventory(
                            this,
                            j,
                            stackInSlot,
                            1
                    );

                    if (stackInSlot.getCount() != count) {
                        break;
                    }
                }

                source.inventory().setStack(i, stackInSlot);
            }
        }

        drawInventoryGizmo(world, inventories, i -> i.equals(sourceInventory.get()));

        Draw.text(inventories.size(), new double3(blockPos).add(0.5, 1, 0.5));

        // Add everything to the inventory in question.
        if (inventories.size() == 1) {
            Inventory targetInventory = inventories.getFirst().inventory();

            for (int i = 0; i < size(); i++) {
                ItemStack transferredStack = getStack(i);

                for (int j = 0; j < targetInventory.size(); j++) {
                    PipelineUtil.insertStackInInventory(
                            targetInventory,
                            j,
                            transferredStack,
                            transferredStack.getCount()
                    );
                }

                setStack(i, transferredStack);
            }

            return;
        }

        // If multiple inventories, spread it amongst the inventories.
        for (int i = 0; i < size(); i++) {
            ItemStack transferredStack = getStack(i);

            while (!transferredStack.isEmpty()) {
                ItemStack stackBefore = transferredStack.copy();

                PositionedInventory positionedInventory = inventories.get(cycleIndex % inventories.size());
                Inventory targetInventory = positionedInventory.inventory();

                for (int k = 0; k < targetInventory.size(); k++) {
                    PipelineUtil.insertStackInInventory(targetInventory, k, transferredStack, 1);

                    if (transferredStack.isEmpty() || transferredStack.getCount() != stackBefore.getCount()) {
                        break;
                    }
                }

                cycleIndex++;

                if (ItemStack.areEqual(transferredStack, stackBefore)) {
                    break;
                }
            }

            setStack(i, transferredStack);
        }
    }

    private static void drawInventoryGizmo(World world, List<PositionedInventory> inventories, Predicate<PositionedInventory> isSource) {
        for (PositionedInventory inventory : inventories) {
            BlockState inventoryState = inventory.blockState(world);
            VoxelShape voxelShape = inventoryState.getCollisionShape(world, inventory.position);

            if (voxelShape.isEmpty()) {
                continue;
            }

            Box boundingBox = voxelShape.getBoundingBox();
            double3 boundingBoxSize = new double3(boundingBox.getLengthX(), boundingBox.getLengthY(), boundingBox.getLengthZ());

            int color = inventory.inventory() instanceof CopperPipeBlockEntity
                    ? 0xffffaa55
                    : isSource.test(inventory)
                    ? 0xff55ff55
                    : 0xff55aaff;

            Draw.box(new double3(inventory.position).add(boundingBox.getCenter()), boundingBoxSize.add(0.01), color);
        }
    }
}
