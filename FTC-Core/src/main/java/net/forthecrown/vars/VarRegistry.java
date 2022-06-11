package net.forthecrown.vars;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.forthecrown.core.Crown;
import net.forthecrown.vars.types.VarType;
import net.forthecrown.vars.types.VarTypes;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Stores and manages Vars
 */
public class VarRegistry {
    private static final Logger LOGGER = Crown.logger();

    public static final Pattern ALLOWED_NAME = Pattern.compile("^[a-z_]\\w*$");
    private static final Object2ObjectMap<String, Var> COM_VARS = new Object2ObjectOpenHashMap<>();

    static final Var<Boolean> SERIALIZE_UNUSED = def("vars_saveUnused", VarTypes.BOOL, false);

    /**
     * Gets a plain var by the given name
     * @param name The name of the var
     * @return The gotten var, null, if no var by the given name was found
     */
    public static Var getVar(String name) {
        return COM_VARS.get(name);
    }

    /**
     * Gets the amount of variables
     * @return The variable count
     */
    public static int size() {
        return COM_VARS.size();
    }

    /**
     * Checks if a variable with the given name exists
     * @param name The var's name
     * @return If the var exists, false otherwise
     */
    public static boolean contains(String name) {
        return COM_VARS.containsKey(name);
    }

    /**
     * Removes The var with the given name
     * @param name The name of the var to remove
     */
    public static void remove(@NotNull String name) {
        Validate.notNull(name, "Name was null");
        COM_VARS.remove(name);
    }

    /**
     * Saves all variables
     */
    public static void save() {
        VarSerializer.save(COM_VARS.values());
    }

    /**
     * Loads all variables
     */
    public static void load() {
        VarSerializer.load(COM_VARS);
    }

    /**
     * Defines a variable, will just return an already existing var if
     * @param name The name of the var
     * @param type The type of the var
     * @param defValue The default value of the var
     * @param <T> The var's type
     * @return The defined var
     */
    public static <T> Var<T> def(String name, VarType<T> type, T defValue) {
        validate(name, type);

        Var<T> var = COM_VARS.get(name);

        // If var null -> it doesn't exist, if it does exist,
        // check given type against var type
        if(var != null) {
            Validate.isTrue(var.getType() == type, "Existing type and given type did not match for comvar '" + name + "'");
            var.setDefaultValue(defValue);
        } else {
            var = type.createVar(name, defValue)
                    .setDefaultValue(defValue);

            COM_VARS.put(name, var);
        }

        var.used = true;
        LOGGER.info("Defined var '{}', type: '{}', defVal: {}", name, type.key().asString(), type.asParsableString(defValue));
        return var;
    }

    private static void validate(String name, VarType<?> type){
        Validate.notNull(name, "Name was null");
        validateName(name);

        Validate.notNull(type, "Type was null");
    }

    public static boolean isValidName(String name) {
        return ALLOWED_NAME.matcher(name).matches();
    }

    /**
     * Checks if a name is a valid variable name
     * @param name The name to check
     */
    public static void validateName(String name) {
        Validate.isTrue(ALLOWED_NAME.matcher(name).matches(), "Illegal variable name, use java variable naming convention (Lowercase first letter and no irregular characters)");
    }

    public static ObjectCollection<Var> getValues() {
        return COM_VARS.values();
    }
}