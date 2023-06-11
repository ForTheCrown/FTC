package net.forthecrown.utils.io.parse;

public class NumberCharPredicate implements CharReadPredicate {

  private boolean exponentRead = false;
  private boolean lastWasExponent = false;
  private boolean decimalRead = false;

  @Override
  public Result matchesCharacter(int value) {
    if (value == '_') {
      return Result.SKIP;
    }

    if (lastWasExponent && (value == '+' || value == '-')) {
      lastWasExponent = false;
      return Result.MATCHES;
    }

    if (!decimalRead && value == '.') {
      decimalRead = true;
      return Result.MATCHES;
    }

    if (value >= '0' && value <= '9') {
      lastWasExponent = false;
      return Result.MATCHES;
    }

    if ((value == 'e' || value == 'E') && !exponentRead) {
      exponentRead = true;
      lastWasExponent = true;
      return Result.MATCHES;
    }

    return Result.INVALID;
  }
}