package net.forthecrown.core.registry;

import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.inventory.ExtendedItemType;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.property.PropertyMap;
import net.forthecrown.user.property.UserProperty;
import org.bukkit.util.CachedServerIcon;

/**
 * Class that provides registry constants for some features of
 * FTC and also provides factory methods.
 * <p>
 * Use {@link #newRegistry()} and {@link #newFreezable()} to
 * create registries. {@link #ofEnum(Class)} will create a
 * registry of all enum constants in a given class and try
 * to either find a key from the enums, if they implemement
 * {@link FtcKeyed} or simply use enum's name in lowercase
 * form
 *
 * @see #newRegistry()
 * @see #newFreezable()
 * @see #ofEnum(Class)
 * @see Registry
 */
public interface Registries {
    // --- CONSTANTS ---

    /** Root registry that stores all other registry constants */
    Registry<Registry>          ROOT            = newRegistry();

    /**
     * User properties registry
     * @see Properties
     * @see PropertyMap
     */
    Registry<UserProperty>      USER_PROPERTIES = newFreezable("user_properties");

    /**
     * Jail cell registry
     * @see net.forthecrown.core.admin.Punishments
     * @see net.forthecrown.core.admin.PunishType
     */
    Registry<JailCell>          JAILS           = newRegistry("jails");

    /**
     * Registry of cosmetic types, each type holds its own
     * registry for its cown cosmetic values
     * @see CosmeticType
     * @see net.forthecrown.cosmetics.Cosmetics
     */
    Registry<CosmeticType>      COSMETIC        = newFreezable("cosmetic_type");

    /**
     * Registry of currently existing dungeon bosses
     * @see net.forthecrown.dungeons.boss.DungeonBoss
     * @see net.forthecrown.dungeons.Bosses
     */
    Registry<KeyedBoss>         DUNGEON_BOSSES  = newFreezable("dungeon_bosses");

    /**
     * Registry of all intractable NPCs
     */
    Registry<SimpleNpc>         NPCS            = newRegistry("npcs");

    /** Server icons */
    Registry<CachedServerIcon>  SERVER_ICONS    = newFreezable("server_icons");

    /** Types of special items */
    Registry<ExtendedItemType>  ITEM_TYPES      = newFreezable("item_types");

    // Usables
    Registry<UsageType<? extends UsageAction>>  USAGE_ACTIONS   = newFreezable("usage_actions");
    Registry<UsageType<? extends UsageTest>>    USAGE_CHECKS    = newFreezable("usage_tests");

    // --- STATIC FUNCTIONS ---

    private static <V> Registry<V> newRegistry(String name) {
        return registerRoot(name, newRegistry());
    }

    private static <V> Registry<V> newFreezable(String name) {
        return registerRoot(name, newFreezable());
    }

    private static <V> Registry<V> registerRoot(String name, Registry<V> reg) {
        return ROOT.register(name, reg).getValue();
    }

    /**
     * Creates a registry which CANNOT be frozen.
     * <p>
     * Calling {@link Registry#freeze()} on a
     * registry created with this method will
     * throw an exception
     *
     * @param <V> The registry's type
     * @return The created registry
     * @see Registry#freeze()
     */
    static <V> Registry<V> newRegistry() {
        return new Registry<>(false);
    }

    /**
     * Creates a registry which can be frozen.
     * <p>
     * Calling {@link Registry#freeze()} won't
     * throw any errors
     *
     * @param <V> The registry's type
     * @return The created registry
     * @see Registry#freeze()
     */
    static <V> Registry<V> newFreezable() {
        return new Registry<>(true);
    }

    /**
     * Creates a frozen registry out of all enum constants in the
     * given class.
     * <p>
     * This will loop through each enum constant and test if its
     * an instance of {@link FtcKeyed}, if it is, it uses the result
     * of {@link FtcKeyed#getKey()} as the enum's key, else it just
     * uses {@link Enum#name()}. Each enum is also registered with
     * its {@link Enum#ordinal()} as its ID
     *
     * @param enumClass The class to turn into a registry
     * @param <E> The enum's type
     * @return The created frozen registry
     */
    static <E extends Enum<E>> Registry<E> ofEnum(Class<E> enumClass) {
        Registry<E> registry = newFreezable();

        for (var e: enumClass.getEnumConstants()) {
            String key;

            if (e instanceof FtcKeyed keyed) {
                key = keyed.getKey();
            } else {
                key = e.name().toLowerCase();
            }

            registry.register(key, e, e.ordinal());
        }

        registry.freeze();
        return registry;
    }
}