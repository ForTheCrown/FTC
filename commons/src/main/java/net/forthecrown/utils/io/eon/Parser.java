package net.forthecrown.utils.io.eon;

import static net.forthecrown.utils.io.eon.TokenType.ASSIGN;
import static net.forthecrown.utils.io.eon.TokenType.BINARY;
import static net.forthecrown.utils.io.eon.TokenType.COMMA;
import static net.forthecrown.utils.io.eon.TokenType.EOF;
import static net.forthecrown.utils.io.eon.TokenType.FALSE;
import static net.forthecrown.utils.io.eon.TokenType.HEX;
import static net.forthecrown.utils.io.eon.TokenType.IDENTIFIER;
import static net.forthecrown.utils.io.eon.TokenType.NAN;
import static net.forthecrown.utils.io.eon.TokenType.NEG_INFINITY;
import static net.forthecrown.utils.io.eon.TokenType.NULL;
import static net.forthecrown.utils.io.eon.TokenType.NUMBER;
import static net.forthecrown.utils.io.eon.TokenType.OCTAL;
import static net.forthecrown.utils.io.eon.TokenType.PARENTHESES_OPEN;
import static net.forthecrown.utils.io.eon.TokenType.POS_INFINITY;
import static net.forthecrown.utils.io.eon.TokenType.QUOTED_STRING;
import static net.forthecrown.utils.io.eon.TokenType.SCOPE_BEGIN;
import static net.forthecrown.utils.io.eon.TokenType.SCOPE_END;
import static net.forthecrown.utils.io.eon.TokenType.SQUARE_CLOSE;
import static net.forthecrown.utils.io.eon.TokenType.SQUARE_OPEN;
import static net.forthecrown.utils.io.eon.TokenType.TRUE;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.utils.io.parse.ParseErrorFactory;

class Parser {

  static final TokenType[] WITH_SCOPE_END = {
      SCOPE_END,
      QUOTED_STRING,
      IDENTIFIER
  };

  static final TokenType[] WITHOUT_SCOPE_END = {
      EOF,
      QUOTED_STRING,
      IDENTIFIER
  };

  private final TokenList tokens;
  private final ParseErrorFactory factory;

  public Parser(TokenList tokens, ParseErrorFactory factory) {
    this.tokens = tokens;
    this.factory = factory;
  }

  private Token expect(TokenType... types) {
    return tokens.expect(factory, types);
  }

  public JsonObject parse() {
    JsonObject compound = new JsonObject();
    parseCompoundInterior(compound, true);
    return compound;
  }

  public JsonObject parseObject() {
    expect(SCOPE_BEGIN);
    JsonObject object = new JsonObject();
    parseCompoundInterior(object, false);
    return object;
  }

  private void parseCompoundInterior(JsonObject compound, boolean topLevel) {
    TokenType[] validStarts = !topLevel ? WITH_SCOPE_END : WITHOUT_SCOPE_END;

    while (true) {
      Token token = expect(validStarts);

      if (token.is(SCOPE_END, EOF)) {
        return;
      }

      String key = token.value();

      Token valueToken = tokens.peek();
      JsonElement value;

      if (valueToken.is(ASSIGN)) {
        tokens.next();
        value = parseElement();
      } else if (valueToken.is(SCOPE_BEGIN, PARENTHESES_OPEN)) {
        value = parseObject();
      } else if (valueToken.is(SQUARE_OPEN)) {
        value = parseArray();
      } else {
        expect(SCOPE_BEGIN, PARENTHESES_OPEN, SQUARE_OPEN, ASSIGN);
        continue;
      }

      compound.add(key, value);

      if (tokens.peek().is(COMMA)) {
        tokens.next();
      }
    }
  }

  public JsonElement parseElement() {
    var peek = tokens.peek();

    if (peek.is(SCOPE_BEGIN, PARENTHESES_OPEN)) {
      return parseObject();
    }

    if (peek.is(SQUARE_OPEN)) {
      return parseArray();
    }

    if (peek.is(TRUE, FALSE)) {
      return parseBoolean();
    }

    if (peek.is(QUOTED_STRING)) {
      return parseString();
    }

    if (peek.is(NULL)) {
      return parseNull();
    }

    if (peek.is(HEX, OCTAL, BINARY, NUMBER, POS_INFINITY, NEG_INFINITY, NAN)) {
      return parseNumber();
    }

    expect(
        SCOPE_BEGIN,    SQUARE_OPEN,
        TRUE,           FALSE,
        QUOTED_STRING,  NULL,
        HEX,            OCTAL,
        BINARY,         NUMBER,
        POS_INFINITY,   NEG_INFINITY,
        NAN
    );

    return null;
  }

  public JsonArray parseArray() {
    expect(SQUARE_OPEN);
    JsonArray array = new JsonArray();

    while (!tokens.peek().is(SQUARE_CLOSE)) {
      JsonElement element = parseElement();
      array.add(element);

      if (tokens.peek().is(COMMA)) {
        tokens.next();
      }
    }

    expect(SQUARE_CLOSE);
    return array;
  }

  public JsonPrimitive parseString() {
    Token token = expect(QUOTED_STRING);
    return new JsonPrimitive(token.value());
  }

  public JsonPrimitive parseNumber() {
    Token token = expect(NUMBER, NAN, HEX, OCTAL, BINARY, POS_INFINITY, NEG_INFINITY);
    var loc = token.location();

    if (token.is(NEG_INFINITY)) {
      return new JsonPrimitive(Double.NEGATIVE_INFINITY);
    }

    if (token.is(POS_INFINITY)) {
      return new JsonPrimitive(Double.POSITIVE_INFINITY);
    }

    if (token.is(NAN)) {
      return new JsonPrimitive(Double.NaN);
    }

    String value = token.value().replace("_", "");

    if (token.is(BINARY)) {
      long bin = Long.parseLong(value, 2);
      return new JsonPrimitive(convertToSmallestType(bin));
    }

    if (token.is(OCTAL)) {
      long octal = Long.parseLong(value, 8);
      return new JsonPrimitive(convertToSmallestType(octal));
    }

    if (token.is(HEX)) {
      long hex = Long.parseLong(value, 16);
      return new JsonPrimitive(convertToSmallestType(hex));
    }

    double val = Double.parseDouble(value);
    return new JsonPrimitive(convertToSmallestType(val));
  }

  private Number convertToSmallestType(Number number) {
    double dval = number.doubleValue();

    if (((byte) dval) == dval) {
      return (byte) dval;
    }

    if (((short) dval) == dval) {
      return (short) dval;
    }

    if (((int) dval) == dval) {
      return (int) dval;
    }

    if (((long) dval) == dval) {
      return (long) dval;
    }

    if (((float) dval) == dval) {
      return (float) dval;
    }

    return number;
  }

  public JsonPrimitive parseBoolean() {
    Token token = expect(TRUE, FALSE);
    return new JsonPrimitive(token.is(TRUE));
  }

  public JsonNull parseNull() {
    expect(NULL);
    return JsonNull.INSTANCE;
  }
}