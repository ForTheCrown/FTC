package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.UUID;
import java.util.stream.IntStream;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.TagOps;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.NotNull;

public class UUIDSerializerParser implements SerializerParser<UUID> {

  @Override
  public @NotNull String asString(@NotNull UUID value) {
    return value.toString();
  }

  @Override
  public @NotNull ArgumentType<UUID> getArgumentType() {
    return ArgumentTypes.uuid();
  }

  @Override
  public <V> @NotNull V serialize(@NotNull DynamicOps<V> ops, @NotNull UUID value) {
    if (ops instanceof NbtOps || ops instanceof TagOps) {
      return ops.createIntList(
          IntStream.of(UUIDUtil.uuidToIntArray(value))
      );
    }

    return ops.createString(value.toString());
  }

  @Override
  public <V> @NotNull DataResult<UUID> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
    var intList = ops.getIntStream(element);

    if (intList.error().isEmpty()) {
      return intList.map(stream -> UUIDUtil.uuidFromIntArray(stream.toArray()));
    }

    return ops.getStringValue(element)
        .flatMap(s -> {
          try {
            return DataResult.success(UUID.fromString(s));
          } catch (IllegalArgumentException e) {
            return Results.error(
                "Invalid UUID: '%s' error: %s",
                s, e.getMessage()
            );
          }
        });
  }
}