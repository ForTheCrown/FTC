package net.forthecrown.utils.io.eon;

import net.forthecrown.utils.io.parse.ParseErrorFactory;

class TokenList {

  private final Token[] tokens;
  private int cursor;

  public TokenList(Token[] tokens) {
    this.tokens = tokens;
    this.cursor = 0;
  }

  public boolean hasNext() {
    return cursor < tokens.length;
  }

  private Token eof() {
    return tokens[tokens.length - 1];
  }

  public Token expect(ParseErrorFactory factory, TokenType... types) {
    var token = next();
    token.expect(factory, types);
    return token;
  }

  public Token next() {
    if (!hasNext()) {
      return eof();
    }

    return tokens[cursor++];
  }

  public Token peek() {
    if (!hasNext()) {
      return eof();
    }

    return tokens[cursor];
  }

  public Token[] toArray() {
    return tokens.clone();
  }

  public int cursor() {
    return cursor;
  }

  public void cursor(int cursor) {
    this.cursor = cursor;
  }
}