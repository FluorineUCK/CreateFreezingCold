package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;

import java.util.List;

public record LiquidAirDefinition(
        String gasName,
        int color,
        int temperature
) {
    public static final List<LiquidAirDefinition> ALL = List.of(
            new LiquidAirDefinition("natural_air", 0xFFBDEFFF, 50),
            new LiquidAirDefinition("ultrawarm_air", 0xFFFFAD70, 100),
            new LiquidAirDefinition("ethereal_air", 0xFFD6B5FF, 220),
            new LiquidAirDefinition("sculk_air", 0xFF4DB8AF, 10)
    );

    public String fluidName() {
        return "liquid_" + gasName;
    }
    public ResourceLocation gasId() {
        return ResourceLocation.fromNamespaceAndPath(
                "createcraftedbeginning",
                gasName
        );
    }
}