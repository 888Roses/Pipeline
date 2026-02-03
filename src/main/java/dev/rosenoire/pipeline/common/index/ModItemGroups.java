package dev.rosenoire.pipeline.common.index;

import net.collectively.geode.registration.GeodeItemGroup;
import net.collectively.geode.registration.ItemGroupBuilder;

import static dev.rosenoire.pipeline.common.Pipeline.geode;

public interface ModItemGroups {
    GeodeItemGroup PIPELINE = geode.registerGroup("pipeline",
            ItemGroupBuilder.of().withIcon(ModItems.COPPER_PIPE::getDefaultStack),
            ModItems.COPPER_PIPE
    );

    static void register() {
    }
}
