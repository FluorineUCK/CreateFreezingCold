package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.fluorineuck.createfreezingcold.registry.CFzCFluids;
import net.fluorineuck.createfreezingcold.registry.CFzCDataComponents;
import net.fluorineuck.createfreezingcold.registry.CFzCAdvancements;
import net.fluorineuck.createfreezingcold.mixin.ItemVaultBlockEntityAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryWrapper;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.BitSet;
import java.util.function.IntPredicate;
import java.util.Optional;


/** Central entry point for evaporation tracking and updates */
public final class LiquidAirEvaporation {
    // Runtime targets are restored by login/entity-load events
    private static final Set<ItemEntity> DROPPED_ITEMS = new HashSet<>();
    private static final Set<UUID> TRACKED_PLAYERS = new HashSet<>();
    // Derived indexes only; persistent target positions remain in SavedData.
    private static final Map<ResourceKey<Level>, Map<BlockPos, ContainerSlotCache>> CONTAINER_SLOTS =
            new HashMap<>();

    public static final int UPDATE_INTERVAL = 100;
    public static final int EVAPORATION_DURATION = 1000;

    private static final int DEFAULT_TEMPERATURE = 293;
    private static final int ULTRAWARM_TEMPERATURE = 330;

    private static final double REFERENCE_TEMPERATURE_DIFFERENCE = 213.0;

    private static final float VAULT_EXPLOSION_POWER = 4.0F;

    private static int elapsedSimulationTicks;
    private static Set<Explosion> observedVaultDetonations;

    private LiquidAirEvaporation() {
    }

    /** Local slot selection; inventory traversal is kept separate from timer updates. */
    static final class ContainerSlotCache {
        private Object inventory;
        private int size = -1;
        private final BitSet slots = new BitSet();

        void record(Object inventory, int slot) {
            if (this.inventory != inventory) {
                this.inventory = inventory;
                size = -1;
                slots.clear();
            }
            slots.set(slot);
        }
        void forget(int slot) {slots.clear(slot);}

        BitSet resolve(Object inventory, int size, IntPredicate isTarget) {
            boolean rebuild = this.inventory != inventory || this.size != size;
            if (!rebuild) {
                for (int slot = slots.nextSetBit(0); slot >= 0; slot = slots.nextSetBit(slot + 1)) {
                    if (slot >= size || !isTarget.test(slot)) {
                        rebuild = true;
                        break;
                    }
                }
            }
            if (rebuild) {
                slots.clear();
                for (int slot = 0; slot < size; slot++) {
                    if (isTarget.test(slot)) {
                        slots.set(slot);
                    }
                }
                this.inventory = inventory;
                this.size = size;
            }
            // Writes during evaporation can call registration again
            return (BitSet) slots.clone();
        }
    }

    private static ContainerSlotCache containerSlots(ServerLevel level, BlockPos pos) {
        return CONTAINER_SLOTS.computeIfAbsent(level.dimension(), key -> new HashMap<>())
                .computeIfAbsent(pos.immutable(), key -> new ContainerSlotCache());
    }

    private static void forgetContainerSlots(ServerLevel level, BlockPos pos) {
        var slots = CONTAINER_SLOTS.get(level.dimension());
        if (slots != null) {
            slots.remove(pos);
        }
    }

    private static void untrackContainer(ServerLevel level, LiquidAirBlockTimers data, BlockPos pos) {
        forgetContainerSlots(level, pos);
        data.untrackContainer(pos);
    }

    // Thermal parameters and per-stack progress
    public enum ThermalContext {
        EXPOSED_BUCKET(1.0),
        CONTAINER(0.5), // Ordinary chests and hoppers share the fixed container coefficient.
        ITEM_VAULT(0.1),
        WORLD_FLUID(1.0),
        DEWAR(0.0);

        private final double conductivity;

        ThermalContext(double conductivity) {
            this.conductivity = conductivity;
        }

        public double conductivity() {
            return conductivity;
        }
    }

    public static double evaporationIncrement(
            ServerLevel level,
            BlockPos pos,
            int fluidTemperature,
            ThermalContext context
    ) {
        double temperatureDifference = Math.max(
                0.0,
                (double) environmentTemperature(level, pos) - fluidTemperature
        );

        return UPDATE_INTERVAL
                * (temperatureDifference / REFERENCE_TEMPERATURE_DIFFERENCE)
                * context.conductivity();
    }

