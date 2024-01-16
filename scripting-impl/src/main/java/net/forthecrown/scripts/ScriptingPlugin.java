package net.forthecrown.scripts;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.Map;
import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.scripts.commands.CommandJs;
import net.forthecrown.scripts.commands.ScriptingCommand;
import net.forthecrown.scripts.listeners.ScriptListeners;
import net.forthecrown.scripts.pack.PackManager;
import net.forthecrown.scripts.preprocessor.PreProcessor;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
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

    // Commands
    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new ScriptingCommand(this));
    new CommandJs();

    // Events
    ScriptListeners.registerAll(this);

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
    PluginJar.saveResources("config.toml");
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