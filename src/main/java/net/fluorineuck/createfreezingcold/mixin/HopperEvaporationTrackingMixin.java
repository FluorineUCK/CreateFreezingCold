package net.fluorineuck.createfreezingcold.mixin;

import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirEvaporation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hoppers override setItem without calling BaseContainerBlockEntity.setItem. */
@Mixin(HopperBlockEntity.class)
public abstract class HopperEvaporationTrackingMixin {
    @Inject(method = "setItem(ILnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void createfreezingcold$trackHopperSlot(int slot, ItemStack stack, CallbackInfo ci) {
        LiquidAirEvaporation.trackContainerSlot((HopperBlockEntity) (Object) this, slot);
    }
}
