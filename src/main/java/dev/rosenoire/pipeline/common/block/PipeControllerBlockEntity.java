package dev.rosenoire.pipeline.common.block;

import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ContainerUser;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PipeControllerBlockEntity extends ChestBlockEntity {
    public PipeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE_CONTROLLER, pos, state);
    }

    public boolean validate(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return !containsAny(otherStack -> ItemStack.areItemsAndComponentsEqual(stack, otherStack));
    }

    @Override
    public Text getName() {
        return Text.literal("Pipe Controller");
    }

    @Override
    public void onOpen(ContainerUser user) {
        super.onOpen(user);

        if (world != null) {
            world.playSound(
                    null,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    SoundEvents.BLOCK_COPPER_CHEST_OPEN,
                    SoundCategory.BLOCKS,
                    0.5F,
                    world.random.nextFloat() * 0.1F + 0.9F
            );
        }
    }

    @Override
    public void onClose(ContainerUser user) {
        super.onClose(user);

        if (world != null) {
            world.playSound(
                    null,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    SoundEvents.BLOCK_COPPER_CHEST_CLOSE,
                    SoundCategory.BLOCKS,
                    0.5F,
                    world.random.nextFloat() * 0.1F + 0.9F
            );
        }
    }
}