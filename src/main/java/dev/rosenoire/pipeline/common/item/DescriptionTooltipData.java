package dev.rosenoire.pipeline.common.item;

import net.minecraft.item.tooltip.TooltipData;

public record DescriptionTooltipData(String translationKey) implements TooltipData {
}
