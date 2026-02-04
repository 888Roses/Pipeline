package dev.rosenoire.pipeline.client.block.renderer;

import dev.rosenoire.pipeline.common.block.CopperPipeBlock;
import dev.rosenoire.pipeline.common.block.CopperPipeBlockEntity;
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

public class CopperPipeBlockEntityRenderer implements BlockEntityRenderer<CopperPipeBlockEntity, CopperPipeBlockEntityRenderState> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final BlockEntityRendererFactory.Context context;

    public CopperPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public CopperPipeBlockEntityRenderState createRenderState() {
        return new CopperPipeBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(CopperPipeBlockEntity blockEntity, CopperPipeBlockEntityRenderState renderState, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, renderState, tickProgress, cameraPos, crumblingOverlay);

        BlockState state = blockEntity.getCachedState();

        renderState.facingDirection = state.get(CopperPipeBlock.FACING);

        renderState.hasContainerNorth = state.get(CopperPipeBlock.NORTH);
        renderState.hasContainerSouth = state.get(CopperPipeBlock.SOUTH);
        renderState.hasContainerEast = state.get(CopperPipeBlock.EAST);
        renderState.hasContainerWest = state.get(CopperPipeBlock.WEST);
        renderState.hasContainerUp = state.get(CopperPipeBlock.UP);
        renderState.hasContainerDown = state.get(CopperPipeBlock.DOWN);
    }

    @Override
    public void render(CopperPipeBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        double3 pos = new double3(state.pos.toCenterPos()).addY(0.75);

        Draw.arrow(pos, pos.add(state.facingDirection.getDoubleVector()), 0xff5555ff);

        for (Direction direction : Direction.values()) {
            if (state.hasContainer(direction)) {
                Draw.arrow(pos, pos.add(direction.getDoubleVector()), 0xff55ff55);
            }
        }
    }
}
