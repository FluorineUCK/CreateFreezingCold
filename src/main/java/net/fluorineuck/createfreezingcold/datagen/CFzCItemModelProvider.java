package net.fluorineuck.createfreezingcold.datagen;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.registry.CFzCFluids;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CFzCItemModelProvider extends ItemModelProvider {

    public CFzCItemModelProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, CreateFreezingCold.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        var bucketParent = getExistingFile(
                ResourceLocation.fromNamespaceAndPath(
                        "neoforge",
                        "item/bucket"
                )
        );
        for (var entry : CFzCFluids.ENTRIES) {
            getBuilder(entry.definition.fluidName() + "_bucket")
                    .parent(bucketParent)
                    .customLoader(DynamicFluidContainerModelBuilder::begin)
                    .fluid(entry.source.get())
                    .applyTint(true);
        }
    }
}