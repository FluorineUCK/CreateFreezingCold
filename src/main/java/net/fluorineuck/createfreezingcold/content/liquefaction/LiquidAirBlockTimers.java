package net.fluorineuck.createfreezingcold.content.liquefaction;

import net.fluorineuck.createfreezingcold.CreateFreezingCold;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LiquidAirBlockTimers extends SavedData {
    private static final String DATA_NAME =
            CreateFreezingCold.MOD_ID + "_liquid_air_timers";

    private static final SavedData.Factory<LiquidAirBlockTimers> FACTORY =
            new SavedData.Factory<>(
                    LiquidAirBlockTimers::new,
                    LiquidAirBlockTimers::load
            );

    public static LiquidAirBlockTimers get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private final Map<BlockPos, Integer> timers = new HashMap<>();
    private final Map<BlockPos, Double> remainders = new HashMap<>();
    private final Set<BlockPos> containers = new HashSet<>();

    public void track(BlockPos pos) {
        if (timers.putIfAbsent(pos.immutable(), 0) == null) {
            setDirty();
        }
    }

    public void untrack(BlockPos pos) {
        if (timers.remove(pos) != null) {
            remainders.remove(pos);
            setDirty();
        }
    }

    public void trackContainer(BlockPos pos) {
        if (containers.add(pos.immutable())) {
            setDirty();
        }
    }

    public boolean isContainerTracked(BlockPos pos) {
        return containers.contains(pos);
    }

    public void untrackContainer(BlockPos pos) {
        if (containers.remove(pos)) {
            setDirty();
        }
    }

    /** A snapshots allow callbacks to register/remove positions during an update. */
    public Set<BlockPos> trackedBlocks() {
        return Set.copyOf(timers.keySet());
    }

    public Set<BlockPos> trackedContainers() {
        return Set.copyOf(containers);
    }

    public @Nullable Integer blockProgress(BlockPos pos) {
        return timers.get(pos);
    }

    public @Nullable Double blockExactProgress(BlockPos pos) {
        Integer whole = timers.get(pos);
        return whole == null ? null : whole + remainders.getOrDefault(pos, 0.0);
    }

    public void updateBlockProgress(BlockPos pos, double progress) {
        Double previous = blockExactProgress(pos);
        if (previous == null) {return;}
        progress = LiquidAirEvaporation.VolumeMath.clampProgress(progress);
        if (Double.compare(previous, progress) != 0) {
            int whole = (int) progress;
            timers.put(pos.immutable(), whole);
            if (progress == whole) {
                remainders.remove(pos);
            } else {
                remainders.put(pos.immutable(), progress - whole);
            }
            setDirty();
        }
    }

    private static LiquidAirBlockTimers load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        var data = new LiquidAirBlockTimers();

        ListTag entries = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            BlockPos pos = BlockPos.of(entry.getLong("Pos"));
            int elapsed = Math.clamp(
                    entry.getInt("Ticks"),
                    0,
                    LiquidAirEvaporation.EVAPORATION_DURATION
            );
            data.timers.put(pos, elapsed);
            double remainder = entry.getDouble("Remainder");
            if (Double.isFinite(remainder) && remainder > 0.0 && remainder < 1.0
                    && elapsed < LiquidAirEvaporation.EVAPORATION_DURATION) {
                data.remainders.put(pos, remainder);
            }
        }
        for (long packedPos : tag.getLongArray("Containers")) {
            data.containers.add(BlockPos.of(packedPos));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(
            CompoundTag tag,
            HolderLookup.@NotNull Provider registries
    ) {
        ListTag entries = new ListTag();
        timers.forEach((pos, elapsed) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", pos.asLong());
            entry.putInt("Ticks", elapsed);
            double remainder = remainders.getOrDefault(pos, 0.0);
            if (remainder != 0.0) {entry.putDouble("Remainder", remainder);}
            entries.add(entry);
        });

        tag.put("Entries", entries);
        tag.putLongArray(
                "Containers",
                containers.stream()
                        .mapToLong(BlockPos::asLong)
                        .toArray()
        );
        return tag;
    }
}