    public static int environmentTemperature(
            ServerLevel level,
            BlockPos pos
    ) {
        return level.dimensionType().ultraWarm()
                ? ULTRAWARM_TEMPERATURE
                : DEFAULT_TEMPERATURE;
    }

    public static double evaporationProgress(ItemStack stack) {
        int whole = stack.getOrDefault(CFzCDataComponents.EVAPORATION_PROGRESS.get(), 0);
        return VolumeMath.clampProgress(whole
                + stack.getOrDefault(CFzCDataComponents.EVAPORATION_REMAINDER.get(), 0.0));
    }

    public static void setProgress(ItemStack stack, double value) {
        value = VolumeMath.clampProgress(value);
        int whole = (int) value;
        stack.set(CFzCDataComponents.EVAPORATION_PROGRESS.get(), whole);
        stack.set(CFzCDataComponents.EVAPORATION_REMAINDER.get(), value - whole);
    }

    public static int bucketProgressToVolume(ItemStack stack) {
        return VolumeMath.toVolume(evaporationProgress(stack));
    }

    public static void volumeToBucketProgress(ItemStack stack, int volume) {
        setProgress(stack, VolumeMath.toProgress(volume));
    }

    static final class VolumeMath {
        static double clampProgress(double progress) {
            return Double.isFinite(progress)
                    ? Math.clamp(progress, 0.0, EVAPORATION_DURATION) : EVAPORATION_DURATION;
        }

        static int toVolume(double progress) {
            double volume = FluidType.BUCKET_VOLUME
                    * (EVAPORATION_DURATION - clampProgress(progress)) / EVAPORATION_DURATION;
            return Math.clamp((int) Math.floor(Math.nextUp(volume)), 0, FluidType.BUCKET_VOLUME);
        }

        static double toProgress(int volume) {
            int bounded = Math.clamp(volume, 0, FluidType.BUCKET_VOLUME);
            return (double) EVAPORATION_DURATION * (FluidType.BUCKET_VOLUME - bounded)
                    / FluidType.BUCKET_VOLUME;
        }
    }

    public static boolean advanceBucketTimer(
            ServerLevel level,
            BlockPos pos,
            ItemStack stack
    ) {
        return advanceBucketTimer(
                level,
                pos,
                stack,
                ThermalContext.EXPOSED_BUCKET
        );
    }

    public static boolean advanceBucketTimer(
            ServerLevel level,
            BlockPos pos,
            ItemStack stack,
            ThermalContext context
    ) {
        if (stack.isEmpty()) {return false;}
        for (var entry : CFzCFluids.ENTRIES) {
            if (!stack.is(entry.bucket.get())) {continue;}

            double increment = evaporationIncrement(
                    level,
                    pos,
                    entry.definition.temperature(),
                    context
            );

            if (increment <= 0.0) {return false;}
            double next = Math.min(
                    evaporationProgress(stack) + increment,
                    EVAPORATION_DURATION
            );
            setProgress(stack, next);
            return next >= EVAPORATION_DURATION;
        }
        return false;
    }

