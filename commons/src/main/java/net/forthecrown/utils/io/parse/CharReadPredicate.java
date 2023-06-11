package net.forthecrown.utils.io.parse;

public interface CharReadPredicate {

  Result matchesCharacter(int codePoint);

  enum Result {
    MATCHES,
    SKIP,
    INVALID
  }
}