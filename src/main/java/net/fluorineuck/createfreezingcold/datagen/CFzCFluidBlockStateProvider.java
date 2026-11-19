package net.fluorineuck.createfreezingcold.datagen;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.registry.CFzCFluids;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CFzCFluidBlockStateProvider extends BlockStateProvider {

    public CFzCFluidBlockStateProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, CreateFreezingCold.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var model = models()
                .getBuilder("liquid_air")
                .texture("particle", mcLoc("block/water_still"));

        for (var entry : CFzCFluids.ENTRIES) {
            simpleBlock(entry.block.get(), model);
        }
    }
}