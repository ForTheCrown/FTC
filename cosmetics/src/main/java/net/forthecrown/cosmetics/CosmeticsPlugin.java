package net.forthecrown.cosmetics;

import net.forthecrown.cosmetics.commands.CommandCosmetics;
import net.forthecrown.cosmetics.listeners.ArrowListener;
import net.forthecrown.cosmetics.listeners.DeathListener;
import net.forthecrown.cosmetics.listeners.DemotionListener;
import net.forthecrown.cosmetics.listeners.PlayerJoinLeaveListener;
import net.forthecrown.events.Events;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import org.bukkit.plugin.java.JavaPlugin;

public class CosmeticsPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    saveDefaultConfig();
    reloadConfig();

    UserService service = Users.getService();
    service.registerComponent(CosmeticData.class);

    Cosmetics.init();
    registerListeners();

    new CommandCosmetics();
  }

  void registerListeners() {
    Events.register(new ArrowListener());
    Events.register(new DeathListener());
    Events.register(new DemotionListener());
    Events.register(new PlayerJoinLeaveListener());
  }
}