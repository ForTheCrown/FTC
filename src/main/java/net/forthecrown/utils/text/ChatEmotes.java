package net.forthecrown.utils.text;

import static net.forthecrown.core.Messages.HEART;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

/**
 * Stores, formats and provides suggestions for emotes
 */
public final class ChatEmotes {
  private ChatEmotes() {}

  public static final char EMOTE_CHAR = ':';
  public static final Map<String, Component> TOKEN_2_EMOTE = new HashMap<>();

  static {
    register("shrug", "¯\\_(ツ)_/¯");
    register("ughcry", "(ಥ﹏ಥ)");
    register("hug", "༼ つ ◕_◕ ༽つ");
    register("hugcry", "༼ つ ಥ_ಥ ༽つ");
    register("bear", "ʕ• ᴥ •ʔ");
    register("smooch", "( ^ 3^) ❤");
    register("why", "ლ(ಠ益ಠლ)");
    register("tableflip", "(ノಠ益ಠ)ノ彡┻━┻");
    register("tableput", " ┬──┬ ノ( ゜-゜ノ)");
    register("pretty", "(◕‿◕ ✿)");
    register("sparkle", "(ﾉ◕ヮ◕)ﾉ*･ﾟ✧");
    register("blush", "(▰˘◡˘▰)");
    register("sad", "(._. )");
    register("pleased", "(ᵔᴥᵔ)");
    register("fedup", "(¬_¬)");
    register("reallysad", "(◉︵◉ )");
    register("heart", HEART);
    register("skull", "☠");
  }

  private static void register(String key, String emote) {
    register(key, Text.LEGACY.deserialize(emote));
  }

  private static void register(String key, Component emote) {
    var formattedKey = EMOTE_CHAR + key + EMOTE_CHAR;
    TOKEN_2_EMOTE.put(formattedKey, emote);
  }

  public static Component format(Component input) {
    var result = input;

    for (var e : TOKEN_2_EMOTE.entrySet()) {
      result = result.replaceText(
          TextReplacementConfig.builder()
              .matchLiteral(e.getKey())
              .replacement(e.getValue())
              .build()
      );
    }

    return result;
  }

  public static CompletableFuture<Suggestions> addSuggestions(
      SuggestionsBuilder builder
  ) {
    var token = builder.getRemainingLowerCase();

    for (var e : TOKEN_2_EMOTE.entrySet()) {
      if (!Completions.matches(token, e.getKey())) {
        continue;
      }

      builder.suggest(e.getKey(), Grenadier.toMessage(e.getValue()));
    }

    return builder.buildFuture();
  }
}