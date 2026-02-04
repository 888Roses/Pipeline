package dev.rosenoire.pipeline.client.block.renderer;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;

public class CopperPipeBlockEntityRenderState extends BlockEntityRenderState {
    // Note: Not using a map for performance reasons.
    public boolean hasContainerNorth, hasContainerSouth, hasContainerEast, hasContainerWest, hasContainerUp, hasContainerDown;
    /// What direction that pipe is facing.
    public Direction facingDirection;

    /// Checks whether there is a container block in this direction of the pipe or not.
    public boolean hasContainer(Direction direction) {
        return switch (direction) {
            case NORTH -> hasContainerNorth;
            case SOUTH -> hasContainerSouth;
            case EAST -> hasContainerEast;
            case WEST -> hasContainerWest;
            case UP -> hasContainerUp;
            case DOWN -> hasContainerDown;
            case null -> false;
        };
    }
}
