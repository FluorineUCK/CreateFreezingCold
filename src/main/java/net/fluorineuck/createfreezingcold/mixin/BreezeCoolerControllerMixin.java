package net.fluorineuck.createfreezingcold.mixin;

import net.fluorineuck.createfreezingcold.content.cooling.SuperChilledCoolerState;
import net.fluorineuck.createfreezingcold.registry.CFzCTags;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.BaseCoolerState;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.InactiveCoolerState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
        targets = "net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerController",
        remap = false
)
public abstract class BreezeCoolerControllerMixin {
    @Shadow
    @Final
    private BreezeCoolerBlockEntity cooler;

    @Redirect(
            method = "tryUpdateCoolantByItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/coolerstates/BaseCoolerState;"
                            + "onItemInsert("
                            + "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlockEntity;"
                            + "Lnet/minecraft/world/item/ItemStack;ZZ)"
                            + "Lnet/minecraft/world/InteractionResult;"
            )
    )
    private InteractionResult createfreezingcold$routeCoolant(
            BaseCoolerState currentState,
            BreezeCoolerBlockEntity cooler,
            ItemStack stack,
            boolean forceOverflow,
            boolean simulate
    ) {
        if (!currentState.isCreative()
                && !(currentState instanceof SuperChilledCoolerState)
                && stack.is(CFzCTags.SUPER_COOLING_FUELS)) {
            return SuperChilledCoolerState.tryUpgrade(
                    cooler, stack, forceOverflow, simulate
            );
        }

        return currentState.onItemInsert(
                cooler, stack, forceOverflow, simulate
        );
    }

    @ModifyArg(
            method = "tryUpdateCoolantByItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/BreezeCoolerBlockEntity;"
                            + "setCoolerState(Lnet/ty/createcraftedbeginning/content/breezes/breezecooler/coolerstates/BaseCoolerState;)V",
                    ordinal = 0
            ),
            index = 0
    )
    private BaseCoolerState createfreezingcold$cycleCreativeState(
            BaseCoolerState originalState
    ) {
        BaseCoolerState currentState =
                ((BreezeCoolerBlockEntityAccessor) cooler)
                    .createfreezingcold$getCurrentState();

        if (currentState instanceof SuperChilledCoolerState) {
            return new InactiveCoolerState();
        }
        if (currentState.getCoolantType()
                == BreezeCoolerBlockEntity.CoolantType.NORMAL) {
            return new SuperChilledCoolerState(
                    BreezeCoolerBlockEntity.getMaxCoolantCapacity(),
                    true
            );
        }

        return originalState;
    }
}