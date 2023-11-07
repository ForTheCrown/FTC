package net.forthecrown.antigrief;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;

/**
 * Checks and manages banned words in user messages and inputs.
 */
public final class BannedWords {
  private BannedWords() {}

  private static final Logger LOGGER = Loggers.getLogger();

  private static final String COOLDOWN_CATEGORY = "banned_words";
  private static final int COOLDOWN_TIME = 3 * 60 * 20;

  private static BannedWordsConfig config = BannedWordsConfig.EMPTY;

  public static void load() {
    Path file = PathUtil.pluginPath("banned_words.toml");
    PluginJar.saveResources("banned_words.toml", file);

    SerializationHelper.readAsJson(file, jsonWrapper -> {
      JsonElement element = jsonWrapper.getSource();

      BannedWordsConfig.loadConfig(element)
          .mapError(s -> "Failed to load config: " + s)
          .resultOrPartial(LOGGER::error)
          .ifPresent(cfg -> config = cfg);
    });
  }

  public static boolean contains(String unfiltered) {
    return contains(Text.renderString(unfiltered));
  }

  public static boolean contains(ComponentLike component) {
    return containsBannedWords(Text.plain(component));
  }

  private static boolean containsBannedWords(String input) {
    String filtered = config.filter(input);

    for (Pattern bannedWord : config.bannedWords()) {
      Matcher matcher = bannedWord.matcher(filtered);

      if (matcher.find()) {
        return true;
      }
    }

    return false;
  }

  public static boolean checkAndWarn(CommandSender sender, ComponentLike component) {
    return _checkAndWarn(sender, Text.plain(component));
  }

  public static boolean checkAndWarn(CommandSender sender, String input) {
    return checkAndWarn(sender, Text.renderString(sender, input));
  }

  private static boolean _checkAndWarn(CommandSender sender, String input) {
    boolean senderHasBypass
        = sender == null || sender.hasPermission(ChatParseFlag.IGNORE_CASE.getPermission());

    if (sender == null || (senderHasBypass && config.allowBypass())) {
      return false;
    }

    boolean result = containsBannedWords(input);

    if (result && !Cooldown.containsOrAdd(sender, COOLDOWN_CATEGORY, COOLDOWN_TIME)) {
      sender.sendMessage(GMessages.BAD_LANGUAGE);
    }

    return result;
  }
}