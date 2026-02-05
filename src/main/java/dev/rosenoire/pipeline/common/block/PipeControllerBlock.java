package dev.rosenoire.pipeline.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class PipeControllerBlock extends BlockWithEntity {
    public PipeControllerBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(DIRECTION, Direction.NORTH));
    }

    // region Block Entity

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(PipeControllerBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PipeControllerBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world instanceof ServerWorld serverWorld) {
            NamedScreenHandlerFactory namedScreenHandlerFactory = this.createScreenHandlerFactory(state, world, pos);
            if (namedScreenHandlerFactory != null) {
                player.openHandledScreen(namedScreenHandlerFactory);
                PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
            }
        }

        return ActionResult.SUCCESS;
    }

    // endregion

    // region Properties

    public static final EnumProperty<Direction> DIRECTION = Properties.HORIZONTAL_FACING;

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DIRECTION);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return Optional.ofNullable(super.getPlacementState(ctx))
                .map(x -> getSafePlacementState(x, ctx))
                .orElse(null);
    }

    public BlockState getSafePlacementState(@NotNull BlockState blockState, ItemPlacementContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        Direction direction = Direction.NORTH;

        if (player != null) {
            direction = player.getHorizontalFacing().getOpposite();

            if (player.isSneaking()) {
                direction = direction.getOpposite();
            }
        }

        return blockState.with(DIRECTION, direction);
    }

    // endregion
}
