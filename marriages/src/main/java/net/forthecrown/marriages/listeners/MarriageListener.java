package net.forthecrown.marriages.listeners;

import net.forthecrown.cosmetics.emotes.Emotes;
import net.forthecrown.marriages.Marriages;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MarriageListener implements Listener {

  public static final String PRIEST_TAG = "marriage_priest";

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof Player)) {
      return;
    }

    if (Cooldown.containsOrAdd(event.getPlayer(), "Marriage_Smooch", 2)) {
      return;
    }

    //This is dumb, I love it
    //Right-Click spouse to smooch them

    User user = Users.get(event.getPlayer());
    User target = Users.get(event.getRightClicked().getUniqueId());

    if (!user.getPlayer().isSneaking()) {
      return;
    }

    if (!Marriages.areMarried(user, target)) {
      return;
    }

    Emotes.SMOOCH.getValue().execute(user, target);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPriestInteract(PlayerInteractEntityEvent event) {
    if (!event.getRightClicked().getScoreboardTags().contains(PRIEST_TAG)) {
      return;
    }

    event.setCancelled(true);

    var user = Users.get(event.getPlayer());
    var prompt = MarriageConfirmation.initialPrompt(user);

    user.sendMessage(prompt);
  }
}