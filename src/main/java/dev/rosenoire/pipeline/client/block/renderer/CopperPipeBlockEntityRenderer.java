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
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
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
        double3 pos = new double3(state.pos.toCenterPos());
        double3 facing = new double3(state.facingDirection.getDoubleVector());
        Draw.arrow(pos.sub(facing.mul(0.5)), pos.add(facing.mul(0.5)), 0xffff55ff);
    }

    private void drawBlockShape(World world, BlockPos position) {
        BlockState blockState = world.getBlockState(position);
        VoxelShape voxelShape = blockState.getOutlineShape(world, position);

        if (voxelShape.isEmpty()) {
            return;
        }

        Box boundingBox = voxelShape.getBoundingBox();
        double3 boundingBoxSize = new double3(boundingBox.getLengthX(), boundingBox.getLengthY(), boundingBox.getLengthZ());
        Draw.box(new double3(boundingBox.getCenter()), boundingBoxSize.add(0.1), 0xff55ff55);
    }
}
