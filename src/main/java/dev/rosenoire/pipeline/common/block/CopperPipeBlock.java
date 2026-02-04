package dev.rosenoire.pipeline.common.block;

import com.mojang.serialization.MapCodec;
import dev.rosenoire.pipeline.common.index.ModBlocks;
import dev.rosenoire.pipeline.common.util.PipelineUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class CopperPipeBlock extends BlockWithEntity {
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public CopperPipeBlock(Settings settings) {
        super(settings);

        setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(CopperPipeBlock::new);
    }


    private VoxelShape cube(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {

        float minX = (x) / 16f;
        float minY = (y) / 16f;
        float minZ = (z) / 16f;

        float maxX = (x + sizeX) / 16f;
        float maxY = (y + sizeY) / 16f;
        float maxZ = (z + sizeZ) / 16f;

        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape base = cube(4,4,4,8,8,8);
        if (state.get(NORTH)) base = VoxelShapes.combine(base, cube(4, 4, 0, 8, 8, 4), BooleanBiFunction.OR);
        if (state.get(SOUTH)) base = VoxelShapes.combine(base, cube(4, 4, 12, 8, 8, 4), BooleanBiFunction.OR);
        if (state.get(WEST)) base = VoxelShapes.combine(base, cube(0, 4, 4, 4, 8, 8), BooleanBiFunction.OR);
        if (state.get(EAST)) base = VoxelShapes.combine(base, cube(12, 4, 4, 4, 8, 8), BooleanBiFunction.OR);
        if (state.get(UP)) base = VoxelShapes.combine(base, cube(4, 12, 4, 8, 4, 8), BooleanBiFunction.OR);
        if (state.get(DOWN)) base = VoxelShapes.combine(base, cube(4, 0, 4, 8, 4, 8), BooleanBiFunction.OR);
        return base;
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return Optional.ofNullable(super.getPlacementState(ctx))
                .map(state -> getNonNullPlacementState(state, ctx))
                .orElse(null);
    }

    private BlockState updateContainersInDirection(BlockState state, WorldView world, BlockPos position) {
        return state
                .with(NORTH, isContainerInDirection(world, position, Direction.NORTH))
                .with(SOUTH, isContainerInDirection(world, position, Direction.SOUTH))
                .with(EAST, isContainerInDirection(world, position, Direction.EAST))
                .with(WEST, isContainerInDirection(world, position, Direction.WEST))
                .with(UP, isContainerInDirection(world, position, Direction.UP))
                .with(DOWN, isContainerInDirection(world, position, Direction.DOWN));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos position, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return updateContainersInDirection(state, world, position);
    }

    private BlockState getNonNullPlacementState(@NotNull BlockState blockState, ItemPlacementContext ctx) {
        BlockPos position = ctx.getBlockPos();
        World world = ctx.getWorld();
        return updateContainersInDirection(blockState.with(FACING, ctx.getSide()), world, position);
    }

    private boolean isContainerInDirection(WorldView world, BlockPos position, Direction inDirection) {
        // The direction cannot be where the pipe comes from. The other directions must lead to containers.
        return PipelineUtil.isContainer(world, position.offset(inDirection))
                // TEMPORARY
                || world.getBlockState(position.offset(inDirection)).isOf(ModBlocks.COPPER_PIPE);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperPipeBlockEntity(pos, state);
    }
}
