package net.forthecrown.voting;

import com.bencodez.votingplugin.advancedcore.listeners.PlayerRewardEvent;
import com.bencodez.votingplugin.events.PlayerPostVoteEvent;
import com.bencodez.votingplugin.events.PlayerSpecialRewardEvent;
import java.util.UUID;
import net.forthecrown.leaderboards.Leaderboards;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPostVote(PlayerPostVoteEvent event) {
    UserService service = Users.getService();
    service.getVotes().add(event.getUser().getJavaUUID(), 1);

    Tasks.runLater(() -> {
      Leaderboards.updateWithSource("votes/daily");
      Leaderboards.updateWithSource("votes/weekly");
      Leaderboards.updateWithSource("votes/monthly");
      Leaderboards.updateWithSource("votes/alltime");
    }, 5);
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
    return Users.getService().isAltAccount(uuid);
  }
}