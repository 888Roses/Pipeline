package dev.rosenoire.pipeline.common.block;

import com.mojang.serialization.MapCodec;
import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import dev.rosenoire.pipeline.common.index.ModBlockTags;
import dev.rosenoire.pipeline.common.util.PipelineUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class PipeBlock extends BlockWithEntity {
    public PipeBlock(Settings settings) {
        super(settings);
        setDefaultState();
    }

    // region Use

    @Override
    protected ActionResult onUseWithItem(ItemStack itemStack, BlockState blockState, World world, BlockPos position, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (itemStack.isIn(ItemTags.PICKAXES)) {
            world.setBlockState(position, blockState.with(FORWARD, getForward(blockState).getOpposite()));
            world.onBlockStateChanged(position, blockState, world.getBlockState(position));
            world.playSound(null, position, SoundEvents.ENTITY_COPPER_GOLEM_STEP, SoundCategory.BLOCKS);
            return ActionResult.SUCCESS;
        }

        return super.onUseWithItem(itemStack, blockState, world, position, player, hand, hitResult);
    }

    // endregion

    // region Shape

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape pipeBase = PipelineUtil.cube(4, 4, 4, 8, 8, 8);

        if (state.get(NORTH)) pipeBase = PipelineUtil.addTo(pipeBase, 4, 4, 0, 8, 8, 4);
        if (state.get(SOUTH)) pipeBase = PipelineUtil.addTo(pipeBase, 4, 4, 12, 8, 8, 4);
        if (state.get(WEST)) pipeBase = PipelineUtil.addTo(pipeBase, 0, 4, 4, 4, 8, 8);
        if (state.get(EAST)) pipeBase = PipelineUtil.addTo(pipeBase, 12, 4, 4, 4, 8, 8);
        if (state.get(UP)) pipeBase = PipelineUtil.addTo(pipeBase, 4, 12, 4, 8, 4, 8);
        if (state.get(DOWN)) pipeBase = PipelineUtil.addTo(pipeBase, 4, 0, 4, 8, 4, 8);

        return pipeBase;
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    // endregion

    // region Block With Entity

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(PipeBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World globalWorld, BlockState state, BlockEntityType<T> type) {
        if (globalWorld.isClient()) {
            return null;
        }

        return validateTicker(type, ModBlockEntities.PIPE,
                (world,
                 position,
                 blockState,
                 blockEntity) -> blockEntity.serverTick(
                        world,
                        position,
                        blockState
                )
        );
    }

    // endregion

    // region Properties

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final EnumProperty<Direction> FORWARD = Properties.FACING;

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, FORWARD);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return Optional.ofNullable(super.getPlacementState(ctx))
                .map(state -> getSafePlacementState(state, ctx))
                .orElse(null);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState blockState, WorldView world, ScheduledTickView tickView, BlockPos position, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return updateConnectionsForState(world, blockState, position, getForward(blockState));
    }

    /// Updates the [#NORTH], [#SOUTH], [#EAST], [#WEST], [#UP] and [#DOWN] connections of the given [BlockState].
    private BlockState updateConnectionsForState(WorldView world, BlockState blockState, BlockPos position, Direction currentForward) {
        return blockState
                .with(NORTH, canConnectToBlockInDirection(world, position, Direction.NORTH, currentForward))
                .with(SOUTH, canConnectToBlockInDirection(world, position, Direction.SOUTH, currentForward))
                .with(EAST, canConnectToBlockInDirection(world, position, Direction.EAST, currentForward))
                .with(WEST, canConnectToBlockInDirection(world, position, Direction.WEST, currentForward))
                .with(UP, canConnectToBlockInDirection(world, position, Direction.UP, currentForward))
                .with(DOWN, canConnectToBlockInDirection(world, position, Direction.DOWN, currentForward));
    }

    /// Gets the placement [BlockState] for an [ItemPlacementContext] using a nonnull starting Block State.
    private @Nullable BlockState getSafePlacementState(@NotNull BlockState blockState, ItemPlacementContext context) {
        BlockPos position = context.getBlockPos();
        World world = context.getWorld();
        Direction side = context.getSide();
        BlockPos clickedBlockPos = position.offset(side.getOpposite());
        BlockState clickedBlockState = world.getBlockState(clickedBlockPos);

        PlayerEntity player = context.getPlayer();
        boolean isPlayerSneaking = player != null && player.isSneaking();

        Direction currentForward = side;

        if (clickedBlockState.getBlock() instanceof PipeBlock) {
            Direction clickedPipeForward = getForward(clickedBlockState);

            if (clickedPipeForward.getAxis() == currentForward.getAxis()) {
                currentForward = clickedPipeForward;
            } else {
                currentForward = side;
            }
        }

        if (isPlayerSneaking) {
            boolean isClickedBlockInventory = false;

            if (clickedBlockState.getBlock() instanceof InventoryProvider) isClickedBlockInventory = true;
            else if (clickedBlockState.hasBlockEntity()) {
                BlockEntity blockEntity = world.getBlockEntity(clickedBlockPos);

                if (blockEntity instanceof Inventory && !(blockEntity instanceof PipeBlockEntity)) {
                    isClickedBlockInventory = true;
                }
            }

            if (!isClickedBlockInventory) {
                currentForward = currentForward.getOpposite();
            }
        }

        return updateConnectionsForState(world, blockState.with(FORWARD, currentForward), position, currentForward);
    }

    /// Sets the default properties of this block.
    private void setDefaultState() {
        setDefaultState(this.getStateManager().getDefaultState()
                .with(FORWARD, Direction.NORTH)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
        );
    }

    // endregion

    // region Utils

    /// Whether the pipe block state at the given [BlockPos] can connect to the block state in the given [Direction] or not.
    public static boolean canConnectToBlockInDirection(WorldView world, BlockPos position, Direction inDirection, Direction currentForward) {
        BlockPos inDirectionPosition = position.offset(inDirection);

        // Prevents pipes from connecting into each other in a way that it may create very small closed loops.
        BlockState blockStateInDirection = world.getBlockState(inDirectionPosition);

        if (blockStateInDirection.isIn(ModBlockTags.DISALLOWED_PIPE_CONTAINER)) {
            return false;
        }

        // TODO: Might want to switch to using tags here.
        if (blockStateInDirection.getBlock() instanceof PipeBlock) {
            Direction.Axis blockStateInDirectionForward = getForward(blockStateInDirection).getAxis();
            Direction.Axis blockStateForward = currentForward.getAxis();

            if (blockStateInDirectionForward == blockStateForward && blockStateForward != inDirection.getAxis()) {
                return false;
            }
        }

        return blockStateInDirection.getBlock() instanceof InventoryProvider || world.getBlockEntity(inDirectionPosition) instanceof Inventory;
    }

    /// Returns the direction the pipe block state is facing, or north as a fallback, if the block state is null, air, or
    /// does not have the [#FORWARD] property.
    public static @NonNull Direction getForward(@Nullable BlockState blockState) {
        if (blockState == null || blockState.isAir()) {
            return Direction.NORTH;
        }

        return blockState.get(FORWARD, Direction.NORTH);
    }

    public static boolean hasContainerInDirection(@NotNull BlockState blockState, Direction direction) {
        return switch (direction) {
            case NORTH -> blockState.get(NORTH);
            case SOUTH -> blockState.get(SOUTH);
            case EAST -> blockState.get(EAST);
            case WEST -> blockState.get(WEST);
            case UP -> blockState.get(UP);
            case DOWN -> blockState.get(DOWN);
            case null -> false;
        };
    }

    // endregion
}
