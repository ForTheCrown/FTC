package net.forthecrown.waypoint;

import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.types.SerializerParsers;
import net.forthecrown.waypoint.type.PlayerWaypointType;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.forthecrown.utils.io.types.SerializerParsers.BOOL;
import static net.forthecrown.utils.io.types.SerializerParsers.STRING;

public class WaypointProperties {
    /** Registry of waypoint properties */
    public static final Registry<WaypointProperty>
            REGISTRY = Registries.newFreezable();

    /**
     * Determines if a pole can be destroyed, if set to true, a waypoint
     * will also never be automatically deleted
     */
    public static final WaypointProperty<Boolean>
    INVULNERABLE    = new WaypointProperty<>("invulnerable", BOOL, false),

    /**
     * Determines if the waypoint can be visited by others without invitation,
     * only applies to named regions that wish to still require invitation to
     * visit.
     */
    PUBLIC          = new WaypointProperty<>("public", BOOL, true),

    /**
     * Only applies to named waypoints to determine whether they want to have
     * or disallow the dynmap marker
     */
    ALLOWS_MARKER   = new WaypointProperty<>("allowsMarker", BOOL, true),

    /**
     * Only applies to named regions, sets the waypoint's marker icon to be
     * the donator icon instead of the normal icon.
     */
    SPECIAL_MARKER  = new WaypointProperty<>("specialMarker", BOOL, false);

    /**
     * Property only used for region poles to determine whether they should
     * display their resident count on the pole.
     */
    public static final WaypointProperty<Boolean>
            HIDE_RESIDENTS = new WaypointProperty<>("hideResidents", BOOL, false)
    {
        @Override
        public void onValueUpdate(Waypoint waypoint,
                                  @Nullable Boolean oldValue,
                                  @Nullable Boolean value
        ) {
            if (waypoint.getType() == WaypointTypes.REGION_POLE
                    && waypoint.get(INVULNERABLE)
            ) {
                Waypoints.placePole(waypoint);
            }
        }
    };

    /**
     * The region's name, will be used on the dynmap, if they allow for the
     * marker, and can be used by other players to visit this region with
     * '/visit [name]'
     */
    public static final WaypointProperty<String>
            NAME = new WaypointProperty<>("name", STRING, null)
    {
        @Override
        public void onValueUpdate(Waypoint waypoint,
                                  @Nullable String oldValue,
                                  @Nullable String value
        ) {
            super.onValueUpdate(waypoint, oldValue, value);

            WaypointManager.getInstance()
                    .onRename(waypoint, oldValue, value);

            if (waypoint.getType() == WaypointTypes.REGION_POLE
                    && waypoint.get(INVULNERABLE)
            ) {
                Waypoints.placePole(waypoint);
            } else if (waypoint.getType() instanceof PlayerWaypointType) {
                Waypoints.setNameSign(
                        waypoint,
                        Waypoints.getEffectiveName(waypoint)
                );
            }
        }
    };

    /** The UUID of the player that owns the waypoint */
    public static final WaypointProperty<UUID>
            OWNER = new WaypointProperty<>("owner", SerializerParsers.UUID, null);

    /** The UUID of the guild that owns the waypoint */
    public static final WaypointProperty<UUID>
            GUILD_OWNER = new WaypointProperty<>("guildOwner", SerializerParsers.UUID, null) {
        @Override
        public void onValueUpdate(Waypoint waypoint,
                                  @Nullable UUID oldValue,
                                  @Nullable UUID value
        ) {
            super.onValueUpdate(waypoint, oldValue, value);

            Waypoints.setNameSign(
                    waypoint,
                    Waypoints.getEffectiveName(waypoint)
            );
        }
    };

    @OnEnable
    private static void init() {
        REGISTRY.freeze();
    }
}