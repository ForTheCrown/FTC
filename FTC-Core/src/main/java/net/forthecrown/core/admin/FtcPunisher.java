package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FtcPunisher extends AbstractJsonSerializer implements Punisher {
    private final Map<UUID, EntryImpl> entryMap = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, JailCell> jailed = new Object2ObjectOpenHashMap<>();

    public FtcPunisher() {
        super("punishments");

        reload();
        Punishments.punisher = this;

        Crown.logger().info("Punishments initialized");
    }

    @NotNull
    @Override
    public PunishEntry getEntry(UUID uuid) {
        return entryMap.computeIfAbsent(uuid, EntryImpl::new);
    }

    @NotNull
    @Override
    public JailCell getCell(UUID prisoner) {
        return jailed.get(prisoner);
    }

    @Override
    public void setJailed(UUID prisoner, JailCell cell) {
        jailed.put(prisoner, cell);
    }

    @Override
    public void removeJailed(UUID prisoner) {
        jailed.remove(prisoner);
    }

    @Override
    protected void save(JsonWrapper json) {
        JsonWrapper jails = JsonWrapper.empty();
        JsonWrapper entries = JsonWrapper.empty();

        for (PunishEntry e: entryMap.values()) {
            JsonElement element = e.serialize();
            if(element == null) continue;

            entries.add(e.entryHolder().toString(), element);
        }

        for (JailCell c: Registries.JAILS) {
            jails.add(c.key().asString(), c);
        }

        json.add("entries", entries);
        json.add("jails", jails);
    }

    @Override
    protected void reload(JsonWrapper json) {
        // Clear entries
        for (EntryImpl e: entryMap.values()) e.clearCurrent();
        entryMap.clear();

        // Clear jails
        Registries.JAILS.clear();

        for (Map.Entry<String, JsonElement> e: json.getObject("entries").entrySet()) {
            UUID id = UUID.fromString(e.getKey());
            EntryImpl entry = new EntryImpl(id);
            entry.deserialize(e.getValue());

            entryMap.put(id, entry);
        }

        for (Map.Entry<String, JsonElement> e: json.getObject("jails").entrySet()) {
            Key k = Keys.parse(e.getKey());
            JailCell cell = new JailCell(k);
            cell.deserialize(e.getValue());

            Registries.JAILS.register(k, cell);
        }
    }

    public static class EntryImpl implements PunishEntry {
        private final UUID holder;

        private final Map<PunishType, Punishment> current = new Object2ObjectOpenHashMap<>();

        private final List<Punishment> past = new ObjectArrayList<>();
        private final List<EntryNote> notes = new ObjectArrayList<>();

        public EntryImpl(UUID holder) {
            this.holder = holder;
        }

        @Override
        public Collection<Punishment> past() {
            return past;
        }

        @Override
        public Collection<Punishment> current() {
            return current.values();
        }

        @Override
        public List<EntryNote> notes() {
            return notes;
        }

        @Override
        public UUID entryHolder() {
            return holder;
        }

        @Override
        public Punishment getCurrent(PunishType type) {
            return current.get(type);
        }

        @Override
        public boolean isPunished(PunishType type) {
            return current.get(type) != null;
        }

        @Override
        public void punish(Punishment punishment) {
            punishment.startTask(() -> revokePunishment(punishment.type()));
            current.put(punishment.type(), punishment);
        }

        @Override
        public void revokePunishment(PunishType type) {
            Punishment punishment = current.remove(type);
            if(punishment == null) return;

            punishment.cancelTask();
            past.add(punishment);

            type.onPunishmentEnd(entryUser());
        }

        void clearCurrent() {
            if(!current.isEmpty()) {
                for (Punishment p: current.values()) {
                    p.cancelTask();
                }

                current.clear();
            }
        }

        @Override
        public JsonElement serialize() {
            JsonWrapper json = JsonWrapper.empty();

            if(!notes.isEmpty()) json.addList("notes", notes);

            if(!current.isEmpty()) json.addList("current", current());
            if(!past.isEmpty()) json.addList("past", past());

            return json.nullIfEmpty();
        }

        @Override
        public void deserialize(JsonElement element) {
            JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

            notes.clear();
            past.clear();
            clearCurrent();

            notes.addAll(json.getList("notes", EntryNote::read, new ArrayList<>()));

            Collection<Punishment> list = json.getList("current", Punishment::read, new ArrayList<>());
            for (Punishment p: list) punish(p);

            past.addAll(json.getList("past", Punishment::read, new ArrayList<>()));
        }
    }
}