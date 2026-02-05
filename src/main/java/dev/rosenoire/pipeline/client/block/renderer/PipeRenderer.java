package dev.rosenoire.pipeline.client.block.renderer;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.block.PipeBlock;
import dev.rosenoire.pipeline.common.block.PipeBlockEntity;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.types.double3;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

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

        renderState.facingDirection = PipeBlock.getForward(blockState);
        for (Direction direction : Direction.values()) {
            renderState.setHasContainerInDirection(direction, PipeBlock.hasContainerInDirection(blockState, direction));
        }
    }

    @Override
    public void render(PipeRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (Pipeline.DEBUG_PIPE_FLOW) {
            double3 pos = new double3(state.pos.toCenterPos());
            double3 facing = new double3(state.facingDirection.getDoubleVector());
            Draw.arrow(pos.sub(facing.mul(0.5)), pos.add(facing.mul(0.5)), 0xffff55ff);
        }
    }
}
