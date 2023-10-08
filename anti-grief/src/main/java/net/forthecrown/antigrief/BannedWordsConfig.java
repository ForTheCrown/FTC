package net.forthecrown.antigrief;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.LITERAL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;
import org.apache.commons.lang3.StringUtils;

record BannedWordsConfig(List<InputFilter> filters, boolean allowBypass, Set<Pattern> bannedWords) {

  private static final Codec<Set<Pattern>> REGEX_SET_CODEC = Codec.STRING
      .xmap(s -> {
        try {
          return Pattern.compile(s, CASE_INSENSITIVE);
        } catch (PatternSyntaxException exc) {
          return Pattern.compile(s, LITERAL | CASE_INSENSITIVE);
        }
      }, Pattern::pattern)
      .listOf()
      .xmap(HashSet::new, ArrayList::new);

  public static final BannedWordsConfig EMPTY = new BannedWordsConfig(List.of(), true, Set.of());

  /**
   * Applies loaded input filters
   * @param input Input
   * @return Filtered input
   */
  String filter(String input) {
    if (filters.isEmpty()) {
      return input;
    }

    String result = input;
    for (InputFilter filter : filters) {
      result = filter.apply(result);
    }
    return result;
  }

  static DataResult<BannedWordsConfig> loadConfig(JsonElement element) {
    if (element == null || !element.isJsonObject()) {
      return Results.error("Not an object: %s", element);
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    if (!json.has("words")) {
      return Results.error("No 'words' list found");
    }

    boolean bypassAllowed = json.getBool("bypassAllowed", true);

    DataResult<Set<Pattern>> wordsResult = REGEX_SET_CODEC
        .parse(JsonOps.INSTANCE, json.get("words"))
        .map(Collections::unmodifiableSet);

    DataResult<List<InputFilter>> filtersResult = json.has("input_filters")
        ? loadFilterSet(json.get("input_filters"))
        : Results.success(List.of());

    return wordsResult.apply2(
        (strings, inputFilters) -> new BannedWordsConfig(inputFilters, bypassAllowed, strings),
        filtersResult.mapError(s -> "Failed to load filters list: " + s)
    );
  }

  static DataResult<List<InputFilter>> loadFilterSet(JsonElement element) {
    if (element == null || !element.isJsonArray()) {
      return Results.error("Not an array: %s", element);
    }

    JsonArray array = element.getAsJsonArray();
    DataResult<List<InputFilter>> result = Results.success(new ArrayList<>());

    for (JsonElement jsonElement : array) {
      var filterResult = loadFilter(jsonElement);

      result = result.apply2(
          (filters1, inputFilter) -> {
            filters1.add(inputFilter);
            return filters1;
          },
          filterResult
      );
    }

    return result;
  }

  static DataResult<InputFilter> loadFilter(JsonElement element) {
    if (!element.isJsonObject()) {
      return Results.error("Not an object: %s", element);
    }

    var obj = element.getAsJsonObject();

    if (obj.size() > 1) {
      return Results.error("Only 1 value allowed");
    } else if (obj.size() < 1) {
      return Results.error("No filter values specified");
    }

    String type = obj.keySet().iterator().next();
    JsonElement value = obj.get(type);

    if (value == null || !value.isJsonObject()) {
      return Results.error("Not a valid replacer object: %s", value);
    }

    JsonObject valueObj = value.getAsJsonObject();
    if (valueObj.size() > 1) {
      return Results.error("Too many values in filter value, only 1 allowed");
    } else if (valueObj.size() < 1) {
      return Results.error("Too few values in filter value, 1 key-value pair is required");
    }

    var valueEntry = valueObj.entrySet().iterator().next();

    if (!valueEntry.getValue().isJsonPrimitive()) {
      return Results.error("Not a primitive string value: %s", valueEntry.getValue());
    }

    String replace = valueEntry.getKey();
    String replacement = valueEntry.getValue().getAsString();

    return switch (type.toLowerCase()) {
      case "chars" -> CharsFilter.load(replace, replacement);
      case "regex", "regexp" -> RegexFilter.load(replace, replacement);
      case "literal", "simple" -> LiteralFilter.load(replace, replacement);
      default -> Results.error("Unknown filter type '%s'", type);
    };
  }
}

interface InputFilter {
  String apply(String input);
}

record RegexFilter(Pattern pattern, String replacement) implements InputFilter {

  static DataResult<InputFilter> load(String key, String value) {
    try {
      Pattern pattern = Pattern.compile(key);
      return Results.success(new RegexFilter(pattern, value));
    } catch (PatternSyntaxException exc) {
      return Results.error("Invalid pattern: %s", exc.getMessage());
    }
  }

  @Override
  public String apply(String input) {
    return pattern.matcher(input).replaceAll(replacement);
  }
}

record LiteralFilter(String literal, String replacement) implements InputFilter {

  static DataResult<InputFilter> load(String key, String value) {
    return Results.success(new LiteralFilter(key, value));
  }

  @Override
  public String apply(String input) {
    return input.replace(literal, replacement);
  }
}

record CharsFilter(String chars, String replacementChars) implements InputFilter {

  static DataResult<InputFilter> load(String key, String value) {
    return Results.success(new CharsFilter(key, value));
  }

  @Override
  public String apply(String input) {
    return StringUtils.replaceChars(input, chars, replacementChars);
  }
}
