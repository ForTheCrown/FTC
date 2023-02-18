package net.forthecrown.events;

import com.bencodez.votingplugin.advancedcore.listeners.PlayerRewardEvent;
import com.bencodez.votingplugin.events.PlayerPostVoteEvent;
import com.bencodez.votingplugin.events.PlayerSpecialRewardEvent;
import java.util.UUID;
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

  @EventHandler(ignoreCancelled = true)
  public void onPlayerReward(PlayerRewardEvent event) {
    if (shouldCancelReward(event.getPlayer().getJavaUUID())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerSpecialReward(PlayerSpecialRewardEvent event) {
    if (shouldCancelReward(event.getUser().getJavaUUID())) {
      event.setCancelled(true);
    }
  }

  private boolean shouldCancelReward(UUID uuid) {
    var alts = UserManager.get().getAlts();
    return alts.isAlt(uuid);
  }
}