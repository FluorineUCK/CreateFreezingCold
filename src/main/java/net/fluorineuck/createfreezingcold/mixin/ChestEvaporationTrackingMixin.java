package net.fluorineuck.createfreezingcold.mixin;

import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirEvaporation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseContainerBlockEntity.class)
public abstract class ChestEvaporationTrackingMixin {
    @Inject(
            method = "setItem(ILnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void createfreezingcold$trackChestSlot(
            int slot,
            ItemStack stack,
            CallbackInfo ci
    ) {
        BaseContainerBlockEntity self =
                (BaseContainerBlockEntity) (Object) this;
        if (self instanceof ChestBlockEntity chest) {
            LiquidAirEvaporation.trackContainerSlot(chest, slot);
        }
    }
}
