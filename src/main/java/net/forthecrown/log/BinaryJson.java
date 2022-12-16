package net.forthecrown.log;

import com.google.gson.*;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A utility class to turn JSON into raw binary with the intention of saving
 * space.
 * <p>
 * The format this class utilizes is a simple one. The first byte of a JSON
 * element is its type. Types are stored as constants below. In the case of some
 * types, no further data is written, this is the case with {@link #TYPE_NULL},
 * {@link #TYPE_TRUE}, {@link #TYPE_FALSE}, {@link #TYPE_EMPTY_ARRAY},
 * {@link #TYPE_EMPTY_OBJECT}. In other cases however, further data will need to
 * be written.
 * <p>
 * For numbers the data that's written will be in the form of whatever the
 * smallest unit is that can store that number, for example: The smallest unit
 * that can contain the number 15 is a <code>byte</code>, so even if it's a
 * <code>long</code> in the element, it's written as a <code>byte</code>.
 * <p>
 * Arrays and objects follow both have a very simple header written, their size
 * in integer form. After that however, they differ. Objects are written as an
 * <code>n</code> number of UTF-8 and JSON element pairs, while arrays are just
 * a list of JSON elements.
 */
public @UtilityClass class BinaryJson {
    /* --------------------------- TYPE CONSTANTS --------------------------- */

    public final byte
            // Null
            TYPE_NULL           = 0,

            // Container types
            TYPE_OBJECT         = 1,
            TYPE_EMPTY_OBJECT   = 2,
            TYPE_ARRAY          = 3,
            TYPE_EMPTY_ARRAY    = 4,

            // String
            TYPE_STRING         = 5,

            // Booleans
            TYPE_FALSE          = 6,
            TYPE_TRUE           = 7,

            // Numbers
            TYPE_BYTE           = 8,
            TYPE_SHORT          = 9,
            TYPE_INT            = 10,
            TYPE_LONG           = 11,
            TYPE_FLOAT          = 12,
            TYPE_DOUBLE         = 13,

            // Special numbers
            TYPE_BIG_DECIMAL    = 14,
            TYPE_BIG_INTEGER    = 15;

    private static final JsonPrimitive
            TRUE = new JsonPrimitive(true),
            FALSE = new JsonPrimitive(false);

    /* ------------------------------ READING ------------------------------- */

    public JsonElement read(Path path) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }

        try (var stream = Files.newInputStream(path)) {
            DataInputStream inputStream = new DataInputStream(stream);
            var element = read(inputStream);

            inputStream.close();
            stream.close();

            return element;
        }
    }

    public JsonElement read(DataInput input) throws IOException {
        byte type = input.readByte();

        return switch (type) {
            // Null
            case TYPE_NULL          -> JsonNull.INSTANCE;

            // Boolean values
            case TYPE_FALSE         -> FALSE;
            case TYPE_TRUE          -> TRUE;

            // Empties
            case TYPE_EMPTY_ARRAY   -> new JsonArray();
            case TYPE_EMPTY_OBJECT  -> new JsonObject();

            // Numbers
            case TYPE_BYTE          -> new JsonPrimitive(input.readByte());
            case TYPE_SHORT         -> new JsonPrimitive(input.readShort());
            case TYPE_INT           -> new JsonPrimitive(input.readInt());
            case TYPE_LONG          -> new JsonPrimitive(input.readLong());
            case TYPE_FLOAT         -> new JsonPrimitive(input.readFloat());
            case TYPE_DOUBLE        -> new JsonPrimitive(input.readDouble());

            // String
            case TYPE_STRING        -> new JsonPrimitive(input.readUTF());

            // Containers
            case TYPE_ARRAY         -> readArray(input);
            case TYPE_OBJECT        -> readObject(input);

            // Complex number types
            case TYPE_BIG_DECIMAL   -> readBigDecimal(input);
            case TYPE_BIG_INTEGER   -> readBigInt(input);

            // Unknown type
            default                 -> throw new IOException("Unknown type: " + type);
        };
    }

    private JsonElement readBigInt(DataInput input) throws IOException {
        int size = input.readInt();
        byte[] array = new byte[size];
        input.readFully(array);

        return new JsonPrimitive(
                new BigInteger(array)
        );
    }

    private JsonElement readBigDecimal(DataInput input) throws IOException {
        String dec = input.readUTF();
        return new JsonPrimitive(
                new BigDecimal(dec)
        );
    }

    private JsonArray readArray(DataInput input) throws IOException {
        int size = input.readInt();
        JsonArray array = new JsonArray(size);

        for (int i = 0; i < size; i++) {
            array.add(read(input));
        }

        return array;
    }

    private JsonObject readObject(DataInput input) throws IOException {
        int size = input.readInt();
        var result = new JsonObject();

        for (int i = 0; i < size; i++) {
            String key = input.readUTF();
            JsonElement element = read(input);

            result.add(key, element);
        }

        return result;
    }

    /* ------------------------------ WRITING ------------------------------- */

    public void write(JsonElement element, Path path) throws IOException {
        try (var stream = Files.newOutputStream(path)) {
            DataOutputStream outputStream = new DataOutputStream(stream);
            write(element, outputStream);
            outputStream.close();
        }
    }

    public void write(JsonElement element, DataOutput output)
            throws IOException
    {
        Objects.requireNonNull(element);

        if (element instanceof JsonObject obj) {
            if (obj.size() == 0) {
                output.writeByte(TYPE_EMPTY_OBJECT);
            } else {
                output.writeByte(TYPE_OBJECT);
                writeObj(obj, output);
            }

            return;
        }

        if (element instanceof JsonArray arr) {
            if (arr.isEmpty()) {
                output.writeByte(TYPE_EMPTY_ARRAY);
            } else {
                output.writeByte(TYPE_ARRAY);
                writeArray(arr, output);
            }

            return;
        }

        if (element.isJsonNull()) {
            output.writeByte(TYPE_NULL);
            return;
        }

        var primitive = element.getAsJsonPrimitive();
        writePrimitive(primitive, output);
    }

    private void writeObj(JsonObject obj, DataOutput output)
            throws IOException
    {
        output.writeInt(obj.size());

        for (var e: obj.entrySet()) {
            output.writeUTF(e.getKey());
            write(e.getValue(), output);
        }
    }

    private void writeArray(JsonArray array, DataOutput output)
            throws IOException
    {
        output.writeInt(array.size());

        for (var e: array) {
            write(e, output);
        }
    }

    private void writePrimitive(JsonPrimitive primitive, DataOutput output)
            throws IOException
    {
        if (primitive.isBoolean()) {
            output.writeByte(
                    primitive.getAsBoolean()
                            ? TYPE_TRUE
                            : TYPE_FALSE
            );
            return;
        }

        if (primitive.isString()) {
            output.writeByte(TYPE_STRING);
            output.writeUTF(primitive.getAsString());
            return;
        }

        Number number = primitive.getAsNumber();

        if (number instanceof BigInteger bigInt) {
            output.writeByte(TYPE_BIG_INTEGER);
            byte[] values = bigInt.toByteArray();

            output.writeInt(values.length);
            for (var b: values) {
                output.writeByte(b);
            }

            return;
        }

        if (number instanceof BigDecimal decimal) {
            output.writeByte(TYPE_BIG_DECIMAL);
            output.writeUTF(decimal.toString());
            return;
        }

        if (number.byteValue() == number.doubleValue()) {
            output.writeByte(TYPE_BYTE);
            output.writeByte(number.byteValue());
            return;
        }

        if (number.shortValue() == number.doubleValue()) {
            output.writeByte(TYPE_SHORT);
            output.writeShort(number.shortValue());
            return;
        }

        if (number.intValue() == number.doubleValue()) {
            output.writeByte(TYPE_INT);
            output.writeInt(number.intValue());
            return;
        }

        if (number.longValue() == number.doubleValue()) {
            output.writeByte(TYPE_LONG);
            output.writeLong(number.longValue());
            return;
        }

        if (number.floatValue() == number.doubleValue()) {
            output.writeByte(TYPE_FLOAT);
            output.writeFloat(number.floatValue());
            return;
        }

        output.writeByte(TYPE_DOUBLE);
        output.writeDouble(number.doubleValue());
    }
}