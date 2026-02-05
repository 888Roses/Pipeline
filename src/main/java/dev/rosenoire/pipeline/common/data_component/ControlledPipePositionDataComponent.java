package dev.rosenoire.pipeline.common.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rosenoire.pipeline.common.index.ModDataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public record ControlledPipePositionDataComponent(boolean hasSelectedPipe, BlockPos position) {
    public static final Codec<ControlledPipePositionDataComponent> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("has_selected_pipe").forGetter(ControlledPipePositionDataComponent::hasSelectedPipe),
                    BlockPos.CODEC.fieldOf("position").forGetter(ControlledPipePositionDataComponent::position)
            ).apply(
                    builder,
                    ControlledPipePositionDataComponent::new
            )
    );

    public static void reset(ItemStack itemStack) {
        itemStack.set(ModDataComponentTypes.CONTROLLED_PIPE, new ControlledPipePositionDataComponent(false, BlockPos.ORIGIN));
    }

    public static void setSelected(ItemStack itemStack, BlockPos position) {
        itemStack.set(ModDataComponentTypes.CONTROLLED_PIPE, new ControlledPipePositionDataComponent(true, position));
    }
}
