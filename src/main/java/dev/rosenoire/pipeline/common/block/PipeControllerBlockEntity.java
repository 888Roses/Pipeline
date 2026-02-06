package dev.rosenoire.pipeline.common.block;

import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import dev.rosenoire.pipeline.common.util.TagHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ContainerUser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public class PipeControllerBlockEntity extends ChestBlockEntity {
    public boolean isReverseMode;

    public PipeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE_CONTROLLER, pos, state);
    }

    public boolean validate(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        boolean isContained = containsAny(filterStack -> {
            if (filterStack.isOf(Items.NAME_TAG) && filterStack.getCustomName() != null) {
                Optional<List<Item>> tagContent = TagHelper.getItemsFromTagItemStack(filterStack);

                if (tagContent.isEmpty()) {
                    return false;
                }

                List<Item> items = tagContent.get();

                if (items.isEmpty()) {
                    return false;
                }

                return items.contains(stack.getItem());
            }

            return ItemStack.areItemsAndComponentsEqual(stack, filterStack);
        });

        return isReverseMode != isContained;
    }

    @Override
    public Text getName() {
        return Text.literal("Filter");
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