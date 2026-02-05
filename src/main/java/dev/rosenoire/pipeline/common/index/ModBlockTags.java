package dev.rosenoire.pipeline.common.index;

import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;
import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModBlockTags {
    TagKey<Block> DISALLOWED_PIPE_CONTAINER = geode.registerBlockTag("disallowed_pipe_container");

    static void register() {}
}
