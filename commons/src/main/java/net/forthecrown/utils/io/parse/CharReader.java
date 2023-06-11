package net.forthecrown.utils.io.parse;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.IntPredicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.io.parse.CharReadPredicate.Result;

public final class CharReader {

  public static final int EOF = -1;

  public static final int UNICODE_LENGTH = 4;

  public static final IntPredicate IS_ID = ch -> {
    return (ch >= 'a' && ch <= 'z')
        || (ch >= 'A' && ch <= 'Z')
        || (ch >= '0' && ch <= '9')
        || ch == '_'
        || ch == '+'
        || ch == '.'
        || ch == '-'
        || ch == '$';
  };

  public static final CharReadPredicate IS_HEX_CHAR = ch -> {
    if ((ch >= '0' && ch <= '9')
        || (ch >= 'a' && ch <= 'f')
        || (ch >= 'A' && ch <= 'F')
    ) {
      return Result.MATCHES;
    }

    return ch == '_' ? Result.SKIP : Result.INVALID;
  };

  public static final CharReadPredicate IS_OCTAL_CHAR = ch -> {
    if (ch >= '0' && ch <= '7') {
      return Result.MATCHES;
    }

    return ch == '_' ? Result.SKIP : Result.INVALID;
  };

  public static final CharReadPredicate IS_BINARY_CHAR = ch -> {
    if (ch == '1' || ch == '0') {
      return Result.MATCHES;
    }

    return ch == '_' ? Result.SKIP : Result.INVALID;
  };

  private final ParseErrorFactory factory;

  private int line   = 1;
  private int column = 1;
  private int index  = 0;

  private final StringBuffer readbuf = new StringBuffer();
  private final StringBuffer quotebuf = new StringBuffer();

  private final StringBuffer input;

  private final WhitespaceConfig whitespaceSettings = new WhitespaceConfig();

  public CharReader(StringBuffer input, ParseErrorFactory factory) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(factory);

