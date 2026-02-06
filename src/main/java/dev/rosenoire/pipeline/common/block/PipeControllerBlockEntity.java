package dev.rosenoire.pipeline.common.block;

import dev.rosenoire.pipeline.common.index.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ContainerUser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
                String tagKeyName = filterStack.getCustomName().getString();
                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, Identifier.of(tagKeyName));
                if (stack.isIn(tag)) {
                    return true;
                }
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