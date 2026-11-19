package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.content.liquefaction.dewar.DewarFlaskBlockEntity;

import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirBucketHandler;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CFzCCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new LiquidAirBucketHandler(stack), Items.BUCKET);
        for (var entry : CFzCFluids.ENTRIES) {
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, context) -> new LiquidAirBucketHandler(stack), entry.bucket.get());
        }
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CFzCBlockEntities.DEWAR_FLASK.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidHandlerItemStack(
                        CFzCDataComponents.DEWAR_CONTENT,
                        stack,
                        DewarFlaskBlockEntity.CAPACITY
                ),
                CFzCItems.DEWAR_FLASK.get()
        );
    }
}