package net.forthecrown.scripts;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.Map;
import net.forthecrown.BukkitServices;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.scripts.commands.CommandJs;
import net.forthecrown.scripts.commands.ScriptingCommand;
import net.forthecrown.scripts.pack.PackManager;
import net.forthecrown.scripts.preprocessor.PreProcessor;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class ScriptingPlugin extends JavaPlugin {

  private ScriptManager service;
  private PackManager packs;

  @Override
  public void onEnable() {
    Path pluginDir = PathUtil.pluginPath();
    Path scriptsDir = pluginDir.resolve("scripts");
    Path packsDir = pluginDir.resolve("packs");

    service = new ScriptManager(scriptsDir, this);
    packs = new PackManager(service, packsDir);

    Scripts.setService(service);
    BukkitServices.register(ScriptService.class, service);

    ScriptPlaceholders.registerAll();

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new ScriptingCommand());

    new CommandJs();

    reload();
  }

  @Override
  public void onDisable() {
    packs.close();
    service.close();
    ScriptPlaceholders.removeAll();
  }

  public void reload() {
    reloadConfig();
    packs.reload();
  }

  @Override
  public void reloadConfig() {
    saveResource("config.toml", false);
    PreProcessor.setImportPlaceholders(null);

    SerializationHelper.readAsJson(
        getDataFolder().toPath().resolve("config.toml"),
        this::loadConfigFrom
    );
  }

  private void loadConfigFrom(JsonWrapper json) {
    if (json.has("importPlaceholders")) {
      Map<String, String> importPlaceholders = json.getMap(
          "importPlaceholders",
          s -> s,
          JsonElement::getAsString
      );

      PreProcessor.setImportPlaceholders(importPlaceholders);
    } else {
      PreProcessor.setImportPlaceholders(null);
    }
  }
}