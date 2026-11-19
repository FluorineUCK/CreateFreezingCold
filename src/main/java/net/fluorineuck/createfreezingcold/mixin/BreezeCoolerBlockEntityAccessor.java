package net.fluorineuck.createfreezingcold.mixin;

import net.ty.createcraftedbeginning.content.breezes.breezecooler.BreezeCoolerBlockEntity;
import net.ty.createcraftedbeginning.content.breezes.breezecooler.coolerstates.BaseCoolerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BreezeCoolerBlockEntity.class, remap = false)
public interface BreezeCoolerBlockEntityAccessor {
    @Accessor(value = "currentState", remap = false)
    BaseCoolerState createfreezingcold$getCurrentState();
}