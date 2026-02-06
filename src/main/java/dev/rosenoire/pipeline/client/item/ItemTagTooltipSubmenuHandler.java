package dev.rosenoire.pipeline.client.item;

import dev.rosenoire.pipeline.common.util.TagHelper;
import net.collectively.geode.math.math;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Optional;

public class ItemTagTooltipSubmenuHandler implements TooltipSubmenuHandler {
    @Override
    public boolean isApplicableTo(Slot slot) {
        ItemStack itemStack = slot.getStack();

        if (!itemStack.isOf(Items.NAME_TAG)) {
            return false;
        }

        return !TagHelper.getItemsFromTagItemStack(itemStack).map(List::isEmpty).orElse(true);
    }

    @Override
    public boolean onScroll(double horizontal, double vertical, int slotId, ItemStack itemStack) {
        Optional<List<Item>> tagContent = TagHelper.getItemsFromTagItemStack(itemStack);
        if (tagContent.isEmpty()) return false;

        List<Item> items = tagContent.get();
        if (!ItemTagTooltipComponent.hasScrollbar(items)) return false;

        int maxScroll = ItemTagTooltipComponent.getMaxScrollAmount(items);
        int scroll = ItemTagTooltipComponent.scrollAmount + (int) Math.signum(vertical) * -1;

        if (scroll <= maxScroll && scroll > -1) {
            playScrollSound((float) scroll, (float) maxScroll);
        }

        ItemTagTooltipComponent.scrollAmount = math.clamp(scroll, 0, maxScroll);
        return true;
    }

    private static void playScrollSound(float scroll, float maxScroll) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayer = client.player;

        if (clientPlayer != null) {
            float pitch = math.lerp(
                    scroll / maxScroll,
                    1.1f, 1.3f
            );

            clientPlayer.playSound(SoundEvents.BLOCK_DECORATED_POT_INSERT, 0.15f, pitch);
        }
    }

    @Override
    public void reset(Slot slot) {
    }

    @Override
    public void onMouseClick(Slot slot, SlotActionType actionType) {
    }
}
