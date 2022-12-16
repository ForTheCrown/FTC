package net.forthecrown.waypoint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.forthecrown.utils.io.types.SerializerParser;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WaypointProperty<T> {
    @EqualsAndHashCode.Include
    private final String name;

    @EqualsAndHashCode.Include
    private final int id;

    private final SerializerParser<T> serializer;
    private final T defaultValue;

    public WaypointProperty(String name,
                            SerializerParser<T> serializer,
                            T defaultValue
    ) {
        this.serializer = Objects.requireNonNull(serializer);
        this.defaultValue = defaultValue;

        Objects.requireNonNull(name, "Name cannot be null");

        var holder = WaypointProperties.REGISTRY
                .register(name, this);

        this.id = holder.getId();
        this.name = holder.getKey();
    }

    public void onValueUpdate(Waypoint waypoint,
                              @Nullable T oldValue,
                              @Nullable T value
    ) {
        // This method is overriden by several instances of this class,
        // but this was the most used function call I made in it lol
        Waypoints.updateDynmap(waypoint);
    }
}