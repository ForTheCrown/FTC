package net.forthecrown.log;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.ArrayIterator;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A log file is a binary representation of the log data of a single day.
 * <p>
 * The logger file is split between 2 major parts: The header, and the content.
 * The header details where in the file a log section is located and what
 * sections are in a file.
 * <p>
 * The header begins with an integer size, stating how many sections the file
 * contains. More specifically, this data is stored in UTF-8, <code>long</code>
 * pairs. The long value is an offset of the section's beginning byte address
 * from the end of the header
 * <p>
 * Sections are lists of {@link BinaryJson} data that begin with a size integer
 * stating how many entries there are. Individual entries are then deserialized
 * and translated into entries by {@link LogSchema} instances.
 */
public class LogFile {
    private static final Logger LOGGER = FTC.getLogger();

    public static final short FILE_VERSION = 1;

    private final DataLog[] logs;

    /** Array of section keys, Contains {@link #count} number of entries */
    private final String[] keys;

    /** Array of section data, Contains {@link #count} number of entries */
    private final ByteArrayOutputStream[] logData;

    /**
     * Versions of each written section, Contains
     * {@link #count} number of entries
     */
    private final short[] versions;

    /** The amount of written logs written to the {@link #logData} array */
    private int count;

    LogFile(DataLog[] logs) {
        this.logs = logs;
        this.keys = new String[logs.length];
        this.logData = new ByteArrayOutputStream[logs.length];
        this.versions = new short[logs.length];
    }

    public void fillArrays() throws IOException {
        // Iterator skips over null entries in the log array
        var it = ArrayIterator.unmodifiable(logs);

        int index = 0;
        while (it.hasNext()) {
            DataLog log = it.next();

            if (log.isEmpty()) {
                continue;
            }

            var keyOptional = DataLogs.SCHEMAS.getHolderByValue(log.getSchema());

            if (keyOptional.isEmpty()) {
                LOGGER.warn("Unregisterd schema found! Cannot serialize");
                continue;
            }

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(byteArray);

            keys[index] = keyOptional.get().getKey();
            versions[index] = keyOptional.get().getValue().getVersion();
            logData[index++] = byteArray;

            count = index;

            dataOutput.writeInt(log.size());

            for (var entry: log.getEntries()) {
                var json = log.getSchema()
                        .serialize(JsonOps.INSTANCE, entry)
                        .resultOrPartial(LOGGER::error);

                if (json.isEmpty()) {
                    continue;
                }

                JsonElement serialized = json.get();
                dataOutput.writeLong(entry.getDate());
                BinaryJson.write(serialized, dataOutput);
            }
        }
    }

    public void write(Path path) throws IOException {
        OutputStream stream = Files.newOutputStream(path);
        DataOutputStream output = new DataOutputStream(stream);
        long offset = 0;

        output.writeShort(FILE_VERSION);
        output.writeInt(count);

        // Create header
        for (int i = 0; i < count; i++) {
            output.writeUTF(keys[i]);
            output.writeLong(offset);
            output.writeShort(versions[i]);

            // Accumulate offset, offset begins after header ends
            offset += logData[i].size();
        }

        for (int i = 0; i < count; i++) {
            var arr = logData[i];
            arr.writeTo(stream);
        }

        output.close();
        stream.close();
    }

    public static Header readHeader(DataInput input) throws IOException {
        short fileVersion = input.readShort();
        int size = input.readInt();

        Header header = new Header(size, fileVersion);
        header.read(input);

        return header;
    }

    public static void readQuery(DataInput input,
                                 LogSchema schema,
                                 QueryResultBuilder builder,
                                 HeaderElement headerElement
    ) throws IOException {
        int size = input.readInt();

        for (int i = 0; i < size; i++) {
            long date = input.readLong();
            JsonElement element = BinaryJson.read(input);

            var dynamic = new Dynamic<>(JsonOps.INSTANCE, element);
            dynamic = DataLogs.fix(dynamic, schema, headerElement.version);

            var entryOpt = schema
                    .deserialize(dynamic)
                    .resultOrPartial(LOGGER::error);

            if (entryOpt.isEmpty()) {
                continue;
            }

            var entry = entryOpt.get()
                    .setDate(date);

            if (builder.getQuery().test(entry)) {
                builder.accept(entry);

                if (builder.hasFoundEnough()) {
                    return;
                }
            }
        }
    }

    public static void readLog(DataInput input, LogContainer container)
            throws IOException
    {
        Header header = readHeader(input);

        for (var element : header) {
            var schemaOpt = DataLogs.SCHEMAS.getHolder(element.section);

            if (schemaOpt.isEmpty()) {
                LOGGER.warn("Unknown schema found: '{}' Skipping...",
                        element.section
                );
                continue;
            }

            var schema = schemaOpt.get();
            int size = input.readInt();
            DataLog log = new DataLog(schema.getValue());

            for (int j = 0; j < size; j++) {
                long date = input.readLong();
                JsonElement jsonElement = BinaryJson.read(input);

                var dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonElement);
                dynamic = DataLogs.fix(
                        dynamic,
                        schema.getValue(),
                        element.version
                );

                var entryOpt = schema.getValue()
                        .deserialize(dynamic)
                        .resultOrPartial(LOGGER::error);

                if (entryOpt.isEmpty()) {
                    continue;
                }

                log.add(entryOpt.get().setDate(date));
            }

            container.setLog(schema, log);
        }
    }

    static class HeaderElement {
        String section;
        long offset;
        short version;
    }

    @Getter
    static class Header implements Iterable<HeaderElement> {
        private final String[] sections;
        private final long[] offsets;
        private final short[] versions;
        private final short fileVersion;

        public Header(int size, short fileVersion) {
            this.fileVersion = fileVersion;

            this.sections = new String[size];
            this.offsets = new long[size];
            this.versions = new short[size];
        }

        void read(DataInput input) throws IOException {
            for (int i = 0; i < sections.length; i++) {
                sections[i] = input.readUTF();
                offsets[i] = input.readLong();
                versions[i] = input.readShort();
            }
        }

        @NotNull
        @Override
        public Iterator<HeaderElement> iterator() {
            return new Iterator<>() {
                int index = 0;
                HeaderElement singleton;

                @Override
                public boolean hasNext() {
                    return index < sections.length;
                }

                @Override
                public HeaderElement next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    if (singleton == null) {
                        singleton = new HeaderElement();
                    }

                    singleton.section = sections[index];
                    singleton.offset = offsets[index];
                    singleton.version = versions[index];
                    ++index;

                    return singleton;
                }
            };
        }
    }
}