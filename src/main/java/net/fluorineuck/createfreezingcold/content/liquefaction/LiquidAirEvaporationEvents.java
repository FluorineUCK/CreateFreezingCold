package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/** NeoForge event adapters */
@EventBusSubscriber(modid = CreateFreezingCold.MOD_ID)
public final class LiquidAirEvaporationEvents {
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel
                && event.getEntity() instanceof ItemEntity itemEntity) {
            LiquidAirEvaporation.trackDroppedItem(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel
                && event.getEntity() instanceof ItemEntity itemEntity) {
            LiquidAirEvaporation.untrackDroppedItem(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LiquidAirEvaporation.clearRuntimeTracking();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LiquidAirEvaporation.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Post event) {
        if (LiquidAirEvaporation.isVolatileBucket(event.getOriginalStack())) {
            LiquidAirEvaporation.trackPlayer(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        LiquidAirEvaporation.trackPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LiquidAirEvaporation.untrackPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        LiquidAirEvaporation.trackPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        LiquidAirEvaporation.trackPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel) {
            LiquidAirEvaporation.recordExplosionDetonation(
                    event.getExplosion()
            );
        }
    }
}
