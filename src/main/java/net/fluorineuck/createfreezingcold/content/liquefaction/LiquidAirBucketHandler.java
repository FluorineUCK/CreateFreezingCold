package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.fluorineuck.createfreezingcold.registry.CFzCFluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

/** Converts virtual bucket loss into mB */
public final class LiquidAirBucketHandler extends FluidBucketWrapper {
    public LiquidAirBucketHandler(ItemStack container) {
        super(container);
    }

    public static ItemStack filledBucket(FluidStack fluid) {
        if (fluid.isEmpty() || !fluid.getComponents().isEmpty()) {return ItemStack.EMPTY;}
        for (var entry : CFzCFluids.ENTRIES) {
            if (fluid.is(entry.source.get())) {
                ItemStack result = new ItemStack(entry.bucket.get());
                LiquidAirEvaporation.volumeToBucketProgress(result, fluid.getAmount());
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FluidStack getFluid() {
        for (var entry : CFzCFluids.ENTRIES) {
            if (container.is(entry.bucket.get())) {
                int amount = LiquidAirEvaporation.bucketProgressToVolume(container);
                return amount == 0 ? FluidStack.EMPTY : new FluidStack(entry.source.get(), amount);
            }
        }
        return super.getFluid();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || container.getCount() != 1) {return 0;}
        for (var entry : CFzCFluids.ENTRIES) {
            if (!resource.is(entry.source.get())) {continue;}
            if (!resource.getComponents().isEmpty()
                    || !(container.is(Items.BUCKET) || container.is(entry.bucket.get()))) {return 0;}
            int current = container.is(Items.BUCKET) ? 0
                    : LiquidAirEvaporation.bucketProgressToVolume(container);
            int accepted = Math.min(resource.getAmount(), FluidType.BUCKET_VOLUME - current);
            if (accepted > 0 && action.execute()) {
                if (container.is(Items.BUCKET)) {container = new ItemStack(entry.bucket.get());}
                LiquidAirEvaporation.volumeToBucketProgress(container, current + accepted);
            }
            return accepted;
        }
        // Normal empty buckets
        return LiquidAirEvaporation.isVolatileBucket(container) ? 0 : super.fill(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!LiquidAirEvaporation.isVolatileBucket(container)) {return super.drain(maxDrain, action);}
        if (maxDrain <= 0 || container.getCount() != 1) {return FluidStack.EMPTY;}
        FluidStack stored = getFluid();
        if (stored.isEmpty()) {return FluidStack.EMPTY;}
        int drained = Math.min(maxDrain, stored.getAmount());
        if (action.execute()) {
            int remaining = stored.getAmount() - drained;
            if (remaining == 0) {
                container = new ItemStack(Items.BUCKET);
            } else {
                LiquidAirEvaporation.volumeToBucketProgress(container, remaining);
            }
        }
        return stored.copyWithAmount(drained);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!LiquidAirEvaporation.isVolatileBucket(container)) {return super.drain(resource, action);}
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(getFluid(), resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }
}
