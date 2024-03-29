package net.forthecrown.core;

import java.nio.file.Path;
import java.util.Objects;
import net.forthecrown.FtcServer;
import net.forthecrown.Worlds;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class FtcServerImpl implements FtcServer {

  private Location serverSpawn;

  private final SettingsBook<User> globalSettings = new SettingsBook<>();
  private final CorePlugin plugin;

  public FtcServerImpl(CorePlugin plugin) {
    this.plugin = plugin;
  }

  private Path spawnJson() {
    return PathUtil.pluginPath("spawn.json");
  }

  public void load() {
    SerializationHelper.readAsJson(spawnJson(), json -> {
      Location loc = JsonUtils.readLocation(json.getSource());
      setServerSpawn(loc);
    });
  }

  public void save() {
    SerializationHelper.writeJsonFile(spawnJson(), jsonWrapper -> {
      var json = JsonUtils.writeLocation(getServerSpawn());
      jsonWrapper.addAll(json);
    });
  }

  @Override
  public @NotNull Location getServerSpawn() {
    if (serverSpawn == null) {
      World overworld = Worlds.overworld();
      return overworld.getSpawnLocation();
    }

    return serverSpawn.clone();
  }

  @Override
  public void setServerSpawn(@NotNull Location serverSpawn) {
    Objects.requireNonNull(serverSpawn);
    this.serverSpawn = serverSpawn.clone();
  }

  @Override
  public @NotNull SettingsBook<User> getGlobalSettingsBook() {
    return globalSettings;
  }

  @Override
  public void announce(ViewerAwareMessage message) {
    ChannelledMessage.create(message)
        .setBroadcast()
        .setRenderer(plugin.getAnnouncer().renderer(Placeholders.newRenderer().useDefaults()))
        .send();
  }

  @Override
  public void registerLeaveListener(String id, LeaveCommandListener listener) {
    Objects.requireNonNull(id, "Null ID");
    Objects.requireNonNull(listener, "Null listener");
    CommandLeave.listeners.put(id, listener);
  }

  @Override
  public void unregisterLeaveListener(String id) {
    Objects.requireNonNull(id, "Null ID");
    CommandLeave.listeners.remove(id);
  }
}