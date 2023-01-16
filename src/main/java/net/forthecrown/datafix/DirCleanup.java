package net.forthecrown.datafix;

import java.nio.file.Path;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.utils.io.PathUtil;

public class DirCleanup extends DataUpdater {

  @Override
  protected boolean update() throws Throwable {
    var directory = ChallengeManager.getInstance()
        .getStorage()
        .getDirectory();

    PathUtil.safeDelete(directory.resolve("challenges.json"))
        .resultOrPartial(LOGGER::error)
        .ifPresent(integer -> LOGGER.info("Deleted old challenges.json"));

    PathUtil.safeDelete(directory.resolve("streak_scripts.json"))
        .resultOrPartial(LOGGER::error)
        .ifPresent(integer -> LOGGER.info("Deleted old streak_scripts.json"));

    PathUtil.safeDelete(directory.resolve("item_challenges.json"))
        .resultOrPartial(LOGGER::error)
        .ifPresent(integer -> LOGGER.info("Deleted old item_challenges.json"));

    directory = ScriptManager.getInstance().getDirectory();
    PathUtil.safeDelete(directory.resolve("loader.json"))
        .resultOrPartial(LOGGER::error)
        .ifPresent(integer -> LOGGER.info("Deleted old script loader.json"));

    PathUtil.safeDelete(Path.of("icons"))
        .resultOrPartial(LOGGER::error)
        .ifPresent(integer -> LOGGER.info("Deleted old icon directory"));

    return true;
  }
}