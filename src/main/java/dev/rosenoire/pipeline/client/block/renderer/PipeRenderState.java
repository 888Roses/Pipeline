package dev.rosenoire.pipeline.client.block.renderer;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;

public class PipeRenderState extends BlockEntityRenderState {
    // Note: Not using a map for performance reasons.
    public boolean hasContainerNorth, hasContainerSouth, hasContainerEast, hasContainerWest, hasContainerUp, hasContainerDown;

    /// What direction that pipe is facing.
    public Direction facingDirection;

    /// Checks whether there is a container block in this direction of the pipe or not.
    public boolean hasContainerInDirection(Direction direction) {
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

    public void setHasContainerInDirection(Direction direction, boolean hasContainerInDirection) {
        switch (direction) {
            case NORTH -> hasContainerNorth = hasContainerInDirection;
            case SOUTH -> hasContainerSouth = hasContainerInDirection;
            case EAST -> hasContainerEast = hasContainerInDirection;
            case WEST -> hasContainerWest = hasContainerInDirection;
            case UP -> hasContainerUp = hasContainerInDirection;
            case DOWN -> hasContainerDown = hasContainerInDirection;
        };
    }
}
