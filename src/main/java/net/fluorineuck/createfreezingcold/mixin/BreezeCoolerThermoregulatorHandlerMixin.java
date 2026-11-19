package net.fluorineuck.createfreezingcold.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.fluorineuck.createfreezingcold.content.cooling.SuperChilledCoolerState;

import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.BaseCoolerState;
import net.ty.createcraftedbeginning.api.thermoregulatorhandlers.AirtightThermoregulatorHandler;
import net.ty.createcraftedbeginning.content.airtights.handlers.thermoregulator.contents.BreezeCoolerThermoregulatorHandler;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BreezeCoolerThermoregulatorHandler.class, remap = false)
public abstract class BreezeCoolerThermoregulatorHandlerMixin {

    @Inject(
            method = "getHeat",
            at = @At("RETURN"),
            cancellable = true
    )
    private void createfreezingcold$applySuperCooling(
            Level level,
            BlockPos pos,
            BlockState state,
            CallbackInfoReturnable<Float> cir
    ) {
        if (!(level.getBlockEntity(pos) instanceof BreezeCoolerBlockEntity cooler)
                || cir.getReturnValueF() != AirtightThermoregulatorHandler.CHILLED) {
            return;
        }
        BaseCoolerState currentState =
                ((BreezeCoolerBlockEntityAccessor) cooler)
                        .createfreezingcold$getCurrentState();
        if (currentState instanceof SuperChilledCoolerState
                && currentState.getRemainingTime() > 0) {
            cir.setReturnValue(
                    (float) AirtightThermoregulatorHandler.SUPERCHILLED
            );
        }
    }
}