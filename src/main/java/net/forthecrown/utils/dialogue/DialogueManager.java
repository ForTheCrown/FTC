package net.forthecrown.utils.dialogue;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryIndex;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

public class DialogueManager {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final String COMMAND_NAME = "npc_dialogue";

  @Getter
  private static final DialogueManager dialogues = new DialogueManager();

  private final LongSet usedIds = new LongOpenHashSet();
  private final Random idGenerator = new Random();

  @Getter
  private final Path directory;

  @Getter
  private final Registry<Dialogue> registry = Registries.newRegistry();

  private final RegistryIndex<Dialogue, String> randomIdIndex
      = new RegistryIndex<>(holder -> holder.getValue().getDisguisedId());

  private DialogueManager() {
    registry.setListener(randomIdIndex);

    this.directory = PathUtil.getPluginDirectory("dialogues");

    try {
      FtcJar.saveResources(
          "dialogues",
          directory,
          ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
      );
    } catch (IOException exc) {
      LOGGER.error("Error saving default conversations", exc);
    }
  }

  String generateDisguisedId() {
    return Long.toUnsignedString(generateId(), Character.MAX_RADIX);
  }

  long generateId() {
    int safeGuard = 256;

    while (true) {
      long l = idGenerator.nextLong();

      if (usedIds.add(l)) {
        return l;
      }

      safeGuard--;

      if (safeGuard < 0) {
        throw new IllegalStateException("Couldn't generate ID in 256 attempts");
      }
    }
  }

  public void run(User user, StringReader reader) throws CommandSyntaxException {
    String key = reader.readUnquotedString();
    Dialogue entry = randomIdIndex.lookupValue(key).orElse(null);

    if (entry == null) {
      LOGGER.warn("Unknown dialogue node '{}', input={}",
          key, reader.getString()
      );

      return;
    }

    reader.skipWhitespace();
    if (!reader.canRead()) {
      if (entry.getEntryNode() == null) {
        LOGGER.warn(
            "Entry {} cannot be executed! No 'entry_node' set, input={}",
            key, reader.getString()
        );

        return;
      }

      entry.run(user);
    }

    String word = reader.readUnquotedString();
    var node = entry.getNodeByRandomId(word);

    if (node == null) {
      LOGGER.warn("No node found with ID '{}' input={}",
          word, reader.getString()
      );

      return;
    }

    if (!node.test(user)) {
      return;
    }

    DialogueRenderer renderer = new DialogueRenderer(user, entry);
    var text = node.render(renderer);

    user.sendMessage(text);
  }

  @OnLoad
  public void load() {
    registry.clear();
    usedIds.clear();

    PathUtil.iterateDirectory(directory, true, true, path -> {
      var relative = directory.relativize(path);
      var str = relative.toString().replaceAll(".json", "");

      SerializationHelper.readJsonFile(path, wrapper -> {
        var entry = Dialogue.deserialize(wrapper);
        registry.register(str, entry);
      });
    });
  }
}