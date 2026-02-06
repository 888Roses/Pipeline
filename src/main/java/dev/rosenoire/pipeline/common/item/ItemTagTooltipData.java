package dev.rosenoire.pipeline.common.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;

public record ItemTagTooltipData(ItemStack itemStack) implements TooltipData {
}
