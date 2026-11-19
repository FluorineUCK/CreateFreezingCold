package net.fluorineuck.createfreezingcold.mixin;

import net.fluorineuck.createfreezingcold.content.cooling.SuperChilledCoolerState;
import net.minecraft.nbt.CompoundTag;

import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerSerialization;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.BaseCoolerState;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.ChilledCoolerState;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BreezeCoolerSerialization.class, remap = false)
public abstract class BreezeCoolerSerializationMixin {
    @Inject(
            method = "readState",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void createfreezingcold$restoreSuperChilled(
            CompoundTag tag,
            CallbackInfoReturnable<BaseCoolerState> cir
    ) {
        CompoundTag stateData = tag.getCompound(
                BreezeCoolerSerialization.COMPOUND_KEY_STATE_DATA);
        if (!stateData.getBoolean(SuperChilledCoolerState.SUPER_CHILLED)) {
            return;
        }

        BaseCoolerState restoredState = cir.getReturnValue();
        if (restoredState.getCoolantType() != BreezeCoolerBlockEntity.CoolantType.NORMAL
                || restoredState.getRemainingTime() <= 0) {
            return;
        }
        cir.setReturnValue(
                new SuperChilledCoolerState(
                        restoredState.getRemainingTime(),
                        restoredState.isCreative()
                )
        );
    }
}