package net.forthecrown.economy;

import com.sk89q.worldguard.WorldGuard;
import lombok.Getter;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.commands.MarketCommands;
import net.forthecrown.economy.market.listeners.MarketListener;
import net.forthecrown.economy.signshops.ShopManager;
import net.forthecrown.economy.signshops.SignShopFlags;
import net.forthecrown.economy.signshops.commands.SignShopCommands;
import net.forthecrown.economy.signshops.listeners.ShopListeners;
import net.forthecrown.events.Events;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ShopsPlugin extends JavaPlugin {

  private ShopConfig shopConfig;

  private MarketManager markets;
  private ShopManager shops;

  public static ShopsPlugin getPlugin() {
    return JavaPlugin.getPlugin(ShopsPlugin.class);
  }

  @Override
  public void onEnable() {
    markets = new MarketManager(this);
    shops = new ShopManager(this);
    reload();

    SignShopCommands.createCommands(shops);
    ShopListeners.registerAll(shops);

    MarketCommands.createCommands(markets);
    Events.register(new MarketListener(markets));
  }

  @Override
  public void onLoad() {
    SignShopFlags.register(WorldGuard.getInstance().getFlagRegistry());
  }

  @Override
  public void onDisable() {
    markets.save();
    shops.save();
  }

  public void reload() {
    reloadConfig();

    markets.load();
    shops.reload();
  }

  @Override
  public void reloadConfig() {
    shopConfig = TomlConfigs.loadPluginConfig(this, ShopConfig.class);
  }
}