    this.factory = factory;
    this.input = input;
  }

  public ParseErrorFactory factory() {
    return factory;
  }

  public int read() throws ParseException {
    if (index >= input.length()) {
      return -1;
    }

    int c = input.codePointAt(index++);

    if (c == EOF) {
      return c;
    }

    if (c == '\n') {
      line++;
      column = 1;
    } else {
      column++;
    }

    return c;
  }

  public int peek() throws ParseException {
    return peek(0);
  }

  private int peek(int i) {
    int peekIndex = index + i;

    if (peekIndex >= input.length()) {
      return -1;
    }

    return input.codePointAt(peekIndex);
  }

  public String expect(String s) throws ParseException {
    Location loc = location();

    if (startsWith(s)) {
      return s;
    }

    throw factory.create(loc, "Expected '%s'", s);
  }

  public boolean startsWith(String s) {
    if (index >= input.length()) {
      return false;
    }

    String remaining = input.substring(index);

    if (remaining.length() < s.length()) {
      return false;
    }

    return remaining.startsWith(s);
  }

  public int expect(int... chars) throws ParseException {
    var loc = location();
    int r = read();

    for (var c: chars) {
      if (r == c) {
        return r;
      }
    }

    throw factory().create(loc, "Expected %s, found %s",
        codepointsToString(chars),
        codepointToString(r)
    );
  }

  private String codepointsToString(int... ints) {
    if (ints.length == 1) {
      return codepointToString(ints[0]);
    }

    StringJoiner joiner = new StringJoiner(", ");
    for (int anInt : ints) {
      joiner.add(codepointToString(anInt));
    }
    return "one of " + joiner;
  }

  private String codepointToString(int i) {
    return i == EOF ? "EOF" : Character.toString(i);
  }

  private void clearReadBuffer() {
    readbuf.delete(0, readbuf.length());
  }

  public String readString(int maxLength, CharReadPredicate codepointPredicate)
      throws ParseException
  {
    clearReadBuffer();

    while (maxLength == -1 || readbuf.length() < maxLength) {
      int peek = peek();
      Result res = codepointPredicate.matchesCharacter(peek);

      if (res == Result.INVALID || peek == EOF) {
        break;
      }

      skip();

      if (res == Result.SKIP) {
        continue;
      }

      readbuf.appendCodePoint(peek);
    }

    return readbuf.toString();
  }

  public String readString(int maxLength, IntPredicate codepointPredicate) throws ParseException {
    clearReadBuffer();

    while ((maxLength == -1 || readbuf.length() < maxLength)
        && codepointPredicate.test(peek())
        && peek() != EOF
    ) {
      readbuf.appendCodePoint(read());
    }

    return readbuf.toString();
  }

  public String readQuoted(boolean allowNewline) throws ParseException {
    int quote = expect('"', '\'', '`');
    readQuotedInterior(false, allowNewline, quote);
    expect(quote);
    return normalizeNewline(quotebuf.toString());
  }

  public String readTripleQuoted() throws ParseException {
    clearReadBuffer();

    expectTriple();
    readQuotedInterior(true, true, '"');
    expectTriple();

    return normalizeNewline(quotebuf.toString());
  }

  static String normalizeNewline(String s) {
    return s.replace("\r\n", "\n")
        .replace("\r", "\n");
  }

  public void skipNewline() {
    int first = peek();
    int second = peek(1);

    if (first == '\r' && second == '\n') {
      skip(2);
    } else {
      skip();
    }
  }

  private void readQuotedInterior(boolean tripleQuotes, boolean allowNewlines, int quote) {
    if (!quotebuf.isEmpty()) {
      quotebuf.delete(0, quotebuf.length());
    }

    boolean escaped = false;

    while (true) {
      int peek = peek();
      var loc = location();

      if (peek == EOF) {
        throw factory.create(loc, "End of input inside quoted string");
      }

      // Handle new lines
      if (peek == '\n' || peek == '\r') {
        if (escaped) {
          skipNewline();
          escaped = false;
          quotebuf.append("\n");

          continue;
        }

        if (!allowNewlines) {
          throw factory.create(loc,
              "Encountered line break inside quoted string "
                  + "(use '\\' to escape line breaks)"
          );
        }

        skipNewline();
        quotebuf.append("\n");

        continue;
      }

      // Handle escaping
      if (peek == '\\') {
        read();

        if (escaped) {
          quotebuf.append("\\");
          escaped = false;
          continue;
        }

        escaped = true;
        continue;
      }

      // Handle quote characters
      if (peek == quote) {
        if (escaped) {
          quotebuf.appendCodePoint(quote);
          read();
          escaped = false;

          continue;
        }

        if (tripleQuotes) {
          if (startsWith("\"\"\"")) {
            break;
          }
        } else {
          break;
        }
      }

      read();

      // Handle escape characters
      if (escaped) {
        switch (peek) {
          case 'u' -> {
            int hexCode = readUnicodeHex(loc);
            quotebuf.appendCodePoint(hexCode);
          }

          case 'n' -> quotebuf.append("\n");
          case 't' -> quotebuf.append("\t");
          case 'r' -> quotebuf.append('\r');
          case 'b' -> quotebuf.append("\b");
          case 'f' -> quotebuf.append("\f");

          default -> throw factory.create(loc,
              "Invalid escape character: '%c'", peek
          );
        }

        escaped = false;
        continue;
      }

      // Append character to buffer
      quotebuf.appendCodePoint(peek);
    }
  }

  private int readUnicodeHex(Location location) {
    String unicode = readString(UNICODE_LENGTH, IS_HEX_CHAR);

    if (unicode.length() < UNICODE_LENGTH) {
      throw factory.create(location, "Invalid hex sequence");
    }

    try {
      return Integer.parseUnsignedInt(unicode, 16);
    } catch (NumberFormatException exc) {
      throw factory.create(location, "Invalid hex sequence");
    }
  }

  private void expectTriple() {
    expect("\"\"\"");
  }

  public void skip(int chars) {
    for (int i = 0; i < chars; i++) {
      read();
    }
  }

  public void skipEmpty() throws ParseException {
    while (true) {
      int p = peek();

      if (p == EOF) {
        return;
      }

      // If EOL matters, don't skip it
      if ((p == '\n' || p == '\r')
          && whitespaceSettings.eolImportant()
      ) {
        return;
      }

      if (Character.isWhitespace(p)) {
        skip();
        continue;
      }

      if (p == '#' && whitespaceSettings.hashtagComments()) {
        skipLineComment();
        continue;
      }

      if (p == '/') {
        int next = peek(1);

        if (next == '/' && whitespaceSettings.lineComments()) {
          skipLineComment();
          continue;
        }

        if (next == '*' && whitespaceSettings.blockComments()) {
          skipBlockComment();
          continue;
        }
      }

      return;
    }
  }

  private void skipLineComment() {
    int i;

    while ((i = read()) != '\n' && i != '\r' && i != -1) {
      // Nothing needed, condition already moves the reader
      // forward with the read() call
    }
  }

  private void skipBlockComment() {
    while (true) {
      int p = read();

      if (p == -1) {
        throw factory.create(location(), "Star comment has no end");
      }

      if (p == '*' && peek() == '/') {
        skip();
        return;
      }
    }
  }

  public Location location() {
    return Location.of(line, column, index);
  }

  public void location(Location location) {
    Objects.requireNonNull(location, "Null location");
    this.line = location.line();
    this.column = location.column();
    this.index = location.index();
  }

  public WhitespaceConfig whitespaceSettings() {
    return whitespaceSettings;
  }

  public StringBuffer input() {
    return input;
  }

  public String readString(CharReadPredicate codepointPredicate) throws ParseException {
    return readString(-1, codepointPredicate);
  }

  public String readString(IntPredicate predicate) throws ParseException {
    return readString(-1, predicate);
  }

  public String readOctal() throws ParseException {
    return readString(IS_OCTAL_CHAR);
  }

  public String readHex() throws ParseException {
    return readString(IS_HEX_CHAR);
  }

  public String readBinary() throws ParseException {
    return readString(IS_BINARY_CHAR);
  }

  public String readNumber() throws ParseException {
    return readString(new NumberCharPredicate());
  }

  public String readJavaIdentifier() throws ParseException {
    return readString(new JavaIdPredicate());
  }

  public String readIdentifier() throws ParseException {
    return readString(IS_ID);
  }

  public String readQuoted() throws ParseException {
    return readQuoted(false);
  }

  public void skip() {
    skip(1);
  }

  @Getter @Setter
  @Accessors(fluent = true, chain = true)
  static class WhitespaceConfig {

    private boolean blockComments   = true;
    private boolean lineComments    = true;
    private boolean hashtagComments = true;

    private boolean eolImportant    = false;
  }
}