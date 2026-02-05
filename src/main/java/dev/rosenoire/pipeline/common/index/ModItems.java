package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.data_component.ControlledPipePositionDataComponent;
import dev.rosenoire.pipeline.common.item.PipeControllerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModItems {
    Item COPPER_PIPE = geode.registerItem("copper_pipe", settings -> new BlockItem(ModBlocks.COPPER_PIPE, settings), new Item.Settings());
    Item COPPER_PIPE_CONTROLLER = geode.registerItem(
            "copper_pipe_controller",
            settings -> new PipeControllerItem(ModBlocks.COPPER_PIPE_CONTROLLER, settings),
            new Item.Settings()
                    .component(ModDataComponentTypes.CONTROLLED_PIPE, new ControlledPipePositionDataComponent(
                            false,
                            BlockPos.ORIGIN
                    ))
    );

    static void register() {
    }
}
