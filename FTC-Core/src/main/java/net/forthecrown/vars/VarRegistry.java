package net.forthecrown.vars;

import com.google.gson.JsonElement;
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
 * The CommandVariables class, manages and handles the CommandVariables
 */
public class VarRegistry {
    private static final Logger LOGGER = Crown.logger();

    public static final Pattern ALLOWED_NAME = Pattern.compile("^[a-z_]\\w*$");
    private static final Object2ObjectMap<String, Var> COM_VARS = new Object2ObjectOpenHashMap<>();

    static final Var<Boolean> SERIALIZE_UNSUSED = getSafe("vars_saveUnused", VarTypes.BOOL, false);

    public static <T> Var<T> set(@NotNull String name, @NotNull VarType<T> type, T value) {
        validate(name, type);

        Var<T> entry = COM_VARS.get(name);
        boolean alreadyExists = entry != null;

        if(alreadyExists && entry.getType() != type) {
            throw new IllegalArgumentException("Mismatch between provided var type and already existing var type for " + name);
        }

        entry = alreadyExists ? entry.update(value) : type.createVar(name, value);
        entry.used = true;
        COM_VARS.put(name, entry);

        return entry;
    }

    public static <T> Var<T> set(@NotNull String name, @NotNull VarType<T> type, JsonElement e) {
        return set(name, type, type.deserialize(e));
    }

    public static <T> T get(@NotNull String name, @NotNull VarType<T> type) {
        validate(name, type);
        Var<T> var = COM_VARS.get(name);
        if(var == null) return null;

        Validate.isTrue(var.getType() == type, "Given type '%s' for %s did not match existing type: '%s'",
                type.key(),
                name,
                var.getType().key()
        );

        return var.get();
    }

    public static Var getVar(String name) {
        return COM_VARS.get(name);
    }

    public static VarType getType(@NotNull String name) {
        Validate.notNull(name, "Name was null");
        validateName(name);
        return COM_VARS.get(name).getType();
    }

    public static int size() {
        return COM_VARS.size();
    }

    public static boolean contains(String name) {
        return COM_VARS.containsKey(name);
    }

    public static void remove(@NotNull String name) {
        Validate.notNull(name, "Name was null");
        COM_VARS.remove(name);
    }

    public static void save() {
        VarSerializer.save(COM_VARS.values());
    }

    public static void load() {
        VarSerializer.load(COM_VARS);
    }

    public static <T> Var<T> getSafe(String name, VarType<T> type, T defValue) {
        validate(name, type);

        Var<T> var = COM_VARS.get(name);

        if(var != null) {
            Validate.isTrue(var.getType() == type, "Existing type and given type did not match for comvar '" + name + "'");
            var.setDefaultValue(defValue);
        } else {
            var = type.createVar(name, defValue)
                    .setDefaultValue(defValue);

            COM_VARS.put(name, var);
        }

        var.used = true;
        LOGGER.info("getSafe called: name: '{}', type: '{}', defVal: {}", name, type.key().asString(), type.asParsableString(defValue));
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
