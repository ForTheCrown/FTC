package net.forthecrown.vault;

import net.forthecrown.utils.PluginUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    if (!PluginUtil.isEnabled("Vault")) {
      getSLF4JLogger().error("Vault plugin not found, disabling");
      getServer().getPluginManager().disablePlugin(this);

      return;
    }

    FtcEconomy economy = new FtcEconomy();
    economy.registerService(this);

    getSLF4JLogger().debug("Registered FtcEconomy service");
  }

  @Override
  public void onDisable() {

  }
}