    public static boolean isVolatileBucket(ItemStack stack) {
        if (stack.isEmpty()) {return false;}

        for (var entry : CFzCFluids.ENTRIES) {
            if (stack.is(entry.bucket.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean tickContainer(
            ServerLevel level,
            BlockPos pos,
            Container container
    ) {
        var cache = containerSlots(level, pos);
        BitSet slots = cache.resolve(
                container,
                container.getContainerSize(),
                slot -> isVolatileBucket(container.getItem(slot))
        );
        boolean changed = false;
        boolean hasRemainingBucket = false;

        for (int slot = slots.nextSetBit(0); slot >= 0; slot = slots.nextSetBit(slot + 1)) {
            ItemStack stack = container.getItem(slot);
            if (!isVolatileBucket(stack)) {
                continue;
            }

            ItemStack updated = stack.copy();
            boolean evaporated = advanceBucketTimer(
                    level,
                    pos,
                    updated,
                    ThermalContext.CONTAINER
            );

            if (evaporated) {
                cache.forget(slot);
                container.setItem(
                        slot,
                        new ItemStack(Items.BUCKET, stack.getCount())
                );
                changed = true;
            } else {
                hasRemainingBucket = true;
                if (!ItemStack.matches(stack, updated)) {
                    container.setItem(slot, updated);
                    changed = true;
                }
            }
        }
        if (changed) {
            container.setChanged();
        }
        return hasRemainingBucket;
    }

    private static boolean tickVault(
            ServerLevel level,
            ItemVaultBlockEntity vault,
            Set<BlockPos> pendingExplosions
    ) {
        if (vault.isRemoved()) {return false;}

        BlockPos controllerPos = vault.getController();
        if (!level.isLoaded(controllerPos)) {
            return true;
        }
        if (!(level.getBlockEntity(controllerPos)
                instanceof ItemVaultBlockEntity controller)
                || controller.isRemoved()
                || !controller.isController()) {
            return true;
        }

        return tickItemHandler(
                level,
                vault.getBlockPos(),
                vault.getInventoryOfBlock(),
                ThermalContext.ITEM_VAULT,
                () -> notifyVaultInventoryChanged(controller),
                () -> pendingExplosions.add(controllerPos.immutable())
        );
    }

    public static boolean tickItemHandler(
            ServerLevel level,
            BlockPos pos,
            IItemHandlerModifiable handler,
            ThermalContext context,
            Runnable onChanged,
            Runnable onEvaporated
    ) {
        var cache = containerSlots(level, pos);
        BitSet slots = cache.resolve(
                handler,
                handler.getSlots(),
                slot -> isVolatileBucket(handler.getStackInSlot(slot))
        );
        boolean changed = false;
        boolean evaporatedAny = false;
        boolean hasRemainingBucket = false;

        for (int slot = slots.nextSetBit(0); slot >= 0; slot = slots.nextSetBit(slot + 1)) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!isVolatileBucket(stack)) {
                continue;
            }

            ItemStack updated = stack.copy();

            if (advanceBucketTimer(level, pos, updated, context)) {
                cache.forget(slot);
                handler.setStackInSlot(
                        slot,
                        new ItemStack(Items.BUCKET, stack.getCount())
                );
                changed = true;
                evaporatedAny = true;
            } else {
                hasRemainingBucket = true;

                if (!ItemStack.matches(stack, updated)) {
                    handler.setStackInSlot(slot, updated);
                    changed = true;
                }
            }
        }
        if (changed) {onChanged.run();}
        if (evaporatedAny) {onEvaporated.run();}
        return hasRemainingBucket;
    }

    private static void notifyVaultInventoryChanged(
            ItemVaultBlockEntity controller
    ) {
        var provider = ((ItemVaultBlockEntityAccessor) controller)
                .createfreezingcold$getItemCapability();
        if (provider == null) {return;}
        if (provider.getCapability() instanceof VersionedInventoryWrapper wrapper) {
            wrapper.incrementVersion();
        }
    }

    public static void recordExplosionDetonation(Explosion explosion) {
        if (observedVaultDetonations != null) {
            observedVaultDetonations.add(explosion);
        }
    }

    private static void explodeVault(
            ServerLevel level,
            BlockPos controllerPos
    ) {
        Vec3 center = Vec3.atCenterOf(controllerPos);
        ExplosionDamageCalculator calculator = new ExplosionDamageCalculator() {
            @Override
            public Optional<Float> getBlockExplosionResistance(
                    Explosion explosion,
                    BlockGetter reader,
                    BlockPos pos,
                    BlockState state,
                    FluidState fluid
            ) {
                if (pos.equals(controllerPos)) {
                    return Optional.of(0.0F);
                }
                return super.getBlockExplosionResistance(
                        explosion, reader, pos, state, fluid
                );
            }
        };

        Set<Explosion> previous = observedVaultDetonations;
        Set<Explosion> observed = new HashSet<>();
        observedVaultDetonations = observed;
        try {
            Explosion explosion = level.explode(
                    null,
                    null,
                    calculator,
                    center,
                    VAULT_EXPLOSION_POWER,
                    false,
                    Level.ExplosionInteraction.BLOCK
            );
            if (observed.contains(explosion)) {
                CFzCAdvancements.onVaultExploded(level, center);
            }
        } finally {
            observedVaultDetonations = previous;
        }
    }

    // Registration & lifecycle
    public static void trackDroppedItem(ItemEntity itemEntity) {
        if (itemEntity.level() instanceof ServerLevel && isVolatileBucket(itemEntity.getItem())) {
            DROPPED_ITEMS.add(itemEntity);
        }
    }
    public static void untrackDroppedItem(ItemEntity itemEntity) {
        if (itemEntity.level() instanceof ServerLevel) {
            DROPPED_ITEMS.remove(itemEntity);
        }
    }

    public static void untrackPlayer(Player player) {
        TRACKED_PLAYERS.remove(player.getUUID());
    }

    public static void clearRuntimeTracking() {
        // Do not clear SavedData！
        DROPPED_ITEMS.clear();
        TRACKED_PLAYERS.clear();
        CONTAINER_SLOTS.clear();
        elapsedSimulationTicks = 0;
        observedVaultDetonations = null;
    }

    public static void trackBlock(ServerLevel level, BlockPos pos) {
        LiquidAirBlockTimers.get(level).track(pos);
    }

    public static void untrackBlock(ServerLevel level, BlockPos pos) {
        LiquidAirBlockTimers.get(level).untrack(pos);
    }

    public static void queuePlayerCheck(Player player) {
        if (player.level() instanceof ServerLevel) {
            TRACKED_PLAYERS.add(player.getUUID());
        }
    }

    public static void trackPlayer(Player player) {
        if (!(player.level() instanceof ServerLevel)) {
            return;
        }
        if (isVolatileBucket(player.containerMenu.getCarried())) {
            TRACKED_PLAYERS.add(player.getUUID());
            return;
        }
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isVolatileBucket(
                    inventory.getItem(slot)
            )) {
                TRACKED_PLAYERS.add(player.getUUID());
                return;
            }
        }
        TRACKED_PLAYERS.remove(player.getUUID());
    }

