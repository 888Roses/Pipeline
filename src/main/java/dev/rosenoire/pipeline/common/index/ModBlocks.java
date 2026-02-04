package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.block.CopperPipeBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModBlocks {
    Block COPPER_PIPE = geode.registerBlock("copper_pipe", CopperPipeBlock::new, AbstractBlock.Settings.create().nonOpaque());

    static void register() {
    }
}
