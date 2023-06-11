package net.forthecrown.utils.io.eon;

import static net.forthecrown.utils.io.eon.TokenType.ASSIGN;
import static net.forthecrown.utils.io.eon.TokenType.BINARY;
import static net.forthecrown.utils.io.eon.TokenType.COMMA;
import static net.forthecrown.utils.io.eon.TokenType.DOT;
import static net.forthecrown.utils.io.eon.TokenType.FALSE;
import static net.forthecrown.utils.io.eon.TokenType.HEX;
import static net.forthecrown.utils.io.eon.TokenType.IDENTIFIER;
import static net.forthecrown.utils.io.eon.TokenType.NAN;
import static net.forthecrown.utils.io.eon.TokenType.NEG_INFINITY;
import static net.forthecrown.utils.io.eon.TokenType.NULL;
import static net.forthecrown.utils.io.eon.TokenType.NUMBER;
import static net.forthecrown.utils.io.eon.TokenType.OCTAL;
import static net.forthecrown.utils.io.eon.TokenType.PARENTHESES_CLOSE;
import static net.forthecrown.utils.io.eon.TokenType.PARENTHESES_OPEN;
import static net.forthecrown.utils.io.eon.TokenType.POS_INFINITY;
import static net.forthecrown.utils.io.eon.TokenType.QUOTED_STRING;
import static net.forthecrown.utils.io.eon.TokenType.SCOPE_BEGIN;
import static net.forthecrown.utils.io.eon.TokenType.SCOPE_END;
import static net.forthecrown.utils.io.eon.TokenType.SQUARE_CLOSE;
import static net.forthecrown.utils.io.eon.TokenType.SQUARE_OPEN;
import static net.forthecrown.utils.io.eon.TokenType.TRUE;
import static net.forthecrown.utils.io.eon.TokenType.UNKNOWN;
import static net.forthecrown.utils.io.parse.CharReader.IS_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.forthecrown.utils.io.parse.CharReader;
import net.forthecrown.utils.io.parse.Location;
import net.forthecrown.utils.io.parse.ParseErrorFactory;

class Lexer {

  private static final Pattern NUMBER_PATTERN
      = Pattern.compile("[+-]?([0-9]+)?(\\.([0-9]+([eE][+-]?[0-9]+)?)?)?");

  private final CharReader source;

  public Lexer(CharReader source) {
    this.source = source;
  }

  public ParseErrorFactory factory() {
    return source.factory();
  }

  public TokenList lex() {
    List<Token> tokens = new ArrayList<>();
    Token n;

    do {
      source.skipEmpty();
      var l = source.location();

      n = nextToken(l).withLocation(l);
      tokens.add(n);
    } while (n.type() != TokenType.EOF);

    Token[] buf = tokens.toArray(Token[]::new);
    return new TokenList(buf);
  }

  private Token nextToken(Location l) {
    int peek = source.peek();

    if (peek == -1) {
      return TokenType.EOF.token();
    }

    return switch (peek) {
      case '"' -> {
        String quoted;

        if (source.startsWith("\"\"\"")) {
          quoted = source.readTripleQuoted();
        } else {
          quoted = source.readQuoted();
        }

        yield QUOTED_STRING.token(quoted);
      }

      case '\'' -> {
        String quoted = source.readQuoted();
        yield QUOTED_STRING.token(quoted);
      }

      case '[' -> {
        source.skip();
        yield SQUARE_OPEN.token();
      }

      case ']' -> {
        source.skip();
        yield SQUARE_CLOSE.token();
      }

      case '{' -> {
        source.skip();
        yield SCOPE_BEGIN.token();
      }

      case '}' -> {
        source.skip();
        yield SCOPE_END.token();
      }

      case '(' -> {
        source.skip();
        yield PARENTHESES_OPEN.token();
      }

      case ')' -> {
        source.skip();
        yield PARENTHESES_CLOSE.token();
      }

      case '=', ':' -> {
        source.skip();
        yield ASSIGN.token();
      }

      case '.' -> {
        source.skip();
        yield DOT.token();
      }

      case ',' -> {
        source.skip();
        yield COMMA.token();
      }

      case '0' -> {
        source.skip();
        peek = source.peek();

        if (peek == 'x' || peek == 'X') {
          source.skip();
          String hex = source.readHex();

          if (hex.isEmpty()) {
            throw factory().create(l, "Invalid hex sequence");
          }

          yield HEX.token(hex);
        }

        if (peek == 'b' || peek == 'B') {
          source.skip();
          String binary = source.readBinary();

          if (binary.isEmpty()) {
            throw factory().create(l, "Invalid binary sequence");
          }

          yield BINARY.token(binary);
        }

        if (peek == 'o' || peek == 'O') {
          source.skip();
          String octal = source.readOctal();

          if (octal.isEmpty()) {
            throw factory().create(l, "Invalid octal sequence");
          }

          yield OCTAL.token(octal);
        }

        if (isDigit(peek)) {
          yield readNumeric(l);
        }

        yield NUMBER.token("0");
      }

      default -> {
        if (!IS_ID.test(peek)) {
          source.skip();
          yield UNKNOWN.token(Character.toString(peek));
        }

        String identifier = source.readIdentifier();

        switch (identifier.toLowerCase()) {
          case "inf":
          case "infinity":
          case "+inf":
          case "+infinity":
            yield POS_INFINITY.token();

          case "-inf":
          case "-infinity":
            yield NEG_INFINITY.token();

          case "nan":
          case "+nan":
          case "-nan":
            yield NAN.token();

          case "null":
            yield NULL.token();

          case "true":
            yield TRUE.token();

          case "false":
            yield FALSE.token();
        }

        if (isNumeric(identifier)) {
          yield NUMBER.token(identifier);
        }

        yield IDENTIFIER.token(identifier);
      }
    };
  }

  private static boolean isDigit(int ch) {
    return (ch >= '0' && ch <= '9') || ch == '.' || ch == 'e' || ch == 'E';
  }

  private Token readNumeric(Location l) {
    String s = source.readNumber();
    return toNumeric(s, l);
  }

  private boolean isNumeric(String s) {
    return NUMBER_PATTERN.matcher(s).matches();
  }

  private Token toNumeric(String s, Location l) {
    if (!isNumeric(s)) {
      throw factory().create(l, "Malformed number '%s'", s);
    }

    return NUMBER.token(s);
  }
}