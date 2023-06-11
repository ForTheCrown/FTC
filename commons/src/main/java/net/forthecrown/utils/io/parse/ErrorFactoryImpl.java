package net.forthecrown.utils.io.parse;

import java.io.IOException;
import java.util.Objects;

final class ErrorFactoryImpl implements ParseErrorFactory {

  private final String messagePrefix;
  private final StringBuffer input;

  public ErrorFactoryImpl(String name, StringBuffer input) {
    this.input = input;

    this.messagePrefix = name == null || name.isBlank()
        ? ""
        : "Error reading '%s':\n".formatted(name);
  }

  @Override
  public ParseException wrap(Location location, IOException exc) {
    var res = create(location, exc.getMessage());
    res.setStackTrace(exc.getStackTrace());
    return res;
  }

  @Override
  public ParseException create(Location location, String format, Object... args) {
    Objects.requireNonNull(location);

    String message = messagePrefix + (format == null ? "" : format.formatted(args));
    String formatted = ErrorMessages.format(input, location, message);

    return new ParseException(formatted);
  }
}