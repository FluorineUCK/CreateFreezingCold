package net.fluorineuck.createfreezingcold.registry;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CFzCAdvancements {
    private static final double WITNESS_RADIUS = 16.0;
    private static void triggerWitnesses(
            ServerLevel level,
            Vec3 position,
            PlayerTrigger trigger
    ) {
        double radiusSquared = WITNESS_RADIUS * WITNESS_RADIUS;
        for (var player : level.players()) {
            if (!player.isSpectator()
                    && player.distanceToSqr(position) <= radiusSquared) {
                trigger.trigger(player);
            }
        }
    }

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(
                    Registries.TRIGGER_TYPE,
                    CreateFreezingCold.MOD_ID
            );

    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger>
            LIQUID_AIR_IN_VAULT =
            TRIGGERS.register(
                    "liquid_air_in_vault",
                    PlayerTrigger::new
            );

    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger>
            VAULT_EXPLODED =
            TRIGGERS.register(
                    "vault_exploded",
                    PlayerTrigger::new
            );

    private CFzCAdvancements() {}
    public static void onLiquidAirInserted(ServerLevel level, Vec3 position) {
        triggerWitnesses(level, position, LIQUID_AIR_IN_VAULT.get());
    }
    public static void onVaultExploded(ServerLevel level, Vec3 position) {
        triggerWitnesses(level, position, VAULT_EXPLODED.get());
    }
}