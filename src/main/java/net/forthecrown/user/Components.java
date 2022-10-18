package net.forthecrown.user;

import com.google.common.collect.Iterators;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.user.data.*;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.property.PropertyMap;
import net.forthecrown.utils.io.JsonWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class which holds component types as constants.
 * <p>
 * A component is a piece of data which adds functionality
 * to the user class, to that end, adding and removing them
 * should be easily done, without much hassle. This is done
 * by Storing the main components here as constants, but also
 * allowing components to be created at any time.
 * <p>
 * Mostly this exists because I was getting tired of adding fields
 * to the user class everytime I wanted to add something, so
 * I implemented this to make the data stored in user objects
 * a bit more dynamic and maneuverable.
 * <p>
 * In the user object itself, the components are stored in a single
 * array, the way you identify which component is at which index
 * is with the {@link ComponentType#getIndex()} method, the return
 * result for that method will be created in {@link #create(String, Class)}
 * and is just an integer that is incremented everytime a new component
 * type is created/registered.
 * <p>
 * Component types also need to be able to create component instances,
 * this is done in {@link ComponentType#create(User)}. For this reason,
 * component types require that any {@link UserComponent} implementation
 * feature a public constructor with 2 parameters: {@link User} and
 * {@link ComponentType}. To ensure this is the case, {@link ComponentType}
 * will test if a constructor with those specifications exists, if it does
 * not, then it throws an exception
 * <p>
 * @see UserJsonSerializer#saveComponents(JsonWrapper, User) to see how components are serialized
 * @see UserJsonSerializer#loadComponents(JsonWrapper, User)  to see how components are deserialized
 */
public final class Components {
    private Components() {}

    /* ----------------------------- LOOKUPS ------------------------------ */

    /**
     * Name -> component type lookup.
     * Names can be specified manually but if {@link #of(Class)} is called,
     * then it is created automatically  with {@link #filterName(String)}.
     * <p>
     * This is also the map used to generate component indexes, this is
     * done by taking the size of this map, making it the index of a
     * type and then adding said type to the map, hence giving it a unique
     * ID and then incrementing it for the next potential component type
     */
    private static final Map<String, ComponentType> BY_NAME = new HashMap<>();

    /**
     * Index -> component type lookup.
     */
    private static final List<ComponentType> INDEX_LOOKUP  = new ArrayList<>();

    /**
     * Component type's user component class -> component type lookup.
     * <p>
     * Do you know how hard it is using the word type for both component types
     * and classes? I should stop doing it lol
     */
    private static final Map<Class, ComponentType> BY_TYPE = new HashMap<>();

    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * An immutable, empty component array.
     * <p>
     * This is the default value of the component array in the
     * {@link User} class to avoid it creating arrays when they
     * might not be necessary.
     */
    public static final UserComponent[] EMPTY_ARRAY = new UserComponent[0];

    /**
     * User UNIX time stamps.
     * @see UserTimeTracker
     */
    public static final ComponentType<UserTimeTracker>  TIME_TRACKER = create("timeStamps", UserTimeTracker.class);

    /**
     * Map of user properties.
     * @see PropertyMap
     * @see Properties
     */
    public static final ComponentType<PropertyMap>      PROPERTIES   = create("properties", PropertyMap.class);

    /**
     * List of homes the user has.
     * @see UserHomes
     */
    public static final ComponentType<UserHomes>        HOMES        = of(UserHomes.class);

    /**
     * The current title, tier and any available titles
     * a user may have
     * @see UserTitles
     * @see RankTitle
     * @see RankTier
     */
    public static final ComponentType<UserTitles>       TITLES       = create("rankData", UserTitles.class);

    /**
     * Various data about a user's interactions/connections with other
     * users, such as who a user might be married to, who they've blocked
     * and so forth.
     * @see UserInteractions
     */
    public static final ComponentType<UserInteractions> INTERACTIONS = of(UserInteractions.class);

    /**
     * A list of {@link MailMessage} a user may have received
     * @see UserMail
     * @see MailMessage
     */
    public static final ComponentType<UserMail>         MAIL         = of(UserMail.class);

    /**
     * Data for what active/available cosmetic effects a
     * user has
     * @see CosmeticData
     * @see net.forthecrown.cosmetics.Cosmetic
     * @see net.forthecrown.cosmetics.CosmeticType
     */
    public static final ComponentType<CosmeticData>     COSMETICS    = of(CosmeticData.class);

    /**
     * Data relating to a user's transaction in the Market
     * region.
     * <p>
     * I won't lie, this feels like a pretty useless component because
     * it serializes no data and just holds useless info.
     * @see UserMarketData
     * @see MarketManager
     * @see net.forthecrown.economy.market.MarketShop
     */
    public static final ComponentType<UserMarketData>   MARKET_DATA  = of(UserMarketData.class);

    /**
     * Data of how much a user has earned from what material in
     * the sell shop.
     * @see UserShopData
     */
    public static final ComponentType<UserShopData>     EARNINGS     = of(UserShopData.class);

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Empty initializer method to force
     * the class to load
     */
    static void init() {}

    /**
     * Creates a component type
     * <p>
     * This method will generate an ID for the returned component.
     *
     * @param name The name of the component type
     * @param typeClass The component's class
     * @param <T> The component's type
     * @return The created component.
     *
     * @throws IllegalStateException If either a naming conflict appeared, or the given
     *                               class was already registered
     */
    public static <T extends UserComponent> @NotNull ComponentType<T> create(String name,
                                                                             Class<T> typeClass
    ) throws IllegalStateException {
        // Test registration conflicts
        if (BY_NAME.containsKey(name)) {
            throw new IllegalStateException("Duplicate component names: '" + name + "'");
        }

        if (BY_TYPE.containsKey(typeClass)) {
            throw new IllegalStateException("Duplicate component classes: " + typeClass);
        }

        // Create type and register
        ComponentType<T> result = new ComponentType<>(BY_NAME.size(), name, typeClass);

        BY_NAME.put(name, result);
        INDEX_LOOKUP.add(result.getIndex(), result);
        BY_TYPE.put(typeClass, result);

        return result;
    }

    /**
     * Gets a matching component type for the given user component class, or
     * creates the component type.
     * <p>
     * This will automatically generate a {@link ComponentType#getSerialId()}
     * for the given class by calling {@link #filterName(String)}.
     * @param typeClass The class to find a component for
     * @param <T> The component's type
     * @return The found, or created, component.
     */
    public static <T extends UserComponent> @NotNull ComponentType<T> of(Class<T> typeClass) {
        var type = get(typeClass);

        if (type != null) {
            return type;
        }

        var name = filterName(typeClass.getSimpleName());
        return create(name, typeClass);
    }

    /**
     * Filter's the given initial name by removing any
     * "User" prefix and then making the first letter
     * lower case.
     *
     * @param initial The initial name to filter
     * @return The filtered name
     */
    private static String filterName(String initial) {
        initial = initial.replaceAll("User", "");
        return initial.substring(0, 1).toLowerCase() + initial.substring(1);
    }

    /**
     * Gets a component type by its name
     * @param name The name to look for
     * @return The component type with the given name, or null, if none was found
     */
    public static ComponentType get(String name) {
        return BY_NAME.get(name);
    }

    /**
     * Gets a component type by a given index
     * @param id The index to lookup
     * @return The component type at that index
     */
    public static ComponentType get(int id) {
        return INDEX_LOOKUP.get(id);
    }

    /**
     * Gets a component type by its component's class
     * @param typeClass The component class
     * @param <T> The component's type
     * @return The type that represents the component, or null, if none exists
     */
    public static <T extends UserComponent> @Nullable ComponentType<T> get(Class<T> typeClass) {
        return BY_TYPE.get(typeClass);
    }

    /**
     * Creates an unmodifiable iterator for
     * looping through all currently existing component
     * type values.
     * <p>
     * Used for component serialization in {@link UserJsonSerializer}
     * @return The created iterator.
     */
    public static Iterator<ComponentType> typeIterator() {
        return Iterators.unmodifiableIterator(BY_NAME.values().iterator());
    }
}