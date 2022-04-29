package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class UserCacheImpl implements UserCache {
    public static final int EXPECTED_SIZE = 1100;

    private final Map<UUID,   CacheEntryImpl> identified = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);
    private final Map<String, CacheEntryImpl> named      = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);
    private final Map<String, CacheEntryImpl> oldNamed   = new Object2ObjectOpenHashMap<>(30);
    private final Map<String, CacheEntryImpl> nicknamed  = new Object2ObjectOpenHashMap<>(20);

    @Override
    public synchronized CacheEntryImpl getNamed(String name) {
        return named.get(name.toLowerCase());
    }

    @Override
    public synchronized CacheEntryImpl getNicked(String nick) {
        return nicknamed.get(nick.toLowerCase());
    }

    @Override
    public synchronized CacheEntry getByLastName(String oldName) {
        return oldNamed.get(oldName.toLowerCase());
    }

    @Override
    public synchronized CacheEntryImpl getEntry(UUID uuid) {
        return identified.get(uuid);
    }

    @Override
    public synchronized CacheEntryImpl createEntry(UUID uuid, String name) {
        CacheEntryImpl entry = new CacheEntryImpl(uuid);
        entry.name = name;
        addEntry(entry);

        return entry;
    }

    public synchronized void addEntry(CacheEntryImpl entry) {
        identified.put(entry.getUniqueId(), entry);
        named.put(entry.getName().toLowerCase(), entry);

        if(entry.getNickname() != null) {
            nicknamed.put(entry.getNickname().toLowerCase(), entry);
        }

        if(entry.getLastName() != null) {
            oldNamed.put(entry.getLastName().toLowerCase(), entry);
        }
    }

    @Override
    public synchronized void onNameChange(CacheEntry entry1, String newName) {
        CacheEntryImpl entry = (CacheEntryImpl) entry1;

        if(entry.lastName != null) oldNamed.remove(entry.lastName);
        named.remove(entry.name.toLowerCase());

        entry.lastNameChange = System.currentTimeMillis();
        entry.lastName = entry.name;
        entry.name = newName;

        named.put(newName.toLowerCase(), entry);
        oldNamed.put(entry.lastName.toLowerCase(), entry);
    }

    @Override
    public synchronized void onNickChange(CacheEntry entry1, String newNick) {
        CacheEntryImpl entry = (CacheEntryImpl) entry1;

        if(entry.nickname != null) {
            nicknamed.remove(entry.nickname.toLowerCase());
        }

        entry.nickname = newNick;

        if(newNick != null) {
            nicknamed.put(newNick.toLowerCase(), entry);
        }
    }

    void clear() {
        named.clear();
        nicknamed.clear();
        identified.clear();
    }

    void addAll(Iterable<CacheEntryImpl> entries) {
        entries.forEach(this::addEntry);
    }

    @Override
    public int size() {
        return identified.size();
    }

    public Map<String, CacheEntryImpl> getNamed() {
        return named;
    }

    public Map<String, CacheEntryImpl> getNicknamed() {
        return nicknamed;
    }

    public Map<UUID, CacheEntryImpl> getIdentified() {
        return identified;
    }

    @Override
    public Stream<CacheEntry> readerStream() {
        return (Stream) identified.values().stream();
    }

    @Override
    public void remove(CacheEntry cache) {
        identified.remove(cache.getUniqueId());
        named.remove(cache.getName());

        if(cache.getNickname() != null) {
            nicknamed.remove(cache.getNickname());
        }

        if(cache.getLastName() != null) {
            oldNamed.remove(cache.getLastName());
        }
    }

    @Data
    public static class CacheEntryImpl implements CacheEntry {
        private final UUID uniqueId;
        String name;
        String nickname;
        String lastName;
        long lastNameChange = NO_NAME_CHANGE;

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "id: " + uniqueId +
                    ", name: " + name +
                    (getNickname() == null ? "" : ", nick: " + nickname) +
                    (getLastName() == null ? "" : ", lastname: " + lastName) +
                    "}";
        }
    }
}