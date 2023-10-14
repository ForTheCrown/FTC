package net.forthecrown.waypoints;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class WaypointProperty<T> {

  @EqualsAndHashCode.Include
  private final String name;

  @EqualsAndHashCode.Include
  private final int id;

  private final ArgumentType<T> parser;
  private final Codec<T> codec;

  private final T defaultValue;

  @Setter
  @Accessors(chain = true)
  private boolean updatesMarker = true;

  @Setter
  @Accessors(chain = true)
  private UpdateCallback<T> callback;

  @Setter
  @Accessors(chain = true)
  private Validator<T> validator;

  public WaypointProperty(
      String name,
      ArgumentType<T> parser,
      Codec<T> codec,
      T defaultValue
  ) {
    this.parser = Objects.requireNonNull(parser);
    this.codec = Objects.requireNonNull(codec);

    this.defaultValue = defaultValue;

    Objects.requireNonNull(name, "Name cannot be null");

    var holder = WaypointProperties.REGISTRY.register(name, this);

    this.id = holder.getId();
    this.name = holder.getKey();
  }

  public void validateValue(Waypoint waypoint, T value) throws CommandSyntaxException {
    if (this.validator == null) {
      return;
    }

    validator.validateValue(waypoint, value);
  }

  public void onValueUpdate(Waypoint waypoint, @Nullable T oldValue, @Nullable T value) {
    if (updatesMarker) {
      Waypoints.updateDynmap(waypoint);
    }

    if (callback != null) {
      callback.onValueUpdate(waypoint, oldValue, value);
    }
  }

  public interface Validator<T> {

    void validateValue(Waypoint waypoint, T newValue) throws CommandSyntaxException;
  }

  public interface UpdateCallback<T> {

    void onValueUpdate(Waypoint waypoint, @Nullable T oldValue, @Nullable T value);
  }
}