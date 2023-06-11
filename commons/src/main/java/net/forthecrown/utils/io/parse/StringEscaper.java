package net.forthecrown.utils.io.parse;

public class StringEscaper {

  private static final String[] REPLACEMENTS = new String[128];

  static {
    REPLACEMENTS['\n'] = "\\n";
    REPLACEMENTS['\t'] = "\\t";
    REPLACEMENTS['\r'] = "\\r";
    REPLACEMENTS['\b'] = "\\b";
    REPLACEMENTS['\f'] = "\\f";
    REPLACEMENTS['\\'] = "\\\\";
  }

  public static String toQuotedString(String string) {
    return toQuotedString(string, '"');
  }

  public static String toQuotedString(String string, char quote) {
    StringBuffer buf = new StringBuffer(string.length() + 2).append(quote);

    string.codePoints().forEach(ch -> {
      if (ch > 127) {
        String unicode = String.format("\\u%04x", ch);
        buf.append(unicode);
        return;
      }

      if (ch == quote) {
        buf.append("\\").append(quote);
        return;
      }

      String replacement = REPLACEMENTS[ch];

      if (replacement != null) {
        buf.append(replacement);
        return;
      }

      buf.appendCodePoint(ch);
    });

    return buf.append(quote).toString();
  }
}