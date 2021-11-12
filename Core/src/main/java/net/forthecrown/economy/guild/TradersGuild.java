package net.forthecrown.economy.guild;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.economy.guild.topics.VoteTopic;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import org.bukkit.World;

import java.util.UUID;
import java.util.function.Consumer;

public interface TradersGuild extends CrownSerializer {
    VoteState getVoteState();

    default boolean isCurrentlyVoting() {
        return getVoteState() != null;
    }

    long getLastVoteEnd();
    boolean canStartVote();

    World getWorld();

    void createVote(VoteTopic topic);
    void finishVoting();

    ObjectList<UUID> getMembers();

    default boolean isMember(UUID id) {
        return getMembers().contains(id);
    }

    void addMember(UUID member);
    void removeMember(UUID member);

    int getMaxMembers();

    VoteBox getVoteBox();

    default void forEachUser(Consumer<CrownUser> consumer) {
        for (UUID id: getMembers()) {
            CrownUser u = UserManager.getUser(id);

            consumer.accept(u);

            u.unloadIfOffline();
        }
    }

    default void forEach(Consumer<UUID> uuidConsumer) {
        getMembers().forEach(uuidConsumer);
    }
}
