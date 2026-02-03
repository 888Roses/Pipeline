package dev.rosenoire.pipeline.common.index;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModItems {
    Item COPPER_PIPE = geode.registerItem("copper_pipe", settings -> new BlockItem(ModBlocks.COPPER_PIPE, settings), new Item.Settings());

    static void register() {
    }
}
