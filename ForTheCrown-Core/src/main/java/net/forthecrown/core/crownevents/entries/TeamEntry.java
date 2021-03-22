package net.forthecrown.core.crownevents.entries;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

public class TeamEntry extends EventEntry implements Iterable<Player> {

    protected Collection<Player> members;

    public TeamEntry(Collection<Player> initialMembers, Listener inEventListener){
        super(inEventListener);
        this.members = initialMembers;
    }

    public void remove(Player participant){
        members.remove(participant);
    }

    public void add(Player participant){
        members.add(participant);
    }

    public void clear(){
        members.clear();
    }

    public Collection<Player> members() {
        return members;
    }

    public void setMembers(Collection<Player> players){
        this.members = players;
    }

    @Nonnull
    @Override
    public Iterator<Player> iterator() {
        return members.iterator();
    }

    @Override
    public void forEach(Consumer<? super Player> action) {
        members.forEach(action);
    }
}
