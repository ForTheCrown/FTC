package net.forthecrown.leaderboards;

import java.util.function.IntPredicate;

public record ScoreFilter(int value, Operation operation, ScoreFilter and)
    implements IntPredicate
{

  @Override
  public boolean test(int value) {
    boolean passed = switch (operation) {
      case EQUALS -> value == this.value;
      case NOT_EQUALS -> value != this.value;
      case LESS_THAN -> value < this.value;
      case LESS_THAN_EQUAL -> value <= this.value;
      case GREATER_THAN -> value > this.value;
      case GREATER_THAN_EQUAL -> value >= this.value;
    };

    if (!passed) {
      return false;
    }

    if (and == null) {
      return true;
    }

    return and.test(value);
  }

  public enum Operation {
    LESS_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN,
    GREATER_THAN_EQUAL,
    EQUALS,
    NOT_EQUALS
  }
}
