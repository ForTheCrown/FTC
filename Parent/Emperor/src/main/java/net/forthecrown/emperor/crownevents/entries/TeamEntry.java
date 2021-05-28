package net.forthecrown.emperor.crownevents.entries;

import net.forthecrown.emperor.crownevents.InEventListener;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;

public class TeamEntry extends EventEntry<TeamEntry> implements Iterable<Player> {

    protected Collection<Player> members;

    public TeamEntry(Collection<Player> initialMembers, InEventListener<TeamEntry> inEventListener){
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
}
