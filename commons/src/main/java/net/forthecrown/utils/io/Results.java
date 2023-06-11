package net.forthecrown.utils.io;

import com.google.errorprone.annotations.FormatString;
import com.mojang.serialization.DataResult;
import lombok.experimental.UtilityClass;

public @UtilityClass class Results {

  public <T> DataResult<T> success(T value) {
    return DataResult.success(value);
  }

  public <T> DataResult<T> error(String msgFormat, Object... args) {
    return DataResult.error(() -> String.format(msgFormat, args));
  }

  public <T> DataResult<T> partial(T partial, @FormatString String msgFormat, Object... args) {
    return DataResult.error(() -> String.format(msgFormat, args), partial);
  }
}