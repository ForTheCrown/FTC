package net.forthecrown.serverlist;

import lombok.Getter;
import net.forthecrown.events.Events;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ServerlistPlugin extends JavaPlugin {

  private ServerListDisplay display;

  @Override
  public void onEnable() {
    display = new ServerListDisplay();
    reload();

    new CommandServerList();
    Events.register(new ServerlistListener());
  }

  public void reload() {
    reloadConfig();
    display.load();
  }

  @Override
  public void reloadConfig() {
    saveResource("config.toml", false);
    SerializationHelper.readAsJson(
        PathUtil.pluginPath("config.toml"),
        json -> {
          boolean allowRandom = json.getBool("allowMaxPlayerRandomization", true);
          display.setAllowMaxPlayerRandomization(allowRandom);

          Component baseMotd = json.getComponent("baseMotd");
          display.setBaseMotd(baseMotd);
        }
    );
  }
}
