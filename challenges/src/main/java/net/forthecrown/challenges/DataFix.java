package net.forthecrown.challenges;

import java.nio.file.Path;
import net.forthecrown.datafix.DataUpdater;
import net.forthecrown.datafix.DataUpdaters;
import net.forthecrown.utils.io.PluginJar;

class DataFix {

  static void execute() {
    DataUpdaters updater = DataUpdaters.create();
    updater.addUpdater(new FilesUpdate());
    updater.execute();
  }
}

class FilesUpdate extends DataUpdater {

  @Override
  protected boolean update() {
    Path scripts = Path.of("plugins", "FTC-Scripting", "scripts", "challenges");
    Path dir = Path.of("plugins", "FTC-Challenges");

    Path challengesToml = dir.resolve("challenges.toml");

    safeDelete(scripts);
    safeDelete(challengesToml);

    PluginJar.saveResources("scripts", scripts);
    PluginJar.saveResources("challenges/challenges.toml", challengesToml);

    return true;
  }
}