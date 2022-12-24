package net.forthecrown.utils.context;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Context {

  /**
   * A context that has no options
   */
  public static final Context EMPTY = new Context(null, null) {
    @Override
    public <T> boolean has(@NotNull ContextOption<T> option) {
      return false;
    }

    @Override
    public <T> T get(@NotNull ContextOption<T> option) {
      return option.getDefaultValue();
    }

    @Override
    public <T> Context set(@NotNull ContextOption<T> option, T value) {
      return this;
    }
  };

  /**
   * The set that created this context
   */
  @Getter
  private final ContextSet contextSet;

  /**
   * The option value array, {@link ContextOption#getIndex()} is used to get the value
   */
  private final Object[] options;

  /**
   * Gets an option's value
   *
   * @param option The option to get the value of
   * @param <T>    The option's type
   * @return The gotten value
   */
  public <T> @Nullable T get(@NotNull ContextOption<T> option) {
    validateOption(option);
    return (T) options[option.getIndex()];
  }

  public <T> @NotNull T getOrThrow(@NotNull ContextOption<T> option) {
    return Optional.ofNullable(get(option)).orElseThrow();
  }

  public <T> Context set(@NotNull ContextOption<T> option, @Nullable T value) {
    validateOption(option);
    options[option.getIndex()] = value;
    return this;
  }

  private void validateOption(ContextOption option) {
    Validate.isTrue(contextSet.has(option), "Invalid option, not contained in parent set");
  }

  public <T> boolean has(@NotNull ContextOption<T> option) {
    return contextSet.has(option);
  }
}