package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import net.forthecrown.datafix.DataUpdater;
import net.forthecrown.datafix.DataUpdaters;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;

class CoreDataFix {

  static void execute() {
    DataUpdaters updaters = DataUpdaters.create();
    updaters.addUpdater(new DirectoryMover());
    updaters.addUpdater(new UserDataMover());
    updaters.addUpdater(new UserUpdate());
    updaters.addUpdater(new MailUpdate());
    updaters.addUpdater(new ScriptRewriter());
    updaters.execute();
  }
}

class ScriptRewriter extends DataUpdater {

  @Override
  protected boolean update() throws Throwable {
    final Path scriptsPath = Path.of("plugins", "FTC-Scripting", "scripts");
    logger.info("Scripts dir={}, exists={}", scriptsPath, Files.exists(scriptsPath));

    return iterateDirectory(scriptsPath, true, true, path -> {
      if (!path.getFileName().toString().endsWith(".js")) {
        return;
      }

      logger.info("Rewriting script file '{}'", path);

      String str = Files.readString(path, StandardCharsets.UTF_8);
      String rewritten = rewrite(str);

      Files.writeString(path, rewritten, StandardCharsets.UTF_8);

      logger.info("Finished file");
    });
  }

  private String rewrite(String string) {
    return string
        // Package import rewrites
        .replace("UserTeleport.Type", "UserTeleport$Type")
        .replace("user.data.UserRanks", "titles.UserRanks")
        .replace("core.Permissions", "Permissions")

        // Util class rewrites, Util no longer exists
        .replace("Util.RANDOM", "(new Packages.java.util.Random())")
        .replace("Util.giveOrDropItem", "ItemStacks.giveOrDropItem")

        .replace("useables.Usables", "usables.UsablesPlugin")
        .replace("Usables.getInstance()", "UsablesPlugin.get()");
  }
}

class MailUpdate extends DataUpdater {

  JsonArray dataDestination;

  @Override
  protected boolean update() throws Throwable {
    Path destination = Path.of("plugins/FTC-Mail/mail.json");
    dataDestination = new JsonArray();

    Path userdata = PathUtil.pluginPath("userdata");

    boolean iterationResult = iterateDirectory(userdata, true, true, path -> {
      if (!PathUtil.isFilenameUUID(path)) {
        logger.warn("Found non userdata file inside userdata directory: {}", path);
        return;
      }

      logger.info("Updating {} mail", path);

      UUID id = PathUtil.getFilenameUUID(path).getOrThrow(true, string -> {});
      JsonObject object = JsonUtils.readFileObject(path);
      JsonWrapper json = JsonWrapper.wrap(object);

      if (!updateUserMail(id, json)) {
        logger.info("Found no mail to fix inside {} data", id);
        return;
      }

      JsonUtils.writeFile(object, path);
      logger.info("Updated {} mail", path);
    });

    if (!iterationResult) {
      return false;
    }

    logger.info("Writing {}", destination);
    PathUtil.ensureParentExists(destination);
    JsonUtils.writeFile(dataDestination, destination);

    return true;
  }

  private boolean updateUserMail(UUID targetId, JsonWrapper userdata) {
    JsonElement mailElement = userdata.remove("mail");

    if (mailElement == null || !mailElement.isJsonArray()) {
      return false;
    }

    JsonArray messages = mailElement.getAsJsonArray();
    for (JsonElement message : messages) {
      var json = updateSingle(targetId, message);
      dataDestination.add(json.getSource());
    }

    return true;
  }

  private JsonWrapper updateSingle(UUID targetId, JsonElement element) {
    JsonWrapper mail = JsonWrapper.wrap(element.getAsJsonObject());
    JsonWrapper json = JsonWrapper.create();

    json.addUUID("target", targetId);
    json.add("message", mail.get("msg"));
    json.add("sent_date", mail.get("sent"));

    if (mail.has("sender")) {
      json.add("sender", mail.get("sender"));
    }

    if (mail.getBool("read", false)) {
      json.addInstant("read_date", Instant.now());
    }

    if (mail.has("attachment")) {
      var attach = mail.get("attachment");

      if (mail.getBool("attachment_claimed", false)) {
        mail.addInstant("claim_date", Instant.now());
      }

      var updated = updateAttachment(JsonWrapper.wrap(attach.getAsJsonObject()));

      if (updated.has("claimed")) {
        updated.remove("claimed");
        mail.addInstant("claim_date", Instant.now());
      }

      json.add("attachment", updated);
    }

    return json;
  }

  private JsonWrapper updateAttachment(JsonWrapper old) {
    JsonWrapper json = JsonWrapper.create();

    if (old.has("item")) {
      JsonArray arr = new JsonArray();
      arr.add(old.get("item"));
      json.add("items", arr);
    }

    if (old.has("rhines")) {
      json.add("rhines", old.get("rhines"));
    }

    if (old.has("gems")) {
      json.add("gems", old.get("gems"));
    }

    if (old.has("tag")) {
      JsonArray arr = new JsonArray();
      arr.add(old.get("tag"));
      json.add("tags", arr);
    }

    if (old.has("script")) {
      json.add("claim_script", old.get("script"));
    }

    if (old.getBool("claimed", false)) {
      json.add("claimed", true);
    }

    return json;
  }
}

class UserUpdate extends DataUpdater {

