package net.forthecrown.user;

import lombok.Getter;

import java.lang.reflect.Constructor;

/**
 * A type representing a {@link UserComponent}.
 * @param <T> The user component implementation class this
 *            type represents
 * @see Components
 */
@Getter
public class ComponentType<T extends UserComponent> {
    /**
     * The parameters a {@link UserComponent} implementation needs to be
     * automatically constructed
     */
    private static final Class<?>[] PARAMS = { User.class, ComponentType.class };

    /**
     * The type's serialized JSON key
     */
    private final String serialId;

    /**
     * The type's runtime array index used to speed
     * up lookups in the {@link User} class
     */
    private final int index;

    /**
     * The class of the user component implementation
     */
    private final Class<T> typeClass;

    ComponentType(int index, String serialId, Class<T> typeClass) {
        this.index = index;
        this.serialId = serialId;
        this.typeClass = typeClass;

        // Get the constructor, this will throw an exception
        // if the constructor doesn't exist, thus validating
        // that the class is valid B)
        getConstructor(getTypeClass());
    }

    /**
     * Gets the constructor used to instantiate user component
     * objects.
     * @param typeClass The component class
     * @param <T> The component class
     * @return The gotten constructor.
     * @throws IllegalArgumentException If a valid constructor couldn't be found
     */
    private static <T extends UserComponent> Constructor<T> getConstructor(Class<T> typeClass) throws IllegalArgumentException {
        try {
            var constructor = typeClass.getDeclaredConstructor(PARAMS);
            constructor.setAccessible(true);

            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    String.format("'%s' is missing a (%s, %s) constructor required for automatic class instantiation",
                            typeClass.getSimpleName(),
                            PARAMS[0],
                            PARAMS[1]
                    )
            );
        }
    }

    /**
     * Creates a new component instance for the given user
     * @param user The user to create the component for
     * @return The created component
     * @throws IllegalArgumentException If this component type's component class
     *                                  does not have a valid constructor
     * @throws IllegalStateException If there was error with calling the component
     *                               class' constructor
     */
    public T create(User user) throws IllegalArgumentException, IllegalStateException {
        var constructor = getConstructor(getTypeClass());

        try {
            return constructor.newInstance(user, this);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}