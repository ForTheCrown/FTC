package net.forthecrown.dialogues;

import java.nio.file.Path;
import lombok.Getter;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;

public class DialogueManager {

  @Getter
  private final Path directory;

  @Getter
  private final Registry<Dialogue> registry = Registries.newRegistry();

  DialogueManager() {
    this.directory = PathUtil.pluginPath();
  }

  public void load() {
    PathUtil.ensureDirectoryExists(directory);
    PluginJar.saveResources("dialogues", directory);

    registry.clear();

    PathUtil.iterateDirectory(directory, true, true, path -> {
      var relative = directory.relativize(path);
      var str = relative.toString().replace(".json", "");

      SerializationHelper.readAsJson(path, json -> {
        var entry = Dialogue.deserialize(json);
        registry.register(str, entry);
      });
    });
  }
}