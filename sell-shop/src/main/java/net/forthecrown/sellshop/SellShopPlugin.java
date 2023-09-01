package net.forthecrown.sellshop;

import lombok.Getter;
import net.forthecrown.sellshop.commands.SellShopCommands;
import net.forthecrown.sellshop.listeners.SellShopListeners;
import net.forthecrown.user.Users;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.utils.io.PathUtil;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class SellShopPlugin extends JavaPlugin {

  private SellShopConfig shopConfig;

  private SellShop sellShop;

  public static SellShopPlugin getPlugin() {
    return getPlugin(SellShopPlugin.class);
  }

  @Override
  public void onEnable() {
    SellProperties.registerAll();

    sellShop = new SellShop(PathUtil.pluginPath());
    reloadConfig();

    SellShopCommands.createCommands(sellShop);
    SellShopListeners.registerAll();

    Users.getService().registerComponent(UserShopData.class);
  }

  @Override
  public void onDisable() {

  }

  @Override
  public void reloadConfig() {
    shopConfig = TomlConfigs.loadPluginConfig(this, SellShopConfig.class);
    sellShop.load();
  }
}
