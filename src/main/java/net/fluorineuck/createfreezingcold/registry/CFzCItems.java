package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;

import net.fluorineuck.createfreezingcold.content.liquefaction.dewar.DewarFlaskItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CFzCItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreateFreezingCold.MOD_ID);

    public static final DeferredItem<DewarFlaskItem> DEWAR_FLASK =
            ITEMS.register(
                    "dewar_flask",
                    () -> new DewarFlaskItem(
                            CFzCBlocks.DEWAR_FLASK.get(),
                            new Item.Properties().stacksTo(1)
                    )
            );
}