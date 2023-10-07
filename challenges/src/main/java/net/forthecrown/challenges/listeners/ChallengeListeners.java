package net.forthecrown.challenges.listeners;

import net.forthecrown.challenges.ChallengesPlugin;
import net.forthecrown.events.Events;

public final class ChallengeListeners {
  private ChallengeListeners() {}

  public static void registerAll(ChallengesPlugin plugin) {
    var manager = plugin.getChallenges();

    Events.register(new DayChangeListener(manager));
    Events.register(new SellShopListener(manager));
    Events.register(new ServerLoadListener(plugin));
  }
}
