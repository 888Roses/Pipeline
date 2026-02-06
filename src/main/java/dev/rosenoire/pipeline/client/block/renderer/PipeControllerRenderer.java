package dev.rosenoire.pipeline.client.block.renderer;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.block.PipeControllerBlock;
import dev.rosenoire.pipeline.common.block.PipeControllerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.model.CopperGolemStatueModel;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CopperGolemOxidationLevel;
import net.minecraft.entity.passive.CopperGolemOxidationLevels;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class PipeControllerRenderer implements BlockEntityRenderer<PipeControllerBlockEntity, PipeControllerRenderer.State> {
    private final BlockEntityRendererFactory.Context context;
    private final CopperGolemStatueModel statueModel;

    private static final Identifier EYE_FILTER_OUT = Pipeline.geode.id("textures/block/copper_pipe_controller_eyes_out.png");
    private static final Identifier EYE_FILTER_IN = Pipeline.geode.id("textures/block/copper_pipe_controller_eyes_in.png");

    public PipeControllerRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;

        LoadedEntityModels loadedEntityModels = context.loadedEntityModels();
        this.statueModel = new CopperGolemStatueModel(loadedEntityModels.getModelPart(EntityModelLayers.COPPER_GOLEM_SITTING));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PipeControllerBlockEntity blockEntity, State state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        BlockState blockState = blockEntity.getCachedState();
        state.direction = blockState.get(PipeControllerBlock.DIRECTION);
        state.isReverseMode = blockEntity.isReverseMode;
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5F, 0.0F, 0.5F);

        CopperGolemOxidationLevel golem = CopperGolemOxidationLevels.get(Oxidizable.OxidationLevel.UNAFFECTED);
        RenderLayer renderLayer = RenderLayers.entityCutoutNoCull(golem.texture());
        RenderLayer eyeRenderLayer = RenderLayers.entityCutoutNoCull(state.isReverseMode ? EYE_FILTER_OUT : EYE_FILTER_IN);

        queue.submitModel(
                statueModel,
                state.direction,
                matrices,
                renderLayer,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0x0000000,
                state.crumblingOverlay
        );

        matrices.scale(1.01f,1.01f,1.01f);

        queue.submitModel(
                statueModel,
                state.direction,
                matrices,
                eyeRenderLayer,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV,
                0x00000000,
                state.crumblingOverlay
        );

        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        public Direction direction;
        public boolean isReverseMode;
    }
}
