package dev.rosenoire.pipeline.client.index;

import dev.rosenoire.pipeline.client.block.renderer.CopperPipeBlockEntityRenderer;
import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public interface ModBlockEntityRenderers {
    static void register() {
        BlockEntityRendererFactories.register(ModBlockEntities.COPPER_PIPE, CopperPipeBlockEntityRenderer::new);
    }
}
