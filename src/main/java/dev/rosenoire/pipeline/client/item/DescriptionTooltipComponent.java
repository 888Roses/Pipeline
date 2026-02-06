package dev.rosenoire.pipeline.client.item;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.item.DescriptionTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

public class DescriptionTooltipComponent implements TooltipComponent {
    private final Text description;

    public DescriptionTooltipComponent(DescriptionTooltipData tooltipData) {
        description = Text.translatable(tooltipData.translationKey());
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return getDescriptionHeight(textRenderer) + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return Pipeline.MAX_TOOLTIP_WIDTH;
    }

    @Override
    public void drawText(DrawContext context, TextRenderer textRenderer, int x, int y) {
        drawEmptyDescription(x, y, textRenderer, context);
    }

    private void drawEmptyDescription(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
        drawContext.drawWrappedTextWithShadow(textRenderer, description, x, y, Pipeline.MAX_TOOLTIP_WIDTH, 0xffAAAAAA);
    }

    private int getDescriptionHeight(TextRenderer textRenderer) {
        return textRenderer.wrapLines(description, Pipeline.MAX_TOOLTIP_WIDTH).size() * 9;
    }
}
