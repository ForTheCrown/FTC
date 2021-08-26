package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

/**
 * Class which stores ComVarType constants for easy access.
 */
public interface ComVarTypes {
    ComVarType<Short>       SHORT =         new PrimitiveComVarType<>(Short.class,          r -> (short) r.readInt(),       JsonPrimitive::new,     JsonElement::getAsShort);
    ComVarType<Byte>        BYTE =          new PrimitiveComVarType<>(Byte.class,           r -> (byte) r.readInt(),        JsonPrimitive::new,     JsonElement::getAsByte);

    ComVarType<Long>        LONG =          new PrimitiveComVarType<>(Long.class,           StringReader::readLong,         JsonPrimitive::new,     JsonElement::getAsLong);
    ComVarType<Double>      DOUBLE =        new PrimitiveComVarType<>(Double.class,         StringReader::readDouble,       JsonPrimitive::new,     JsonElement::getAsDouble);
    ComVarType<Float>       FLOAT =         new PrimitiveComVarType<>(Float.class,          StringReader::readFloat,        JsonPrimitive::new,     JsonElement::getAsFloat);
    ComVarType<Integer>     INTEGER =       new PrimitiveComVarType<>(Integer.class,        StringReader::readInt,          JsonPrimitive::new,     JsonElement::getAsInt);
    ComVarType<Boolean>     BOOLEAN =       new PrimitiveComVarType<>(Boolean.class,        StringReader::readBoolean,      JsonPrimitive::new,     JsonElement::getAsBoolean);
    ComVarType<String>      STRING =        new PrimitiveComVarType<>(String.class,         StringReader::getRemaining,     JsonPrimitive::new,     JsonElement::getAsString);
    ComVarType<Character>   CHAR =          new PrimitiveComVarType<>(Character.class,      StringReader::read,             JsonPrimitive::new,     JsonElement::getAsCharacter);

    ComVarType<World>       WORLD =         new WorldComVarType();
    ComVarType<Key>         KEY =           new KeyComVarType();
    ComVarType<Component>   COMPONENT =     new ComponentComVarType();
}
