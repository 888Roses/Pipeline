package dev.rosenoire.pipeline.client;

import dev.rosenoire.pipeline.client.index.ModBlockEntityRenderers;
import dev.rosenoire.pipeline.client.index.ModTooltipComponents;
import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.index.ModItems;
import net.collectively.geode.GeodeClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class PipelineClient implements ClientModInitializer {
    public static final GeodeClient geode = GeodeClient.create(Pipeline.geode.getLinkedModId());

    @Override
    public void onInitializeClient() {
        Pipeline.log.info("PipelineClient#onInitializeClient");

        ModBlockEntityRenderers.register();
        ModTooltipComponents.register();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.WAXED_OXIDIZED_COPPER_BULB, ModItems.COPPER_PIPE, ModItems.COPPER_PIPE_CONTROLLER);
        });

        geode.register();
    }
}