    public static void trackContainerSlot(RandomizableContainerBlockEntity container, int slot) {
        if (!(container instanceof ChestBlockEntity || container instanceof HopperBlockEntity)
                || container.isRemoved()
                || !(container.getLevel() instanceof ServerLevel level)
                || container.getLootTable() != null
                || slot < 0 || slot >= container.getContainerSize()) {
            return;
        }

        if (!isVolatileBucket(container.getItem(slot))) {
            return;
        }
        BlockPos pos = container.getBlockPos();
        LiquidAirBlockTimers.get(level).trackContainer(pos);
        containerSlots(level, pos).record(container, slot);
    }

    private static boolean trackVaultSlot(ItemVaultBlockEntity vault, int slot) {
        if (vault.isRemoved()
                || !(vault.getLevel() instanceof ServerLevel level)) {
            return false;
        }

        var inventory = vault.getInventoryOfBlock();
        if (slot < 0 || slot >= inventory.getSlots()
                || !isVolatileBucket(inventory.getStackInSlot(slot))) {
            return false;
        }

        LiquidAirBlockTimers.get(level).trackContainer(vault.getBlockPos());
        containerSlots(level, vault.getBlockPos()).record(inventory, slot);
        return true;
    }

    public static void onVaultContentsChanged(ItemVaultBlockEntity vault, int slot) {
        trackVaultSlot(vault, slot);
    }

    public static void onVaultItemInserted(
            ItemVaultBlockEntity vault,
            int slot
    ) {
        if (!trackVaultSlot(vault, slot)) {return;}
        CFzCAdvancements.onLiquidAirInserted(
                (ServerLevel) vault.getLevel(),
                Vec3.atCenterOf(vault.getBlockPos())
        );
    }

    // Scheduler for all registered targets
    public static void tick(MinecraftServer server) {
        if (!server.tickRateManager().runsNormally()) {
            return;
        }

        if (++elapsedSimulationTicks < UPDATE_INTERVAL) {
            return;
        }
        elapsedSimulationTicks = 0;
        for (ServerLevel level : server.getAllLevels()) {
            var data = LiquidAirBlockTimers.get(level);
            tickBlocks(level, data);
            tickContainers(level, data);
        }
        tickPlayers(server);
        tickDroppedItems();
    }

    private static void tickPlayers(MinecraftServer server) {
        for (UUID playerId : Set.copyOf(TRACKED_PLAYERS)) {
            var player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                TRACKED_PLAYERS.remove(playerId);
                continue;
            }

            var inventory = player.getInventory();
            boolean changed = false;
            boolean hasRemainingBucket = false;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!isVolatileBucket(stack)) {
                    continue;
                }

