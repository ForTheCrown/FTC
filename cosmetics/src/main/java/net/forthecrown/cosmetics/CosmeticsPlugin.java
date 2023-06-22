package net.forthecrown.cosmetics;

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
  }
}