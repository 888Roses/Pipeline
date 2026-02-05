package dev.rosenoire.pipeline.common.index;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.data_component.ControlledPipePositionDataComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface ModDataComponentTypes {
    ComponentType<ControlledPipePositionDataComponent> CONTROLLED_PIPE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Pipeline.geode.id("controlled_pipe"),
            ComponentType.<ControlledPipePositionDataComponent>builder()
                    .codec(ControlledPipePositionDataComponent.CODEC)
                    .build()
    );

    static void register() {
    }
}
