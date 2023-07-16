package net.forthecrown.core;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import net.forthecrown.datafix.DataUpdater;
import net.forthecrown.datafix.DataUpdaters;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;

class CoreDataFix {

  static void execute() {
    DataUpdaters updaters = DataUpdaters.create();
    updaters.addUpdater(new DirectoryMover());
    updaters.addUpdater(new UserUpdate());
    updaters.execute();
  }
}

class UserUpdate extends DataUpdater {

  @Override
  protected boolean update() {
    Path userdata = PathUtil.pluginPath("userdata");

    return iterateDirectory(userdata, true, true, path -> {
      JsonObject object = JsonUtils.readFileObject(path);
      JsonWrapper json = JsonWrapper.wrap(object);
      transformData(json);
      JsonUtils.writeFile(object, path);
    });
  }

  private void transformData(JsonWrapper json) {
    JsonWrapper properties = json.getWrappedNonNull("properties");
    JsonWrapper blockList = json.getWrappedNonNull("blockList");

    JsonWrapper inter = json.getWrapped("interactions");
    if (inter != null) {
      if (inter.has("spouse")) {
        properties.add("spouse", inter.get("spouse"));
      }

      if (inter.has("blocked")) {
        blockList.add("blocked", inter.get("blocked"));
      }

      if (inter.has("separated")) {
        blockList.add("separated", inter.get("separated"));
      }
    }

    JsonWrapper homes = json.getWrapped("homes");
    if (homes != null && homes.has("user:home:region")) {
      properties.add("homeWaypoint", json.get("user:home:region"));
      homes.remove("user:home:region");
    }

    json.remove("interactions");

    if (!properties.isEmpty()) {
      json.add("properties", properties);
    }

    if (!blockList.isEmpty()) {
      json.add("blockList", blockList);
    }
  }
}

class DirectoryMover extends DataUpdater {

  final Path pluginDir = Path.of("plugins");
  final Path oldPluginDir = pluginDir.resolve("ForTheCrown");

  @Override
  protected boolean update() {
    logger.info("pluginsDir={}", pluginDir.toAbsolutePath());

    // New plugin directories
    Path core         = pluginDir.resolve("FTC-Core");
    Path serverlist   = pluginDir.resolve("FTC-ServerList");
    Path waypoints    = pluginDir.resolve("FTC-Waypoints");
    Path specialItems = pluginDir.resolve("FTC-SpecialItems");
    Path antiGrief    = pluginDir.resolve("FTC-AntiGrief");
    Path end          = pluginDir.resolve("FTC-EndOpener");
    Path rw           = pluginDir.resolve("FTC-ResourceWorld");
    Path titles       = pluginDir.resolve("FTC-UserTitles");
    Path structs      = pluginDir.resolve("FTC-Structures");
    Path scripts      = pluginDir.resolve("FTC-ScriptEngine");
    Path guilds       = pluginDir.resolve("FTC-Guilds");
    Path dungeons     = pluginDir.resolve("FTC-Dungeons");
    Path dialogues    = pluginDir.resolve("FTC-Dialogues");
    Path challenges   = pluginDir.resolve("FTC-Challenges");

    copyFromOld("serverlist.toml",        serverlist.resolve("serverlist.toml"));
    copyFromOld("icons",                  serverlist.resolve("icons"));

    copyFromOld("punishments.json",       antiGrief.resolve("punishments.json"));
    copyFromOld("banned_words.json",      antiGrief.resolve("banned_words.json"));

    copyFromOld("waypoints.dat",          waypoints.resolve("waypoints.dat"));
    copyFromOld("end_opener.json",        end.resolve("config.json"));
    copyFromOld("rw_data",                rw.resolve("sections"));
    copyFromOld("user/ranks.toml",        titles.resolve("ranks.toml"));
    copyFromOld("weapon_abilities",       specialItems.resolve("sword-abilities"));
    copyFromOld("structures",             structs);
    copyFromOld("scripts",                scripts);
    copyFromOld("guilds",                 guilds);
    copyFromOld("dungeons",               dungeons);
    copyFromOld("dialogues",              dialogues);
    copyFromOld("challenges",             challenges);

    copyFromOld("cooldowns.json",         core.resolve("cooldowns.json"));
    copyFromOld("announcer.json",         core.resolve("announcer.json"));
    copyFromOld("stored_inventories.dat", core.resolve("stored_inventories.dat"));
    copyFromOld("user/alts.json",         core.resolve("alts.json"));
    copyFromOld("user/balances.json",     core.resolve("balances.json"));
    copyFromOld("user/gems.json",         core.resolve("gems.json"));
    copyFromOld("user/playtime.json",     core.resolve("playtime.json"));
    copyFromOld("user/profiles.json",     core.resolve("profiles.json"));
    copyFromOld("user/votes.json",        core.resolve("votes.json"));
    copyFromOld("user/data",              core.resolve("userdata"));

    return true;
  }

  private void copyFromOld(String oldName, Path dest) {
    copySafe(oldPluginDir.resolve(oldName), dest);
  }
}
