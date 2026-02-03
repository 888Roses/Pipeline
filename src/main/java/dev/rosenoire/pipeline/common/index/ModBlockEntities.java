package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.block.CopperPipeBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModBlockEntities {
    BlockEntityType<CopperPipeBlockEntity> COPPER_PIPE = geode.registerBlockEntity("copper_pipe", CopperPipeBlockEntity::new, ModBlocks.COPPER_PIPE);

    static void register() {
    }
}
