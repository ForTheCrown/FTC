package net.forthecrown.regions;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.text.Messages;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.UUID;

/**
 * Stores data about users that live within a region
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RegionResidency {
    /**
     * Unknown move in time stamp, -1
     */
    public static final long UNKNOWN_MOVEIN = -1;

    @Getter
    private final PopulationRegion region;

    @Getter
    private final Map<UUID, Resident> entries = new Object2ObjectOpenHashMap<>();

    /**
     * Gets a residency entry for the given UUID
     * @param uuid the UUID to get the entry of
     * @return The gotten or created entry
     */
    private Resident getEntry(UUID uuid) {
        return entries.computeIfAbsent(uuid, uuid1 -> new Resident());
    }

    public void moveIn(UUID uuid) {
        getEntry(uuid).setDirectMoveIn(System.currentTimeMillis());
    }

    public void moveOut(UUID uuid) {
        var entry = entries.get(uuid);

        if (entry == null) {
            return;
        }

        entry.setDirectMoveIn(0);

        if (entry.isEmpty()) {
            entries.remove(uuid);
        }
    }

    public void setHome(UUID uuid, String home) {
        getEntry(uuid).addHome(home, System.currentTimeMillis());
    }

    public void removeHome(UUID uuid, String home) {
        var entry = entries.get(uuid);

        if (entry == null) {
            return;
        }

        entry.removeHome(home);

        if (entry.isEmpty()) {
            entries.remove(uuid);
        }
    }

    public Tag save() {
        ListTag list = new ListTag();

        for (var e: entries.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("resident", e.getKey());

            Resident entry = e.getValue();

            if (entry.isDirectResident()) {
                tag.putLong("directMoveIn", entry.directMoveIn);
            }

            if (entry.hasHomes()) {
                CompoundTag homes = new CompoundTag();
                entry.homes.forEach(homes::putLong);

                tag.put("homes", homes);
            }

            list.add(tag);
        }

        return list;
    }

    public void load(ListTag t) {
        clear();

        for (Tag listT: t) {
            CompoundTag tag = (CompoundTag) listT;

            UUID resident = tag.getUUID("resident");
            Resident entry = new Resident();

            entry.directMoveIn = tag.getLong("directMoveIn");

            if(tag.contains("homes")) {
                CompoundTag homesTag = tag.getCompound("homes");

                for (var e: homesTag.tags.entrySet()) {
                    entry.homesSafe().put(e.getKey(), ((LongTag) e.getValue()).getAsLong());
                }
            }

            entries.put(resident, entry);
        }
    }

    private void clearEmpty() {
        entries.entrySet().removeIf(entry -> {
            Resident e = entry.getValue();
            boolean notDirect = !e.isDirectResident();
            boolean noHomes = !e.hasHomes();

            return noHomes && notDirect;
        });
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        clearEmpty();
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    /**
     * Writes info about the region residency
     * @param writer The writer to write the display info to
     */
    public void write(TextWriter writer) {
        if(isEmpty()) {
            return;
        }

        String headerFormat;
        if (size() == 1) {
            headerFormat = "&e{0}&r person lives in this region &6({1})&r.";
        } else {
            headerFormat = "&e{0}&r people live in this region &6({1})&r.";
        }

        writer.formattedLine(headerFormat, NamedTextColor.GRAY, size(), region.displayName());

        int index = 0;
        for (var e: entries.entrySet()) {
            writer.formattedLine("{0}) ", NamedTextColor.YELLOW, ++index);

            User user = Users.get(e.getKey());

            boolean privateProfile = user.get(Properties.PROFILE_PRIVATE);

            if (privateProfile) {
                continue;
            }

            boolean direct = e.getValue().isDirectResident();
            long firstMoveIn = e.getValue().firstMoveIn();

            user.unloadIfOffline();

            String entryFormat;

            if (direct) {
                entryFormat = "&6{0, user}&r has lived here for &e{1, time, -timestamp}&r.";
            } else {
                entryFormat = "&6{0, user} has had a home here for &e{1, time -timestamp}&r.";
            }

            writer.formattedLine(entryFormat,
                    NamedTextColor.GRAY,
                    user,
                    firstMoveIn == UNKNOWN_MOVEIN ? "an unknown time" : firstMoveIn
            );
        }

        writer.line(Messages.PAGE_BORDER);
    }

    public static class Resident {
        @Setter @Getter
        private long directMoveIn;

        private Object2LongMap<String> homes;

        public void addHome(String home, long timeStamp) {
            Object2LongMap<String> homes = homesSafe();
            homes.put(home, timeStamp);
        }

        public void removeHome(String home) {
            if(homes == null) return;
            homes.removeLong(home);
        }

        public boolean isDirectResident() {
            return directMoveIn != 0L;
        }

        public long firstMoveIn() {
            if (directMoveIn > 0) {
                return directMoveIn;
            }

            if (!hasHomes()) {
                return UNKNOWN_MOVEIN;
            }

            return homes.values()
                    .longStream()
                    .min()
                    .orElse(UNKNOWN_MOVEIN);
        }

        public boolean hasHomes() {
            return homes != null && !homes.isEmpty();
        }

        public boolean isEmpty() {
            return !hasHomes() && !isDirectResident();
        }

        private Object2LongMap<String> homesSafe() {
            return homes == null ? (homes = new Object2LongOpenHashMap<>()) : homes;
        }
    }
}