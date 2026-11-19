package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.fluorineuck.createfreezingcold.content.liquefaction.dewar.DewarFlaskBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CFzCBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    CreateFreezingCold.MOD_ID
            );

    public static final Supplier<BlockEntityType<DewarFlaskBlockEntity>> DEWAR_FLASK =
            BLOCK_ENTITY_TYPES.register(
                    "dewar_flask",
                    () -> BlockEntityType.Builder.of(
                            DewarFlaskBlockEntity::new,
                            CFzCBlocks.DEWAR_FLASK.get()
                    ).build(null)
            );
}