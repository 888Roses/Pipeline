package dev.rosenoire.pipeline.common;

import dev.rosenoire.pipeline.common.index.*;
import net.collectively.geode.Geode;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline implements ModInitializer {
    // TODO: Use custom debug renderers instead, like minecraft does.
    public static boolean DEBUG_PIPE_PERMS = false;
    public static boolean DEBUG_PIPE_FLOW = true;
    public static final int MAX_TOOLTIP_WIDTH = 96;

    public static final String MOD_ID = "pipeline";
    public static final Geode geode = Geode.create(MOD_ID);
    public static final Logger log = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        log.info("Pipeline#onInitialize");

        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModDataComponentTypes.register();
        ModItemGroups.register();
        ModBlockTags.register();

        geode.register();
    }
}
