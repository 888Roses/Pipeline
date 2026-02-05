package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.block.PipeBlockEntity;
import dev.rosenoire.pipeline.common.block.PipeControllerBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModBlockEntities {
    BlockEntityType<PipeBlockEntity> PIPE = geode.registerBlockEntity(
            "pipe",
            PipeBlockEntity::new,
            ModBlocks.COPPER_PIPE
    );

    BlockEntityType<PipeControllerBlockEntity> PIPE_CONTROLLER = geode.registerBlockEntity(
            "pipe_controller",
            PipeControllerBlockEntity::new,
            ModBlocks.COPPER_PIPE_CONTROLLER
    );

    static void register() {
    }
}
