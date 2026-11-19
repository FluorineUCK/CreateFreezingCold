package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Own bucket only: bridges progress to the source actually placed, not the clicked block. */
public final class LiquidAirBucketItem extends BucketItem {
    private final Fluid fluid;

    public LiquidAirBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
        this.fluid = fluid;
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos,
                                 @Nullable BlockHitResult hit, @Nullable ItemStack container) {
        if (container != null && LiquidAirEvaporation.bucketProgressToVolume(container) == 0) {
            return false;
        }
        var before = level.getBlockState(pos);
        if (!before.isAir() && !before.canBeReplaced(fluid)) {
            return hit != null && emptyContents(player, level,
                    hit.getBlockPos().relative(hit.getDirection()), null, container);
        }
        if (before.getFluidState().isSource() && before.getFluidState().getType() == fluid) {
            return false;
        }
        boolean placed = super.emptyContents(player, level, pos, null, container);
        if (placed && level instanceof ServerLevel serverLevel) {
            var after = level.getBlockState(pos);
            if (after.getBlock() instanceof LiquidAirBlocks && after.getFluidState().isSource()
                    && after.getFluidState().getType() == fluid) {
                var data = LiquidAirBlockTimers.get(serverLevel);
                data.track(pos);
                data.updateBlockProgress(pos,
                        container == null ? 0.0 : LiquidAirEvaporation.evaporationProgress(container));
            }
        }
        return placed;
    }
}
