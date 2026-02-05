package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.block.PipeBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModBlockEntities {
    BlockEntityType<PipeBlockEntity> PIPE = geode.registerBlockEntity(
            "pipe",
            PipeBlockEntity::new,
            ModBlocks.COPPER_PIPE
    );

    static void register() {
    }
}
