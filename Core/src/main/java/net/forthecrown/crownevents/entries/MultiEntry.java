package net.forthecrown.crownevents.entries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class MultiEntry implements EventEntry, Iterable<Player> {

    protected final List<Player> players = new ArrayList<>();

    public MultiEntry(Collection<Player> initialPlayers){
        players.addAll(initialPlayers);
    }

    public MultiEntry() {
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
    public UnmodifiableIterator<Player> iterator() {
        return getPlayers().iterator();
    }

    public ImmutableList<Player> getPlayers(){
        return ImmutableList.copyOf(players);
    }
}
