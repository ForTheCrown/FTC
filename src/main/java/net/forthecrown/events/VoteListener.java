package net.forthecrown.events;

import com.bencodez.votingplugin.events.PlayerPostVoteEvent;
import net.forthecrown.user.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerPostVote(PlayerPostVoteEvent event) {
        UserManager.get()
                .getVotes()
                .add(event.getUser().getJavaUUID(), 1);
    }
}