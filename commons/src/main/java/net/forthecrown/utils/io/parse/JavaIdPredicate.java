package net.forthecrown.utils.io.parse;

import java.util.function.IntPredicate;

public class JavaIdPredicate implements IntPredicate {

  private boolean first = true;

  @Override
  public boolean test(int value) {
    if (first) {
      first = false;
      return Character.isJavaIdentifierStart(value);
    }

    return Character.isJavaIdentifierPart(value);
  }
}