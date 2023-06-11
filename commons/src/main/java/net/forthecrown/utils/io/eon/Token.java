package net.forthecrown.utils.io.eon;

import java.util.StringJoiner;
import net.forthecrown.utils.io.parse.Location;
import net.forthecrown.utils.io.parse.ParseErrorFactory;

record Token(TokenType type, String value, Location location) {

  public static Token token(TokenType type, Location location) {
    return new Token(type, "", location);
  }

  public static Token token(TokenType type, String value, Location location) {
    return new Token(type, value, location);
  }

  public boolean is(TokenType... types) {
    for (var t: types) {
      if (t == type) {
        return true;
      }
    }

    return false;
  }

  public void expect(ParseErrorFactory factory, TokenType... types) {
    if (is(types)) {
      return;
    }

    throw factory.create(location, "Expected %s, found %s",
        typesToString(types), type.name().toLowerCase()
    );
  }

  private static String typesToString(TokenType... types) {
    if (types.length == 1) {
      return types[0].name().toLowerCase();
    }

    StringJoiner joiner = new StringJoiner(", ");
    for (TokenType type : types) {
      joiner.add(type.name().toLowerCase());
    }

    return joiner.toString();
  }

  public Token withLocation(Location location) {
    return new Token(type, value, location);
  }
}