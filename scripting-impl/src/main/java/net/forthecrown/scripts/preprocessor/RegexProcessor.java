package net.forthecrown.scripts.preprocessor;

import com.mojang.brigadier.context.StringRange;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract
class RegexProcessor implements Processor {

  private final Pattern pattern;

  @Override
  public void run(StringBuffer buffer, List<PreProcessorCallback> callbacks) {
    Matcher matcher = pattern.matcher(buffer);

    while (matcher.find()) {
      MatchResult result = matcher.toMatchResult();

      int start = result.start();
      int end = result.end();

      StringRange range = StringRange.between(start, end);
      SubWriter writer = new SubWriter(range, buffer);

      process(writer, result, callbacks);
      matcher = pattern.matcher(buffer);
    }
  }

  protected abstract void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  );
}
