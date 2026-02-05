package dev.rosenoire.pipeline.common.item;

import dev.rosenoire.pipeline.common.block.PipeBlockEntity;
import dev.rosenoire.pipeline.common.data_component.ControlledPipePositionDataComponent;
import dev.rosenoire.pipeline.common.index.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeControllerItem extends BlockItem {
    public PipeControllerItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack itemStack = context.getStack();
        World world = context.getWorld();
        BlockPos position = context.getBlockPos();

        PlayerEntity player = context.getPlayer();
        if (player == null) return super.useOnBlock(context);

        ControlledPipePositionDataComponent component = itemStack.get(ModDataComponentTypes.CONTROLLED_PIPE);
        if (component == null) ControlledPipePositionDataComponent.reset(itemStack);

        if (component == null) {
            player.sendMessage(Text.literal("Component is null!").formatted(Formatting.RED), false);
            return super.useOnBlock(context);
        }

        if (component.hasSelectedPipe()) {
            if (world.getBlockEntity(component.position()) instanceof PipeBlockEntity blockEntity) {
                blockEntity.setController(context.getBlockPos().offset(context.getSide()));
            }

            ControlledPipePositionDataComponent.reset(itemStack);
            player.setStackInHand(context.getHand(), itemStack);
        } else {
            if (world.getBlockEntity(position) instanceof PipeBlockEntity) {
                ControlledPipePositionDataComponent.setSelected(itemStack, position);
                player.setStackInHand(context.getHand(), itemStack);

                world.playSound(null, position, SoundEvents.ENTITY_COPPER_GOLEM_STEP, SoundCategory.BLOCKS);

                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }
}
