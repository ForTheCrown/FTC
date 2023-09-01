package net.forthecrown.voting;

import net.forthecrown.events.Events;
import org.bukkit.plugin.java.JavaPlugin;

public class VotingPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Events.register(new VoteListener());
  }

  @Override
  public void onDisable() {

  }
}
