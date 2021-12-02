package net.forthecrown.utils;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Properties;
import java.util.function.Function;

/**
 * I thought I was gonna be doing something a bit complex with Properties files.
 * <p>Turns out no, so now this just exists :I</p>
 */
public class PropertiesWrapper extends Properties {
    public PropertiesWrapper() {
    }

    public PropertiesWrapper(int initialCapacity) {
        super(initialCapacity);
    }

    public PropertiesWrapper(Properties defaults) {
        super(defaults);
    }

    public <T> T parse(String name, Function<String, T> parser) { return parse(name, parser, null); }
    public <T> T parse(String name, Function<String, T> parser, T def) {
        if(!containsKey(name)) return def;

        String val = getProperty(name);
        return parser.apply(val);
    }

    public void add(String name, Object obj) {
        setProperty(name, obj.toString());
    }

    public boolean getBoolean(String name) { return getBoolean(name, false); }
    public boolean getBoolean(String name, boolean def) {
        return parse(name, BooleanUtils::toBoolean, def);
    }

    public byte getByte(String name) { return getByte(name, (byte) 0); }
    public byte getByte(String name, byte def) {
        return parse(name, s -> NumberUtils.toByte(s, def), def);
    }

    public short getShort(String name) { return getShort(name, (short) 0); }
    public short getShort(String name, short def) {
        return parse(name, s -> NumberUtils.toShort(s, def), def);
    }

    public int getInt(String name) { return getInt(name, 0); }
    public int getInt(String name, int def) {
        return parse(name, s -> NumberUtils.toInt(s, def), def);
    }

    public float getFloat(String name) { return getFloat(name, 0F); }
    public float getFloat(String name, float def) {
        return parse(name, s -> NumberUtils.toFloat(s, def), def);
    }

    public double getDouble(String name) { return getDouble(name, 0D); }
    public double getDouble(String name, double def) {
        return parse(name, s -> NumberUtils.toDouble(s, def), def);
    }

    public long getLong(String name) { return getLong(name, 0L); }
    public long getLong(String name, long def) {
        return parse(name, s -> NumberUtils.toLong(s, def), def);
    }

    public char getChar(String name, char def) {
        return parse(name, s -> CharUtils.toChar(s, def), def);
    }
}
