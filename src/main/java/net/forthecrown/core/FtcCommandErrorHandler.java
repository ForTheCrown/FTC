package net.forthecrown.core;

import com.mojang.brigadier.StringReader;
import net.forthecrown.grenadier.CommandExceptionHandler;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;

public class FtcCommandErrorHandler implements CommandExceptionHandler {

  static final CommandExceptionHandler DEFAULT
      = Grenadier.getProvider().getExceptionHandler();

  static void init() {
    Grenadier.getProvider().setExceptionHandler(new FtcCommandErrorHandler());
  }

  @Override
  public void onCommandException(StringReader input,
                                 Throwable throwable,
                                 CommandSource source
  ) {
    DEFAULT.onCommandException(input, throwable, source);
  }

  @Override
  public void onSuggestionException(String input, Throwable throwable,
                                    CommandSource source
  ) {
    if (throwable.getMessage() != null
        && throwable.getMessage().contains("No such argument '")
    ) {
      return;
    }

    DEFAULT.onSuggestionException(input, throwable, source);
  }
}