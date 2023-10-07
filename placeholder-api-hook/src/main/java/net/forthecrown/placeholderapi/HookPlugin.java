package net.forthecrown.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import net.forthecrown.text.placeholder.PlaceholderSource;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.utils.PluginUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class HookPlugin extends JavaPlugin {

  PlaceholderSource source;

  @Override
  public void onEnable() {
    if (!PluginUtil.isEnabled("PlaceholderAPI")) {
      getSLF4JLogger().error("No PlaceholderAPI found, disabling self");
      getServer().getPluginManager().disablePlugin(this);

      return;
    }

    LocalExpansionManager manager = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
    FtcPlaceholderSource source = new FtcPlaceholderSource(manager);

    this.source = source;
    Placeholders.getService().addDefaultSource(source);
  }

  @Override
  public void onDisable() {
    if (source == null) {
      return;
    }

    Placeholders.getService().removeDefaultSource(source);
  }
}
