package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LiquidAirBlocks extends LiquidBlock {

    public LiquidAirBlocks(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public ItemStack pickupBlock(
            @Nullable Player player,
            LevelAccessor level,
            BlockPos pos,
            BlockState state
    ) {
        Double progress = level instanceof ServerLevel serverLevel
                ? LiquidAirBlockTimers.get(serverLevel).blockExactProgress(pos) : null;
        ItemStack result = super.pickupBlock(player, level, pos, state);
        if (progress != null && LiquidAirEvaporation.isVolatileBucket(result)) {
            LiquidAirEvaporation.setProgress(result, progress);
        }
        if (player != null
                && LiquidAirEvaporation.isVolatileBucket(result)) {
            LiquidAirEvaporation.queuePlayerCheck(player);
        }
        return result;
    }
    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level instanceof ServerLevel serverLevel) {
            LiquidAirEvaporation.trackBlock(serverLevel, pos);
        }
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (level instanceof ServerLevel serverLevel
                && !state.is(newState.getBlock())) {
            LiquidAirEvaporation.untrackBlock(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}