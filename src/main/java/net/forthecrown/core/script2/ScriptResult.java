package net.forthecrown.core.script2;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.core.FTC;

@Builder(builderClassName = "Builder")
@RequiredArgsConstructor(staticName = "of")
public class ScriptResult {

  /** The script that executed the method */
  @Getter
  private final Script script;

  /** The name of them method that was executed */
  @Getter
  @Accessors(fluent = true)
  private final Optional<String> method;

  private final Object result;

  private final Throwable exception;

  public Optional<Object> result() {
    return Optional.ofNullable(result);
  }

  public Optional<Throwable> error() {
    return Optional.ofNullable(exception);
  }

  public ScriptResult throwIfError() {
    if (error().isPresent()) {
      throw new IllegalStateException(error().get());
    }

    return this;
  }

  /**
   * Logs the error message this result represents, if there is an error message
   * @return This
   */
  public ScriptResult logIfError() {
    error().ifPresent(e -> {
      if (method.isEmpty()) {
        FTC.getLogger().error(
            "Couldn't evaluate script {}", script.getName(),
            e
        );

        return;
      }

      FTC.getLogger().error(
          "Couldn't invoke method '{}' in '{}'",
          method.get(),
          script.getName(),
          e
      );
    });

    return this;
  }

  /** Closes the underlying script this result came from */
  public void close() {
    getScript().close();
  }

  public Optional<Boolean> asBoolean() {
    return result().flatMap(o -> {
      if (o instanceof Boolean b) {
        return Optional.of(b);
      }

      if (o instanceof Number number) {
        return Optional.of(
            number.longValue() != 0
        );
      }

      if (o instanceof String str) {
        return Optional.of(
            Boolean.parseBoolean(str)
        );
      }

      return Optional.empty();
    });
  }
}