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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    append(builder);
    return builder.toString();
  }

  private void append(StringBuilder builder) {
    switch (operation) {
      case NOT_EQUALS -> builder.append("!=");
      case EQUALS -> builder.append("=");
      case LESS_THAN -> builder.append("<");
      case LESS_THAN_EQUAL -> builder.append("<=");
      case GREATER_THAN -> builder.append(">");
      case GREATER_THAN_EQUAL -> builder.append(">=");
    }

    builder.append(value);

    if (and != null) {
      builder.append("&");
      and.append(builder);
    }
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
