package dev.rosenoire.pipeline.client.index;

import dev.rosenoire.pipeline.client.item.DescriptionTooltipComponent;
import dev.rosenoire.pipeline.client.item.ItemTagTooltipComponent;
import dev.rosenoire.pipeline.common.item.DescriptionTooltipData;
import dev.rosenoire.pipeline.common.item.ItemTagTooltipData;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public interface ModTooltipComponents {
    static void register() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof DescriptionTooltipData desc) {
                return new DescriptionTooltipComponent(desc);
            }

            if (data instanceof ItemTagTooltipData tagData) {
                return new ItemTagTooltipComponent(tagData);
            }

            return null;
        });
    }
}
