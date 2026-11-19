package net.fluorineuck.createfreezingcold.mixin;

import net.fluorineuck.createfreezingcold.content.cooling.SuperChilledCoolerState;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.BaseCoolerState;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.ChilledCoolerState;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BaseCoolerState.class, remap = false)
public abstract class BaseCoolerStateMixin {

    @Inject(
            method = "tickFluid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlockEntity;"
                            + "getFluidCoolingData(Lnet/neoforged/neoforge/fluids/FluidStack;)"
                            + "Lnet/ty/createcraftedbeginning/recipe/CoolingRecipeLookup$CoolingData;"
            ),
            cancellable = true
    )
    private void createfreezingcold$skipSuperChilledFluidRefill(
            BreezeCoolerBlockEntity cooler,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if ((Object) this instanceof SuperChilledCoolerState) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(
            method = "updateRemainingTime",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlockEntity;"
                            + "setCoolerState(Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/coolerstates/BaseCoolerState;)V",
                    ordinal = 0
            ), index = 0
    )
    private BaseCoolerState createfreezingcold$replaceExhaustedState(
            BaseCoolerState originalState
    ) {
        if ((Object) this instanceof SuperChilledCoolerState) {
            return new ChilledCoolerState(
                    SuperChilledCoolerState.AFTERCOOLING_TICKS,
                    false
            );
        }
        return originalState;
    }
}