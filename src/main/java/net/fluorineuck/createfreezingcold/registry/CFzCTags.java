package net.fluorineuck.createfreezingcold.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class CFzCTags {
    public static final TagKey<Item> SUPER_COOLING_FUELS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            "createfreezingcold",
                            "super_cooling_fuels"
                    )
            );
    private CFzCTags() {
    }
}