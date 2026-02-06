package dev.rosenoire.pipeline.client.index;

import dev.rosenoire.pipeline.client.item.DescriptionTooltipComponent;
import dev.rosenoire.pipeline.common.item.DescriptionTooltipData;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public interface ModTooltipComponents {
    static void register() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof DescriptionTooltipData desc) {
                return new DescriptionTooltipComponent(desc);
            }

            return null;
        });
    }
}