  @Override
  protected boolean update() throws IOException {
    Path userdata = PathUtil.pluginPath("userdata");

    Map<UUID, JsonWrapper> profileData = new Object2ObjectOpenHashMap<>();
    loadProfiles(profileData);

    boolean iterResult = iterateDirectory(userdata, true, true, path -> {
      logger.info("Transforming userdata: {}", path);

      JsonObject object = JsonUtils.readFileObject(path);
      var idResult = PathUtil.getFilenameUUID(path);

      if (idResult.result().isEmpty()) {
        logger.error("Found non-user file in userdata directory: {}", path);
        return;
      }

      JsonWrapper json = JsonWrapper.wrap(object);
      transformData(json, idResult.result().get(), profileData);
      JsonUtils.writeFile(object, path);

      logger.info("Completed transformation of {}'s data", path);
    });

    saveProfiles(profileData);

    return iterResult;
  }

  void saveProfiles(Map<UUID, JsonWrapper> map) throws IOException {
    JsonArray array = new JsonArray();
    for (JsonWrapper value : map.values()) {
      array.add(value.getSource());
    }

    JsonUtils.writeFile(array, PathUtil.pluginPath("profiles.json"));
  }

  void loadProfiles(Map<UUID, JsonWrapper> map) throws IOException {
    JsonArray element = JsonUtils.readFile(PathUtil.pluginPath("profiles.json")).getAsJsonArray();

    int i = 0;

    for (JsonElement jsonElement : element) {
      if (!jsonElement.isJsonObject()) {
        logger.error("Found non-object profile entry at index {}", i);
        continue;
      }

      i++;

      JsonWrapper json = JsonWrapper.wrap(jsonElement.getAsJsonObject());
      UUID id = json.getUUID("uuid");

      map.put(id, json);
    }
  }

  private void transformData(JsonWrapper json, UUID id, Map<UUID, JsonWrapper> profileData) {
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

    JsonElement ipElement = json.remove("ip");
    if (ipElement != null && !ipElement.isJsonNull()) {
      var data = profileData.get(id);

      if (data != null) {
        data.add("ip", ipElement);
        data.add("lastIpUpdate", json.getObject("timeStamps").get("lastJoin").deepCopy());
      }
    }

    JsonWrapper homes = json.getWrapped("homes");
    if (homes != null && homes.has("user:home:region")) {
      properties.add("homeWaypoint", json.remove("user:home:region"));
    }

    JsonElement guild = json.remove("guild");
    if (guild != null) {
      properties.add("guild", guild);
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

class UserDataMover extends DataUpdater {

  final Path pluginDir = Path.of("plugins");
  final Path oldPluginDir = pluginDir.resolve("ForTheCrown");

  @Override
  protected boolean update() throws Throwable {
    Path core = pluginDir.resolve("FTC-Core");

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
    if (!copySafe(oldPluginDir.resolve(oldName), dest)) {
      return;
    }

    logger.info("Copied old FTC file '{}' to '{}'", oldName, dest);
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
    Path specialItems = pluginDir.resolve("FTC-ExtendedItems");
    Path antiGrief    = pluginDir.resolve("FTC-AntiGrief");
    Path rw           = pluginDir.resolve("FTC-ResourceWorld");
    Path titles       = pluginDir.resolve("FTC-UserTitles");
    Path structs      = pluginDir.resolve("FTC-Structures");
    Path scripts      = pluginDir.resolve("FTC-Scripting");
    Path guilds       = pluginDir.resolve("FTC-Guilds");
    Path dungeons     = pluginDir.resolve("FTC-Dungeons");
    Path dialogues    = pluginDir.resolve("FTC-Dialogues");
    Path challenges   = pluginDir.resolve("FTC-Challenges");
    Path usables      = pluginDir.resolve("FTC-Usables");
    Path sellshop     = pluginDir.resolve("FTC-SellShop");
    Path shops        = pluginDir.resolve("FTC-Shops");
    Path kingship     = pluginDir.resolve("FTC-Kingship");

    copyFromOld("icons",                  serverlist.resolve("icons"));

    copyFromOld("punishments.json",       antiGrief.resolve("punishments.json"));
    copyFromOld("banned_words.json",      antiGrief.resolve("banned_words.json"));

    copyFromOld("waypoints.dat",          waypoints.resolve("waypoints.dat"));
    copyFromOld("rw_data",                rw.resolve("sections"));
    copyFromOld("user/ranks.toml",        titles.resolve("ranks.toml"));
    copyFromOld("weapon_abilities",       specialItems.resolve("sword_abilities"));
    copyFromOld("scripts",                scripts.resolve("scripts"));
    copyFromOld("structures",             structs);
    copyFromOld("guilds",                 guilds);
    copyFromOld("dungeons",               dungeons);
    copyFromOld("dialogues",              dialogues);
    copyFromOld("challenges",             challenges);
    copyFromOld("usables",                usables);
    copyFromOld("economy/crops.shop",     sellshop.resolve("crops.shop"));
    copyFromOld("economy/drops.shop",     sellshop.resolve("drops.shop"));
    copyFromOld("economy/minerals.shop",  sellshop.resolve("minerals.shop"));
    copyFromOld("economy/mining.shop",    sellshop.resolve("mining.shop"));
    copyFromOld("economy/shops.json",     sellshop.resolve("shops.json"));
    copyFromOld("economy/markets",        shops.resolve("markets"));

    copyFromOld("cooldowns.json",         core.resolve("cooldowns.json"));
    copyFromOld("stored_inventories.dat", core.resolve("stored_inventories.dat"));
    copyFromOld("user/king.json",         kingship.resolve("data.json"));

    return true;
  }

  private void copyFromOld(String oldName, Path dest) {
    if (!copySafe(oldPluginDir.resolve(oldName), dest)) {
      return;
    }

    logger.info("Copied old FTC file '{}' to '{}'", oldName, dest);
  }
}
