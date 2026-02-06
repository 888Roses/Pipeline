package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.block.CopperPipeBlock;
import dev.rosenoire.pipeline.common.block.PipeControllerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModBlocks {
    Block COPPER_PIPE = geode.registerBlock(
            "copper_pipe",
            CopperPipeBlock::new,
            AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
                    .nonOpaque()
    );

    Block COPPER_PIPE_CONTROLLER = geode.registerBlock(
            "copper_pipe_controller",
            PipeControllerBlock::new,
            AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
                    .nonOpaque()
    );

    static void register() {
    }
}
