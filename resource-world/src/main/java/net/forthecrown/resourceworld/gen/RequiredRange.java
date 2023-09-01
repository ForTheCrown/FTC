package net.forthecrown.resourceworld.gen;

import java.util.Objects;
import lombok.Getter;

@Getter
public class RequiredRange<T> {

  private final T value;

  private final Integer min;
  private final Integer max;

  public RequiredRange(T value, Integer min, Integer max) {
    Objects.requireNonNull(value, "Null value");

    this.value = value;
    this.min = min;
    this.max = max;
  }

  public boolean contains(int value) {
    return (min != null && value >= min)
        && (max != null && value <= max);
  }
}
