package net.fluorineuck.createfreezingcold.content.cooling;

import net.fluorineuck.createfreezingcold.registry.CFzCTags;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.ChilledCoolerState;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerController;
import net.ty.createcraftedbeginning.recipe.CoolingRecipeLookup;

public final class SuperChilledCoolerState extends ChilledCoolerState {
    public static final String SUPER_CHILLED = "createfreezingcold:super_chilled";
    public static final int AFTERCOOLING_TICKS = 5000;

    public SuperChilledCoolerState(int remainingTime) {super(remainingTime, false);}
    public SuperChilledCoolerState(int remainingTime, boolean isCreative) {super(remainingTime, isCreative);}

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putBoolean(SUPER_CHILLED, true);
    }

    @Override
    public InteractionResult onItemInsert(
            BreezeCoolerBlockEntity cooler,
            ItemStack stack,
            boolean forceOverflow,
            boolean simulate
    ) {
        Level level = cooler.getLevel();
        if (level == null
                || isCreative()
                || !stack.is(CFzCTags.SUPER_COOLING_FUELS)) {
            return InteractionResult.FAIL;
        }

        // TODO: 高级燃料是否使用独立时长数据？目前复用 CCB 冷却配方。
        int addedTime = CoolingRecipeLookup
                .findCoolingData(level, stack, null)
                .time();
        if (addedTime <= 0) {
            return InteractionResult.FAIL;
        }
        long nextTime = (long) getRemainingTime() + addedTime;

        if (!forceOverflow
                && shouldRejectAutomaticOverflow(getRemainingTime(), nextTime)) {
            return InteractionResult.FAIL;
        }
        if (simulate) {
            return InteractionResult.SUCCESS;
        }

        updateRemainingTime(
                cooler,
                nextTime,
                BreezeCoolerController.CoolingSyncMode.IMMEDIATE
        );
        cooler.playSound();
        cooler.spawnParticleBurst();

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onSnowballImpact(BreezeCoolerBlockEntity cooler) {
        return false;
    }

    public static InteractionResult tryUpgrade(
            BreezeCoolerBlockEntity cooler,
            ItemStack stack,
            boolean forceOverflow,
            boolean simulate
    ) {
        Level level = cooler.getLevel();
        if (level == null
                || cooler.isCreative()
                || !stack.is(CFzCTags.SUPER_COOLING_FUELS)) {
            return InteractionResult.FAIL;
        }

        int fuelTime = CoolingRecipeLookup
                .findCoolingData(level, stack, null)
                .time();
        if (fuelTime <= 0
                || !forceOverflow
                && shouldRejectAutomaticOverflow(0, fuelTime)) {
            return InteractionResult.FAIL;
        }
        if (simulate) {
            return InteractionResult.SUCCESS;
        }

        int initialTime = Math.min(
                fuelTime,
                BreezeCoolerBlockEntity.getMaxCoolantCapacity()
        );
        cooler.setCoolerState(new SuperChilledCoolerState(initialTime));
        cooler.playSound();
        cooler.spawnParticleBurst();

        return InteractionResult.SUCCESS;
    }
}