package net.forthecrown.king;

import net.forthecrown.events.CoinCreationEvent;
import net.forthecrown.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CoinListener implements Listener {

  private final Kingship kingship;

  public CoinListener(Kingship kingship) {
    this.kingship = kingship;
  }

  @EventHandler(ignoreCancelled = true)
  public void onCoinCreation(CoinCreationEvent event) {
    if (!kingship.hasMonarch()) {
      return;
    }

    String title = kingship.getTitle();
    User monarch = kingship.getMonarch();
    String playerName = monarch.getName();

    event.getBuilder().addLore("&8During the reign of " + title + " " + playerName);
  }
}
