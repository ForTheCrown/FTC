package net.forthecrown.datafix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.io.PathUtil;
import org.apache.logging.log4j.Logger;

/**
 * A class to handle data transformers. A data transformer is anything which
 * trudges through a relatively large amount to data to reformat, fix or change
 * it. Mostly I use these to update data to a new format, like for updating
 * users from YAML -> JSON
 * <p>
 * Data transformers need to have a specific implementation, Normally, each data
 * transformer will be checked if they've been ran before.
 */
public class Transformers {

  private static final Logger LOGGER = Loggers.getLogger();

  // A list of all current data transformers
  // Add and remove entries from here as needed
  private static final DataUpdater[] CURRENT_TRANSFORMERS = {
  };

  private static final Set<String> COMPLETED_TRANSFORMERS = new HashSet<>();

  private static Path getPath() {
    return PathUtil.pluginPath("dataTransformerInfo.txt");
  }

  public static boolean shouldRun(DataUpdater c) {
    if (COMPLETED_TRANSFORMERS.isEmpty()) {
      load();
    }

    return !COMPLETED_TRANSFORMERS.contains(c.getClass().getName());
  }

  @OnLoad
  private static void load() {
    COMPLETED_TRANSFORMERS.clear();

    Path f = getPath();

    if (!Files.exists(f)) {
      return;
    }

    try (BufferedReader reader = Files.newBufferedReader(f)) {
      reader.lines()
          .forEach(s -> {
            COMPLETED_TRANSFORMERS.add(s);
            LOGGER.debug("Loaded completed updater: '{}'", s);
          });
    } catch (IOException e) {
      LOGGER.error("Couldn't load transformer data file", e);
    }
  }

  @OnSave
  public static void save() {
    Path p = getPath();

    try {
      if (COMPLETED_TRANSFORMERS.isEmpty()) {
        Files.deleteIfExists(p);
        return;
      }

      BufferedWriter bufferedWriter = Files.newBufferedWriter(p, StandardCharsets.UTF_8);

      for (var v : COMPLETED_TRANSFORMERS) {
        bufferedWriter.write(v);
        bufferedWriter.newLine();
      }

      bufferedWriter.close();
    } catch (IOException e) {
      LOGGER.info("Couldn't save transformer data file", e);
    }
  }

  public static void runCurrent() {
    for (var c : CURRENT_TRANSFORMERS) {
      runTransformer(c);
    }

    if (DataUpdater.LOGGER.getOutput() != null) {
      DataUpdater.LOGGER.close();
      Loggers.getLogger().info("Completed all data updaters");
    }
  }

  public static void runTransformer(DataUpdater c) {
    if (!shouldRun(c)) {
      return;
    }

    if (DataUpdater.LOGGER.getOutput() == null) {
      DataUpdater.LOGGER.initFilePrinter();
      Loggers.getLogger().info("Running current data updaters!");
    }

    if (!c.runUpdater()) {
      return;
    }

    COMPLETED_TRANSFORMERS.add(c.getClass().getName());
    save();
  }
}