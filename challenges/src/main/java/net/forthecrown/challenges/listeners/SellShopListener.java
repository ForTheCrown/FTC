package net.forthecrown.challenges.listeners;

import net.forthecrown.challenges.ChallengeManager;
import net.forthecrown.challenges.Challenges;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.sellshop.event.SellShopCreateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class SellShopListener implements Listener {

  private final ChallengeManager manager;

  public SellShopListener(ChallengeManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onSellShopCreate(SellShopCreateEvent event) {
    var builder = event.getBuilder();

    builder.add(
        Slot.of(4, 2),
        Menus.createOpenNode(
            manager::getItemChallengeMenu,
            Challenges.createMenuHeader()
        )
    );
  }
}
