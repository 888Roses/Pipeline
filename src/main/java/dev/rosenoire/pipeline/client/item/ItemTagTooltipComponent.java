package dev.rosenoire.pipeline.client.item;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.item.ItemTagTooltipData;
import dev.rosenoire.pipeline.common.util.TagHelper;
import net.collectively.geode.math.math;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record ItemTagTooltipComponent(ItemTagTooltipData data) implements TooltipComponent {
    private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.ofVanilla("container/bundle/slot_background");
    private static final Identifier SCROLL_ARROW_DOWN_TEXTURE = Pipeline.geode.id("scroll_arrow_down");
    private static final Identifier SCROLL_ARROW_UP_TEXTURE = Pipeline.geode.id("scroll_arrow_up");
    private static final int SCROLL_ARROW_SIZE = 16;

    private static final Text INVALID_TAG = Text.translatable("item.pipeline.tag.invalid");

    private static final int MAX_SLOTS_X = 4;
    private static final int MAX_SLOTS_Y = 3;
    private static final int SLOT_SIZE = 22;

    public static int scrollAmount = 0;

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return TagHelper.getItemsFromTagItemStack(data().itemStack())
                .map(x -> math.max(10, calculateHeight(x)))
                .orElse(10);
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return MAX_SLOTS_X * SLOT_SIZE;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        Optional<List<Item>> tagContent = TagHelper.getItemsFromTagItemStack(data().itemStack());

        if (tagContent.isEmpty()) {
            context.drawWrappedTextWithShadow(textRenderer, INVALID_TAG, x, y, Pipeline.MAX_TOOLTIP_WIDTH, 0xffff5555);
            return;
        }

        List<Item> items = tagContent.get();

        if (items.isEmpty()) {
            context.drawWrappedTextWithShadow(textRenderer, INVALID_TAG, x, y, Pipeline.MAX_TOOLTIP_WIDTH, 0xffff5555);
            return;
        }

        int calculatedHeight = calculateHeight(items);
        boolean hasScrollbar = hasScrollbar(items);

        int maxScroll = getMaxScrollAmount(items);
        if (!hasScrollbar) scrollAmount = 0;

        int index = scrollAmount * MAX_SLOTS_X - 1;
        for (int iy = 0; iy < MAX_SLOTS_Y; iy++) {
            boolean shouldStopDrawingItems = false;

            for (int ix = 0; ix < MAX_SLOTS_X; ix++) {
                index++;

                if (index >= items.size()) {
                    shouldStopDrawingItems = true;
                    break;
                }

                drawItem(context, items.get(index), x + ix * SLOT_SIZE, y + iy * SLOT_SIZE);
            }

            if (shouldStopDrawingItems) {
                break;
            }
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long time = client.world != null ? client.world.getTime() : 0;

        if (hasScrollbar) {
            Identifier scrollArrowTexture = scrollAmount >= maxScroll ? SCROLL_ARROW_UP_TEXTURE : SCROLL_ARROW_DOWN_TEXTURE;
            context.drawGuiTexture(
                    RenderPipelines.GUI_TEXTURED, scrollArrowTexture,
                    x + (MAX_SLOTS_X * SLOT_SIZE) / 2 - 3 - SCROLL_ARROW_SIZE / 2,
                    y + calculatedHeight - SCROLL_ARROW_SIZE - 7 + (int) math.ceil(math.sin(time / 10f) * 2),
                    SCROLL_ARROW_SIZE, SCROLL_ARROW_SIZE
            );
        }
    }

    public static int getMaxScrollAmount(List<Item> items) {
        return (int) math.round(items.size() / (double) MAX_SLOTS_X - MAX_SLOTS_Y);
    }

    public int calculateHeight(List<Item> items) {
        int rowCount = 0;
        int counter = 0;

        for (int i = 0; i < items.size(); i++) {
            counter++;

            if(counter > MAX_SLOTS_X) {
                rowCount++;
                counter = 0;
            }
        }

        return math.min(MAX_SLOTS_Y, (rowCount == 0 ? items.isEmpty() ? 0 : rowCount + 1 : rowCount + 1)) * SLOT_SIZE - 3;
    }

    public static boolean hasScrollbar(List<Item> items) {
        return getMaxScrollAmount(items) > 0;
    }

    public void drawItem(DrawContext context, Item item, int x, int y) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x - 4, y - 4, SLOT_SIZE + 2, SLOT_SIZE + 2);
        context.drawItem(item.getDefaultStack(), x, y);
    }
}
