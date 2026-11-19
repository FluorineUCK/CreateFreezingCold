package net.fluorineuck.createfreezingcold.content.liquefaction.dewar;

import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirEvaporation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.common.SoundActions;


public class DewarFlaskBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5, 0, 5, 11, 9, 11),
            Block.box(7, 9, 7, 9, 10.8, 9)
    );

    public DewarFlaskBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public DewarFlaskBlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new DewarFlaskBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (FluidUtil.getFluidHandler(stack).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof DewarFlaskBlockEntity dewar)) {
            return ItemInteractionResult.FAIL;
        }

        boolean transferred;
        if (LiquidAirEvaporation.isVolatileBucket(stack)) {
            if (stack.getCount() != 1) {return ItemInteractionResult.CONSUME;}
            // Filled air buckets always pour INTO the Dewar, even if that bucket is only partial.
            var source = FluidUtil.getFluidHandler(stack.copyWithCount(1)).orElseThrow();
            if (LiquidAirEvaporation.bucketProgressToVolume(stack) == 0) {
                if (!player.hasInfiniteMaterials()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                LiquidAirEvaporation.trackPlayer(player);
                return ItemInteractionResult.CONSUME;
            }
            FluidStack moved = FluidUtil.tryFluidTransfer(
                    dewar.getFluidHandler(), source, Integer.MAX_VALUE, true);
            transferred = !moved.isEmpty();
            if (transferred) {
                if (!player.hasInfiniteMaterials()) {
                    player.setItemInHand(hand, source.getContainer());
                }
                var sound = moved.getFluidType().getSound(moved,
                        SoundActions.BUCKET_EMPTY);
                if (sound != null) {
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);
                }
            }
        } else {
            transferred = FluidUtil.interactWithFluidHandler(player, hand, dewar.getFluidHandler());
        }
        if (transferred) {
            LiquidAirEvaporation.trackPlayer(player);
        }
        return ItemInteractionResult.CONSUME;
    }
}
