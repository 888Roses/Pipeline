package dev.rosenoire.pipeline.client.block.renderer;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.block.PipeBlock;
import dev.rosenoire.pipeline.common.block.PipeBlockEntity;
import dev.rosenoire.pipeline.common.block.PipeControllerBlockEntity;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.types.double3;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class PipeRenderer implements BlockEntityRenderer<PipeBlockEntity, PipeRenderState> {
    private final BlockEntityRendererFactory.Context context;

    public PipeRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void updateRenderState(PipeBlockEntity blockEntity, PipeRenderState renderState, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, renderState, tickProgress, cameraPos, crumblingOverlay);

        BlockState blockState = blockEntity.getCachedState();
        World world = blockEntity.getWorld();
        BlockPos position = blockEntity.getPos();

        renderState.facingDirection = PipeBlock.getForward(blockState);
        for (Direction direction : Direction.values()) {
            renderState.setHasContainerInDirection(direction, PipeBlock.hasContainerInDirection(blockState, direction));
        }

        renderState.controllerRenderable = null;
        if (world != null && blockEntity.hasController()) {
            PipeControllerBlockEntity pipeController = blockEntity.getControllerBlock(world);

            if (pipeController != null) {
                renderState.controllerRenderable = RenderableHitbox.get(
                        world,
                        pipeController.getPos(),
                        pipeController.getCachedState(),
                        0.01f,
                        0xffffff55
                );
            }
        }

        PipeBlockEntity.ConnectionData connectionData = PipeBlockEntity.ConnectionData.get(
                blockEntity.getWorld(),
                blockEntity.getPos(),
                blockState
        );

        renderState.connectionData_hasSource = false;
        renderState.connectionData_connections.clear();

        if (connectionData.source() != null) {
            renderState.connectionData_hasSource = true;
            renderState.connectionData_sourcePos = connectionData.source().position();

            renderState.connectionData_sourceRenderableHitbox = RenderableHitbox.get(
                    world,
                    renderState.connectionData_sourcePos,
                    connectionData.source().getBlockState(world),
                    0.015f,
                    0xff55ff55
            );

            renderState.connectionData_sourcePipeRenderableHitbox = RenderableHitbox.get(
                    world,
                    position,
                    blockState,
                    0.01f,
                    0xff55ff55
            );
        }

        for (PipeBlockEntity.InventoryWithPosition connection : connectionData.connections()) {
            renderState.connectionData_connections.add(RenderableConnection.get(world, position, connection));
        }

        if (world != null) {
            renderState.hasControllerAbove = world.getBlockEntity(position.up()) instanceof PipeControllerBlockEntity;

            BlockState blockBelow = world.getBlockState(position.down());
            renderState.hasSolidBlockBelow = !blockBelow.isAir() && blockBelow.isSolidBlock(world, position.down());
        }
    }

    @Override
    public void render(PipeRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (Pipeline.DEBUG_PIPE_FLOW) {
            Draw.arrow(
                    new double3(state.pos.toCenterPos()).add(state.facingDirection.getDoubleVector().multiply(-0.5)),
                    new double3(state.pos.toCenterPos()).add(state.facingDirection.getDoubleVector().multiply(0.5)),
                    0xffffff55
            );

            if (state.controllerRenderable != null) {
                state.controllerRenderable.render();

                Draw.line(
                        new double3(state.pos.toCenterPos()),
                        new double3(state.controllerRenderable.position()),
                        0xffffff55
                );
            }

            if (state.connectionData_hasSource) {
                state.connectionData_sourceRenderableHitbox.render();
                state.connectionData_sourcePipeRenderableHitbox.render();

                Draw.text(
                        "Has Source",
                        new double3(state.pos).add(0.5, 1.5, 0.5)
                );

                Draw.arrow(
                        new double3(state.connectionData_sourcePos).add(0.5, 1.05, 0.5),
                        new double3(state.pos).add(0.5, 1.05, 0.5),
                        0xff55ff55
                );
            }

            for (RenderableConnection connection : state.connectionData_connections) {
                connection.render();
            }
        }

        if (state.hasControllerAbove) {
            queue.submitBlock(
                    matrices,
                    Blocks.SCAFFOLDING.getDefaultState()
                            .with(ScaffoldingBlock.BOTTOM, !state.hasSolidBlockBelow),
                    state.lightmapCoordinates,
                    OverlayTexture.DEFAULT_UV,
                    0x00000000
            );
        }
    }

    public record RenderableConnection(RenderableHitbox shape, double3 arrowA, double3 arrowB, int arrowColor) {
        public static RenderableConnection get(World world, BlockPos position, PipeBlockEntity.InventoryWithPosition connection) {
            int color = 0x00ffaa55;

            // If the connection is a pipe we want to distinguish it from the "final destinations".
            // The color is also based on whether the axis of the connection is positive or not.
            if (!(connection.inventory() instanceof PipeBlockEntity)) {
                color = 0xff55aaff;
            }

            RenderableHitbox shape = RenderableHitbox.get(world, connection.position(), connection.getBlockState(world), 0.01f, color);

            Direction direction = Direction.fromVector(connection.position().subtract(position), Direction.NORTH);
            color = 0xff55ffff;

            return new RenderableConnection(
                    shape,
                    new double3(position).add(0.5, 1.05, 0.5),
                    new double3(position).add(0.5, 1.05, 0.5).add(direction.getDoubleVector().multiply(0.5)),
                    color
            );
        }

        public void render() {
            shape.render();
            Draw.arrow(arrowA, arrowB, arrowColor);
        }
    }

    public record RenderableHitbox(double3 position, double3 size, int color) {
        public static RenderableHitbox get(World world, BlockPos position, BlockState blockState, float offset, int color) {
            VoxelShape voxelShape = blockState.getCollisionShape(world, position);

            if (voxelShape.isEmpty()) {
                return null;
            }

            Box boundingBox = voxelShape.getBoundingBox();

            return new RenderableHitbox(
                    new double3(position).add(boundingBox.getCenter()),
                    new double3(boundingBox.getLengthX(), boundingBox.getLengthY(), boundingBox.getLengthZ()).add(offset),
                    color
            );
        }

        public void render() {
            Draw.box(position, size, color);
        }
    }
}
