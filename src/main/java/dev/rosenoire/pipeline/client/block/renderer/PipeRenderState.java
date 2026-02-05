package dev.rosenoire.pipeline.client.block.renderer;

import dev.rosenoire.pipeline.common.block.PipeBlockEntity;
import net.collectively.geode.types.double3;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeRenderState extends BlockEntityRenderState {
    // Note: Not using a map for performance reasons.
    public boolean hasContainerNorth, hasContainerSouth, hasContainerEast, hasContainerWest, hasContainerUp, hasContainerDown;

    /// What direction that pipe is facing.
    public Direction facingDirection;
    public PipeBlockEntity.ConnectionData connectionData;

    public boolean connectionData_hasSource;
    public BlockPos connectionData_sourcePos;
    public PipeRenderer.RenderableHitbox connectionData_sourceRenderableHitbox;
    public PipeRenderer.RenderableHitbox connectionData_sourcePipeRenderableHitbox;
    public final List<PipeRenderer.RenderableConnection> connectionData_connections = new ArrayList<>();

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
