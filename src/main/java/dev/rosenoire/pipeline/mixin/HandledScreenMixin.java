package dev.rosenoire.pipeline.mixin;

import dev.rosenoire.pipeline.client.item.ItemTagTooltipSubmenuHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("CodeBlock2Expr")
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected abstract void addTooltipSubmenuHandler(TooltipSubmenuHandler handler);

    @Inject(method = "init", at = @At("TAIL"))
    private void pipeline$init(CallbackInfo ci) {
        addTooltipSubmenuHandler(new ItemTagTooltipSubmenuHandler());
    }
}
