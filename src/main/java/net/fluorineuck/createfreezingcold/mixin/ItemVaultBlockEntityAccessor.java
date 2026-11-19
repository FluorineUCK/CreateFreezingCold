package net.fluorineuck.createfreezingcold.mixin;

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.foundation.ICapabilityProvider;

import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemVaultBlockEntity.class, remap = false)
public interface ItemVaultBlockEntityAccessor {
    @Accessor("itemCapability")
    @Nullable
    ICapabilityProvider<IItemHandler>
    createfreezingcold$getItemCapability();
}