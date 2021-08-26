package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.comvars.ParseFunction;
import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

class PrimitiveComVarType<T> implements ComVarType<T> {
    private final ParseFunction<T> fromString;
    private final Function<T, JsonElement> serializationFunc;
    private final Function<JsonElement, T> deserializationFunc;
    private final Class<T> clazz;
    private final Key key;

    PrimitiveComVarType(Class<T> clazz, ParseFunction<T> fromString, Function<T, JsonElement> json, Function<JsonElement, T> func) {
        this.fromString = fromString;
        this.clazz = clazz;
        this.serializationFunc = json;
        deserializationFunc = func;
        key = Crown.coreKey(clazz.getSimpleName().toLowerCase() + "_type");

        Registries.COMVAR_TYPES.register(key, this);
    }

    private static CommandSyntaxException mismatchException(String className, String input) {
        return FtcExceptionProvider.createWithContext("Mismatch between input and variable type. Var is: " + className, input, 0);
    }

    @Override
    public T parse(StringReader input) throws CommandSyntaxException {
        return fromString.parse(input);
    }

    @Override
    public String asParsableString(T value) {
        return value == null ? "null" : value.toString();
    }

    @Override
    public JsonElement serialize(@Nullable T value) {
        return value == null ? JsonNull.INSTANCE : serializationFunc.apply(value);
    }

    @Override
    public T deserialize(JsonElement element) {
        return deserializationFunc.apply(element);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveComVarType<?> type = (PrimitiveComVarType<?>) o;
        return clazz.equals(type.clazz);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(clazz)
                .toHashCode();
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
