package net.fluorineuck.createfreezingcold.client;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.registry.CFzCFluids;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

@EventBusSubscriber(
        modid = CreateFreezingCold.MOD_ID,
        value = Dist.CLIENT
)
public final class CFzCClientEvents {

    @SubscribeEvent
    public static void registerCoolerModels(ModelEvent.RegisterAdditional event) {
        // Explicit registration also makes this independent of Flywheel subscriber order.
        event.register(ModelResourceLocation.standalone(SuperChilledModels.IDLE.modelLocation()));
        event.register(ModelResourceLocation.standalone(SuperChilledModels.ACTIVE.modelLocation()));
    }

    private static final ResourceLocation STILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "block/water_still"
            );
    private static final ResourceLocation FLOWING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "block/water_flow"
            );

    @SubscribeEvent
    public static void registerFluidExtensions(
            RegisterClientExtensionsEvent event
    ) {
        for (var entry : CFzCFluids.ENTRIES) {
            int color = entry.definition.color();
            event.registerFluidType(
                    new IClientFluidTypeExtensions() {
                        @Override
                        public @NotNull ResourceLocation getStillTexture() {
                            return STILL_TEXTURE;
                        }
                        @Override
                        public @NotNull ResourceLocation getFlowingTexture() {
                            return FLOWING_TEXTURE;
                        }
                        @Override
                        public int getTintColor() {
                            return color;
                        }
                    },
                    entry.type.get()
            );
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        var colors = new DynamicFluidContainerModel.Colors();
        for (var entry : CFzCFluids.ENTRIES) {
            event.register(colors, entry.bucket.get());
        }
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var entry : CFzCFluids.ENTRIES) {
                ItemBlockRenderTypes.setRenderLayer(
                        entry.source.get(),
                        RenderType.translucent()
                );
                ItemBlockRenderTypes.setRenderLayer(
                        entry.flowing.get(),
                        RenderType.translucent()
                );
            }
        });
    }
}
