package net.forthecrown.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.function.Supplier;
import net.forthecrown.grenadier.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleType<T> implements UsageType<T> {

  private final Supplier<T> supplier;

  public SimpleType(Supplier<T> supplier) {
    Objects.requireNonNull(supplier);
    this.supplier = supplier;
  }

  @Override
  public T parse(StringReader reader, CommandSource source) {
    return supplier.get();
  }

  @Override
  public T createEmpty() throws UnsupportedOperationException {
    return supplier.get();
  }

  @Override
  public <S> DataResult<T> load(@Nullable Dynamic<S> dynamic) {
    return DataResult.success(supplier.get());
  }

  @Override
  public <S> @NotNull DataResult<S> save(@NotNull T value, @NotNull DynamicOps<S> ops) {
    return DataResult.success(ops.empty());
  }
}