                double before = evaporationProgress(stack);
                if (advanceBucketTimer(
                        player.serverLevel(),
                        player.blockPosition(),
                        stack
                )) {
                    inventory.setItem(
                            slot,
                            new ItemStack(Items.BUCKET, stack.getCount())
                    );
                    changed = true;
                } else {
                    hasRemainingBucket = true;
                    if (evaporationProgress(stack) != before) {
                        changed = true;
                    }
                }
            }
            if (changed) {
                inventory.setChanged();
            }

            var menu = player.containerMenu;
            ItemStack carried = menu.getCarried();
            if (isVolatileBucket(carried)) {
                ItemStack updated = carried.copy();
                if (advanceBucketTimer(
                        player.serverLevel(),
                        player.blockPosition(),
                        updated
                )) {
                    menu.setCarried(new ItemStack(Items.BUCKET, carried.getCount()));
                    changed = true;
                } else {
                    hasRemainingBucket = true;
                    if (!ItemStack.matches(carried, updated)) {
                        menu.setCarried(updated);
                        changed = true;
                    }
                }
            }
            if (changed) {
                menu.broadcastChanges();
            }
            if (!hasRemainingBucket) {
                TRACKED_PLAYERS.remove(playerId);
            }
        }
    }

    private static void tickDroppedItems() {

        var iterator = DROPPED_ITEMS.iterator();
        while (iterator.hasNext()) {

            ItemEntity itemEntity = iterator.next();
            if (itemEntity.isRemoved()
                    || !(itemEntity.level() instanceof ServerLevel level)) {
                iterator.remove();
                continue;
            }

            ItemStack stack = itemEntity.getItem();
            ItemStack updated = stack.copy();

            if (advanceBucketTimer(
                    level,
                    itemEntity.blockPosition(),
                    updated
            )) {
                itemEntity.setItem(
                        new ItemStack(Items.BUCKET, stack.getCount())
                );
                iterator.remove();
            } else if (!ItemStack.matches(stack, updated)) {
                itemEntity.setItem(updated);
            }
        }
    }

    private static void tickBlocks(ServerLevel level, LiquidAirBlockTimers data) {
        for (BlockPos pos : data.trackedBlocks()) {
            Double elapsed = data.blockExactProgress(pos);
            if (elapsed == null || !level.isLoaded(pos)) {
                continue;
            }

            var state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof LiquidAirBlocks)) {
                data.untrack(pos);
                continue;
            }

            int fluidTemperature = state
                    .getFluidState()
                    .getType()
                    .getFluidType()
                    .getTemperature();
            if (environmentTemperature(level, pos)
                    <= fluidTemperature) {
                continue;
            }

            double next = Math.min(
                    elapsed + UPDATE_INTERVAL,
                    EVAPORATION_DURATION
            );
            if (next != elapsed) {
                data.updateBlockProgress(pos, next);
            }
            if (next >= EVAPORATION_DURATION) {
                level.setBlock(
                        pos,
                        Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL
                );
            }
        }
    }

    private static void tickContainers(ServerLevel level, LiquidAirBlockTimers data) {
        Set<BlockPos> pendingExplosions = new HashSet<>();
        for (BlockPos pos : data.trackedContainers()) {
            if (!level.isLoaded(pos)) {
                forgetContainerSlots(level, pos);
                continue;
            }

            var blockEntity = level.getBlockEntity(pos);
            boolean hasRemainingBucket;
            if (blockEntity instanceof ChestBlockEntity chest) {
                hasRemainingBucket = tickContainer(level, pos, chest);
            } else if (blockEntity instanceof HopperBlockEntity hopper) {
                hasRemainingBucket = tickContainer(level, pos, hopper);
            } else if (blockEntity instanceof ItemVaultBlockEntity vault) {
                hasRemainingBucket = tickVault(level, vault, pendingExplosions);
            } else {
                untrackContainer(level, data, pos);
                continue;
            }
            if (!hasRemainingBucket) {
                untrackContainer(level, data, pos);
            }
        }

        // Explode after inventory traversal
        for (BlockPos controllerPos : pendingExplosions) {
            if (!level.isLoaded(controllerPos)) {
                continue;
            }

            if (level.getBlockEntity(controllerPos)
                    instanceof ItemVaultBlockEntity controller
                    && !controller.isRemoved()
                    && controller.isController()) {
                explodeVault(level, controllerPos);
            }
        }
    }
}
