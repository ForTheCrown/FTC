package net.forthecrown.comvars;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.forthecrown.comvars.types.ComVarType;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * The CommandVariables class, manages and handles the CommandVariables
 */
public class ComVarRegistry {
    public static final Pattern ALLOWED_NAME = Pattern.compile("^[a-z_]\\w*$");
    private static final Object2ObjectMap<String, ComVar> COM_VARS = new Object2ObjectOpenHashMap<>();

    public static <T> ComVar<T> set(@NotNull String name, @NotNull ComVarType<T> type, T value){
        validate(name, type);

        ComVar<T> entry = COM_VARS.get(name);
        boolean alreadyExists = entry != null;
        if(alreadyExists && entry.getType() != type) throw new IllegalArgumentException("Mismatch between provided var type and already existing var type");

        entry = alreadyExists ? COM_VARS.get(name).update(value) : new ComVar<>(type, name, value);
        return setVar(entry);
    }

    public static <T> ComVar<T> set(@NotNull String name, @NotNull ComVarType<T> type, JsonElement e) {
        return set(name, type, type.deserialize(e));
    }

    public static JsonElement serialize(String name) {
        return getVar(name).serialize();
    }

    public static String getString(@NotNull String name){
        Validate.notNull(name, "Name was null");;
        validateName(name);
        if(!COM_VARS.containsKey(name)) throw new NullPointerException("No variable by the name of: " + name + " found");

        ComVar var = COM_VARS.get(name);
        return var.toString();
    }

    public static <T> T get(@NotNull String name, @NotNull ComVarType<T> type){
        return getVar(name, type).getValue();
    }

    public static <T> ComVar<T> setVar(ComVar<T> variable) {
        Validate.notNull(variable, "Variable was null");

        COM_VARS.put(variable.getName(), variable);
        return variable;
    }

    public static <T> ComVar<T> getVar(@NotNull String name, @NotNull ComVarType<T> type){
        validate(name, type);
        if(!COM_VARS.containsKey(name)) throw new NullPointerException("No variable by the name of: " + name + " found");
        if(!COM_VARS.get(name).getType().equals(type)) throw new IllegalArgumentException("Provided type does not match stored type");

        return (ComVar<T>) COM_VARS.get(name);
    }

    public static ComVar getVar(String name){
        validateName(name);
        return COM_VARS.get(name);
    }

    public static ComVarType getType(@NotNull String name){
        Validate.notNull(name, "Name was null");
        validateName(name);
        return COM_VARS.get(name).getType();
    }

    public static Set<String> getVariables(){
        return COM_VARS.keySet();
    }

    public static int size(){
        return COM_VARS.size();
    }

    public static boolean contains(String name){
        return COM_VARS.containsKey(name);
    }

    public static void remove(@NotNull String name){
        Validate.notNull(name, "Name was null");
        COM_VARS.remove(name);
    }

    private static void validate(String name, ComVarType<?> type){
        Validate.notNull(name, "Name was null");
        validateName(name);

        Validate.notNull(type, "Type was null");
    }

    /**
     * Checks if a name is a valid variable name
     * <p></p>
     * Fuck you, these need the same syntax as normal java variables.
     * <p>Love you &lt;3</p>
     * @param name The name to check
     */
    public static void validateName(String name) {
        Validate.isTrue(ALLOWED_NAME.matcher(name).matches(), "Illegal variable name, use java variable naming convention (Lowercase first letter and no irregular characters)");
    }

    public static ObjectCollection<ComVar> getValues() {
        return COM_VARS.values();
    }
}