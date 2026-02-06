package dev.rosenoire.pipeline.client;

import dev.rosenoire.pipeline.client.index.ModBlockEntityRenderers;
import dev.rosenoire.pipeline.client.index.ModTooltipComponents;
import dev.rosenoire.pipeline.common.Pipeline;
import net.collectively.geode.GeodeClient;
import net.fabricmc.api.ClientModInitializer;

public class PipelineClient implements ClientModInitializer {
    public static final GeodeClient geode = GeodeClient.create(Pipeline.geode.getLinkedModId());

    @Override
    public void onInitializeClient() {
        Pipeline.log.info("PipelineClient#onInitializeClient");

        ModBlockEntityRenderers.register();
        ModTooltipComponents.register();

        geode.register();
    }
}
