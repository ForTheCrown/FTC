package net.forthecrown.datafix;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.plugin.Plugin;

public class DataUpdaters {

  @Getter
  final Plugin plugin;

  @Getter
  final Path completedTxt;

  private final List<DataUpdater> updaters = new ArrayList<>();
  private final List<String> completed = new ArrayList<>();

  private DataUpdaters(Plugin plugin) {
    Objects.requireNonNull(plugin);

    this.plugin = plugin;
    this.completedTxt = PathUtil.pluginPath(plugin, "datafixer", "completed.txt");
  }

  public static DataUpdaters create() {
    Plugin caller = PluginUtil.getCallingPlugin();
    return create(caller);
  }

  public static DataUpdaters create(Plugin plugin) {
    return new DataUpdaters(plugin);
  }

  public void addUpdater(DataUpdater updater) {
    Objects.requireNonNull(updater);
    this.updaters.add(updater);
    updater.bind(this);
  }

  public void execute() {
    if (completed.isEmpty()) {
      load();
    }

    for (var u: updaters) {
      runTransformer(u);
    }
  }

  public void load() {
    SerializationHelper.readFile(completedTxt, Files::readAllLines, completed::addAll);
  }

  public void save() {
    SerializationHelper.writeFile(completedTxt, file -> {
      String joined = completed.stream().reduce("", (s, s2) -> s + "\n" + s2);
      Files.writeString(completedTxt, joined);
    });
  }

  public boolean shouldRun(DataUpdater c) {
    if (completed.isEmpty()) {
      load();
    }

    return !completed.contains(c.getClass().getName());
  }

  public void runTransformer(DataUpdater c) {
    if (!shouldRun(c)) {
      return;
    }

    if (!c.runUpdater()) {
      return;
    }

    completed.add(c.getClass().getName());
    save();
  }
}
