package net.fluorineuck.createfreezingcold.content.liquefaction.dewar;

import net.fluorineuck.createfreezingcold.registry.CFzCBlockEntities;
import net.fluorineuck.createfreezingcold.registry.CFzCDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponentMap;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class DewarFlaskBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1000;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            DewarFlaskBlockEntity.this.setChanged();
        }
    };

    public IFluidHandler getFluidHandler() {
        return tank;
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }
    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag.getCompound("Tank"));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(
                CFzCDataComponents.DEWAR_CONTENT.get(),
                SimpleFluidContent.copyOf(tank.getFluid())
        );
    }
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        SimpleFluidContent content = input.getOrDefault(
                CFzCDataComponents.DEWAR_CONTENT.get(),
                SimpleFluidContent.EMPTY
        );
        FluidStack fluid = content.copy();
        fluid.setAmount(Math.min(fluid.getAmount(), tank.getCapacity()));
        tank.setFluid(fluid);
    }

    public DewarFlaskBlockEntity(BlockPos pos, BlockState state) {
        super(CFzCBlockEntities.DEWAR_FLASK.get(), pos, state);
    }
}