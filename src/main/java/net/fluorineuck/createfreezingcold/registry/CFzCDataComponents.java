package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;


import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CFzCDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    CreateFreezingCold.MOD_ID
            );

    public static final Supplier<DataComponentType<SimpleFluidContent>> DEWAR_CONTENT =
            DATA_COMPONENTS.registerComponentType(
                    "dewar_content",
                    builder -> builder
                            .persistent(SimpleFluidContent.CODEC)
                            .networkSynchronized(SimpleFluidContent.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<Integer>> EVAPORATION_PROGRESS =
            DATA_COMPONENTS.registerComponentType(
                    "evaporation_progress",
                    builder -> builder
                            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
            );

    public static final Supplier<DataComponentType<Double>> EVAPORATION_REMAINDER =
            DATA_COMPONENTS.registerComponentType(
                    "evaporation_remainder",
                    builder -> builder
                            .persistent(Codec.doubleRange(0.0, 1.0))
                            .networkSynchronized(ByteBufCodecs.DOUBLE)
            );
}