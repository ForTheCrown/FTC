package net.forthecrown.scripts;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.Map;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.scripts.commands.ScriptingCommand;
import net.forthecrown.BukkitServices;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class ScriptingPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    ScriptService service = new ServiceImpl(Path.of("scripts"), this);
    Scripts.setService(service);
    BukkitServices.register(ScriptService.class, service);

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new ScriptingCommand());
  }

  @Override
  public void onDisable() {
    ServiceImpl service = (ServiceImpl) Scripts.getService();
    service.close();
  }

  @Override
  public void reloadConfig() {
    saveResource("config.toml", false);
    PreProcessor.importPlaceholders = null;

    SerializationHelper.readJsonFile(
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

      PreProcessor.importPlaceholders = importPlaceholders;
    } else {
      PreProcessor.importPlaceholders = null;
    }
  }
}