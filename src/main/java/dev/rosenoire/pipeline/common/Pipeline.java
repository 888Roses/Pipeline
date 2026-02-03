package dev.rosenoire.pipeline.common;

import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import dev.rosenoire.pipeline.common.index.ModBlocks;
import dev.rosenoire.pipeline.common.index.ModItemGroups;
import dev.rosenoire.pipeline.common.index.ModItems;
import net.collectively.geode.Geode;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline implements ModInitializer {
    public static final String MOD_ID = "pipeline";
    public static final Geode geode = Geode.create(MOD_ID);
    public static final Logger log = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        log.info("Pipeline#onInitialize");
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModItemGroups.register();

        geode.register();
    }
}
