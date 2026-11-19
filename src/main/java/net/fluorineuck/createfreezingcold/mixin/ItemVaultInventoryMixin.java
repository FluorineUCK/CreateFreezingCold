package net.fluorineuck.createfreezingcold.mixin;

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;

import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirEvaporation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity$1",
        remap = false
)
public abstract class ItemVaultInventoryMixin extends ItemStackHandler {
    @Shadow
    @Final
    private ItemVaultBlockEntity this$0;

    @Inject(method = "onContentsChanged(I)V", at = @At("TAIL"))
    private void createfreezingcold$trackChangedSlot(int slot, CallbackInfo ci) {
        LiquidAirEvaporation.onVaultContentsChanged(this$0, slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        int offeredCount = stack.getCount();
        ItemStack remainder = super.insertItem(slot, stack, simulate);
        if (!simulate && remainder.getCount() < offeredCount) {
            LiquidAirEvaporation.onVaultItemInserted(this$0, slot);
        }
        return remainder;
    }
}
