package net.forthecrown.emperor.comvars;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.comvars.types.ComVarType;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The CommandVariables class, manages and handles the CommandVariables
 */
public class ComVars {
    private static final Map<String, ComVar<?>> COM_VARS = new HashMap<>();

    public static <T> ComVar<T> set(@NotNull String name, @NotNull ComVarType<T> type, T value){
        validate(name, type);

        boolean alreadyExists = COM_VARS.containsKey(name);
        if(alreadyExists && !COM_VARS.get(name).getType().equals(type)) throw new IllegalArgumentException("Mismatch between provided var type and already existing var type");

        ComVar<T> entry = alreadyExists ? ((ComVar<T>) COM_VARS.get(name)).update(value) : new ComVar<>(type, name, value);
        return setRaw(name, entry);
    }

    public static <T> void parseVar(String name, String input) throws CommandSyntaxException {
        ComVar<T> type = (ComVar<T>) COM_VARS.get(name);
        T value = type.getType().fromString(new StringReader(input));
        set(name, type.getType(), value);
    }

    public static <T> String getString(@NotNull String name){
        Validate.notNull(name, "Name was null");;
        if(!COM_VARS.containsKey(name)) throw new NullPointerException("No variable by the name of: " + name + " found");

        ComVar<T> var = (ComVar<T>) COM_VARS.get(name);
        ComVarType<T> type= var.getType();
        return type.asString(var.getValue());
    }

    public static <T> T get(@NotNull String name, @NotNull ComVarType<T> type){
        return getRaw(name, type).getValue();
    }

    public static <T> ComVar<T> setRaw(String name, ComVar<T> variable){
        Validate.notNull(variable, "Variable was null");
        Validate.notNull(name, "Name was null");

        COM_VARS.put(name, variable);
        return variable;
    }

    public static <T> ComVar<T> getRaw(@NotNull String name, @NotNull ComVarType<T> type){
        validate(name, type);
        if(!COM_VARS.containsKey(name)) throw new NullPointerException("No variable by the name of: " + name + " found");
        if(!COM_VARS.get(name).getType().equals(type)) throw new IllegalArgumentException("Provided type does not match stored type");

        return (ComVar<T>) COM_VARS.get(name);
    }

    public static ComVar<?> getVar(String name){
        return COM_VARS.get(name);
    }

    public static ComVarType<?> getType(@NotNull String name){
        Validate.notNull(name, "Name was null");
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

    public void remove(@NotNull String name){
        Validate.notNull(name, "Name was null");
        COM_VARS.remove(name);
    }

    private static void validate(String name, ComVarType<?> type){
        Validate.notNull(name, "Name was null");
        Validate.notNull(type, "Type was null");
    }
}
