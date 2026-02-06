package dev.rosenoire.pipeline.mixin;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.item.ItemTagTooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @At("HEAD"), method = "getTooltipData", cancellable = true)
    private void pipeline$getTooltipData(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (stack.isOf(Items.NAME_TAG) && stack.getCustomName() != null && stack.getCustomName().getString().startsWith("#")) {
            cir.setReturnValue(Optional.of(new ItemTagTooltipData(stack)));
        }
    }
}
