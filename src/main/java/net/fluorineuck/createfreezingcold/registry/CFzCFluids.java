package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirDefinition;
import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.BucketItem;
import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirBucketItem;
import net.fluorineuck.createfreezingcold.content.liquefaction.LiquidAirBucketHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public final class CFzCFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreateFreezingCold.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, CreateFreezingCold.MOD_ID);

    public static final List<LiquidAirEntry> ENTRIES =
            LiquidAirDefinition.ALL.stream()
                    .map(LiquidAirEntry::new)
                    .toList();

    public static final class LiquidAirEntry {

        public final LiquidAirDefinition definition;
        public final DeferredHolder<FluidType, FluidType> type;
        public final DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
        public final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
        public final DeferredBlock<LiquidAirBlocks> block;
        public final DeferredItem<BucketItem> bucket;

        private LiquidAirEntry(LiquidAirDefinition definition) {
            this.definition = definition;
            String name = definition.fluidName();

            type = FLUID_TYPES.register(
                    name,
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .descriptionId(
                                            "fluid."
                                                    + CreateFreezingCold.MOD_ID
                                                    + "." + name
                                    )
                                    .temperature(definition.temperature())
                    ) {
                        @Override
                        public ItemStack getBucket(FluidStack stack) {
                            return LiquidAirBucketHandler.filledBucket(stack);
                        }
                    }
            );
            source = FLUIDS.register(
                    name,
                    () -> new BaseFlowingFluid.Source(properties())
            );
            flowing = FLUIDS.register(
                    "flowing_" + name,
                    () -> new BaseFlowingFluid.Flowing(properties())
            );
            block = CFzCBlocks.BLOCKS.registerBlock(
                    name,
                    properties -> new LiquidAirBlocks(source.get(), properties),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
            );
            bucket = CFzCItems.ITEMS.register(
                    name + "_bucket",
                    () -> new LiquidAirBucketItem(
                            source.get(),
                            new Item.Properties()
                                    .craftRemainder(Items.BUCKET)
                                    .stacksTo(1)
                    )
            );
        }

        private BaseFlowingFluid.Properties properties() {
            return new BaseFlowingFluid.Properties(
                    type,
                    source,
                    flowing
            )
                    .block(block)
                    .bucket(bucket);
        }
    }
}