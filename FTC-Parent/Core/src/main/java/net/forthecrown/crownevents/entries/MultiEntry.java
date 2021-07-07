package net.forthecrown.crownevents.entries;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class MultiEntry implements EventEntry, Iterable<Player> {

    protected final List<Player> players = new ArrayList<>();

    public MultiEntry(Collection<Player> initialPlayers){
        players.addAll(initialPlayers);
    }

    public int size() {
        return players.size();
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public boolean contains(Player o) {
        return players.contains(o);
    }

    public boolean add(Player player) {
        return players.add(player);
    }

    public boolean remove(Player o) {
        return players.remove(o);
    }

    public void clear() {
        players.clear();
    }

    @NotNull
    @Override
    public ListIterator<Player> iterator() {
        return players.listIterator();
    }
}
