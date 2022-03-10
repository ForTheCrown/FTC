package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

/**
 * Class which stores ComVarType constants for easy access.
 */
public interface VarTypes {
    VarType<Short>      SHORT       = new PrimitiveVarType<>(Short.class, r -> (short) r.readInt(),       JsonPrimitive::new,     JsonElement::getAsShort);
    VarType<Byte>       BYTE        = new PrimitiveVarType<>(Byte.class, r -> (byte) r.readInt(),        JsonPrimitive::new,     JsonElement::getAsByte);

    VarType<Long>       LONG        = new PrimitiveVarType<>(Long.class,           StringReader::readLong,         JsonPrimitive::new,     JsonElement::getAsLong);
    VarType<Double>     DOUBLE      = new PrimitiveVarType<>(Double.class,         StringReader::readDouble,       JsonPrimitive::new,     JsonElement::getAsDouble);
    VarType<Float>      FLOAT       = new PrimitiveVarType<>(Float.class,          StringReader::readFloat,        JsonPrimitive::new,     JsonElement::getAsFloat);
    VarType<Integer>    INT         = new PrimitiveVarType<>(Integer.class,        StringReader::readInt,          JsonPrimitive::new,     JsonElement::getAsInt);
    VarType<Boolean>    BOOL        = new PrimitiveVarType<>(Boolean.class,        StringReader::readBoolean,      JsonPrimitive::new,     JsonElement::getAsBoolean, PrimitiveVarType.fromType(BoolArgumentType.bool()));
    VarType<String>     STRING      = new PrimitiveVarType<>(String.class,         StringReader::getRemaining,     JsonPrimitive::new,     JsonElement::getAsString);
    VarType<Character>  CHAR        = new PrimitiveVarType<>(Character.class,      StringReader::read,             JsonPrimitive::new,     JsonElement::getAsCharacter);

    VarType<World>      WORLD       = new WorldVarType();
    VarType<Key>        KEY         = new KeyVarType();
    VarType<Component>  COMPONENT   = new ComponentVarType();
    VarType<Long>       TIME        = new TimeIntervalVarType();

    static void init() {}
}