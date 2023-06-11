package net.forthecrown.utils.io.eon;

public enum TokenType {

  EOF,
  UNKNOWN,

  // Boolean literals
  TRUE,
  FALSE,

  NULL,

  // String types
  QUOTED_STRING,
  IDENTIFIER,

  // Numeric types
  NUMBER,
  HEX,
  OCTAL,
  BINARY,
  POS_INFINITY,
  NEG_INFINITY,
  NAN,

  // Assignment operator
  ASSIGN,
  DOT,
  COMMA,

  // Brackets
  SCOPE_BEGIN,
  SCOPE_END,
  SQUARE_OPEN,
  SQUARE_CLOSE,
  PARENTHESES_OPEN,
  PARENTHESES_CLOSE;

  public Token token() {
    return Token.token(this, null);
  }

  public Token token(String value) {
    return Token.token(this, value, null);
  }
}