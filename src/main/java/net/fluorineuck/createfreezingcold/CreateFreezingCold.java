package net.fluorineuck.createfreezingcold;

import net.minecraft.resources.ResourceLocation;

import net.fluorineuck.createfreezingcold.registry.CFzCBlocks;
import net.fluorineuck.createfreezingcold.registry.CFzCItems;
import net.fluorineuck.createfreezingcold.registry.CFzCBlockEntities;
import net.fluorineuck.createfreezingcold.registry.CFzCCapabilities;
import net.fluorineuck.createfreezingcold.registry.CFzCDataComponents;
import net.fluorineuck.createfreezingcold.registry.CFzCFluids;
import net.fluorineuck.createfreezingcold.registry.CFzCAdvancements;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;


@Mod(CreateFreezingCold.MOD_ID)
public final class CreateFreezingCold {
    public static final String MOD_ID = "createfreezingcold";

    public CreateFreezingCold(IEventBus modEventBus) {
        modEventBus.addListener(CFzCCapabilities::register);
        CFzCBlocks.BLOCKS.register(modEventBus);
        CFzCItems.ITEMS.register(modEventBus);
        CFzCBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        CFzCDataComponents.DATA_COMPONENTS.register(modEventBus);
        CFzCFluids.FLUID_TYPES.register(modEventBus);
        CFzCFluids.FLUIDS.register(modEventBus);
        CFzCAdvancements.TRIGGERS.register(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
