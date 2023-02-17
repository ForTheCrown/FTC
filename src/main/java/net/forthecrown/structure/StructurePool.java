package net.forthecrown.structure;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.utils.io.JsonUtils;
import org.apache.logging.log4j.Logger;

public class StructurePool {

  private static final Logger LOGGER = Loggers.getLogger();

  public static StructurePool EMPTY
      = new StructurePool(Collections.emptyList());

  @Getter
  private final ImmutableList<String> structureNames;

  public StructurePool(List<String> structureNames) {
    Objects.requireNonNull(structureNames);

    this.structureNames = structureNames.isEmpty()
        ? ImmutableList.of()
        : ImmutableList.copyOf(structureNames);
  }

  public Stream<BlockStructure> getStructures() {
    var structs = Structures.get();

    return structureNames.stream()
        .map(s -> structs.getRegistry().get(s))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  public static StructurePool load(JsonArray array) {
    return new StructurePool(
        JsonUtils.stream(array)
            .map(JsonElement::getAsString)
            .filter(s -> {
              if (!Keys.isValidKey(s)) {
                LOGGER.warn("Invalid key for pool element: '{}'", s);
                return false;
              }

              return true;
            })
            .toList()
    );
  }
}