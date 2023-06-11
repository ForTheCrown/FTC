package net.forthecrown.utils.io.parse;

final class ErrorMessages {
  private ErrorMessages() {}

  public static String format(StringBuffer input, Location location, String message) {
    int pos = location.index();

    final int lineStart = findLineStart(input, pos);
    final int lineEnd = findLineEnd(input, pos);

    final int lineNumber = location.line();
    final int column = location.column();

    String context = input.substring(lineStart, lineEnd);
    String errorFormat = "%s\n%s\n%" + (column + 1) + "s Line %s Column %s";

    return errorFormat.formatted(
        CharReader.normalizeNewline(message),
        CharReader.normalizeNewline(context),
        "^",
        lineNumber,
        column
    );
  }

  private static int findLineStart(StringBuffer reader, int cursor) {
    return findLineEndStart(reader, cursor, -1);
  }

  private static int findLineEnd(StringBuffer reader, int cursor) {
    return findLineEndStart(reader, cursor, 1);
  }

  private static int findLineEndStart(
      StringBuffer reader,
      int pos,
      int direction
  ) {
    int r = pos;

    while (r >= 0 && r < reader.length()) {
      char c = reader.charAt(r);

      if (c == '\n' /*|| c == '\r'*/) {
        return direction == -1 ? r + 1 : r;
      }

      r += direction;
    }

    return Math.max(0, r);
  }
}