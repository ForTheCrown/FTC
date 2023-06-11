package net.forthecrown.scripts;

public final class ExecResults {
  private ExecResults() {}

  public static ExecResult<Boolean> toBoolean(ExecResult<Object> result) {
    return result.map(o -> {
      if (o instanceof Boolean bool) {
        return bool;
      }

      if (o instanceof Number number) {
        return number.longValue() != 0;
      }

      String val = String.valueOf(o);
      return Boolean.parseBoolean(val);
    });
  }
}