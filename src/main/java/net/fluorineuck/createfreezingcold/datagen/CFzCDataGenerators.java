package net.fluorineuck.createfreezingcold.datagen;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CreateFreezingCold.MOD_ID)
public final class CFzCDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(
                event.includeClient(),
                new CFzCItemModelProvider(
                        generator.getPackOutput(),
                        event.getExistingFileHelper()
                )
        );
    }
}