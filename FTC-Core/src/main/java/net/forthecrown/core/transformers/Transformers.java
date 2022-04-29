package net.forthecrown.core.transformers;

import net.forthecrown.core.Crown;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class Transformers {
    private static final Logger LOGGER = Crown.logger();

    private static final Set<String> COMPLETED_TRANSFORMERS = new HashSet<>();

    private static File file(File dataFile) {
        return new File(dataFile, "dataTransformerInfo.txt");
    }

    public static boolean shouldRun(Class c) {
        return !COMPLETED_TRANSFORMERS.contains(c.getName());
    }

    public static void complete(Class c) {
        if (COMPLETED_TRANSFORMERS.add(c.getName())) {
            LOGGER.info(c.getSimpleName() + " marked as finished and transformer progress saved");

            save(Crown.dataFolder());
        }
    }

    public static void load(File dataFile) {
        COMPLETED_TRANSFORMERS.clear();

        File f = file(dataFile);

        if(!f.exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            reader.lines()
                    .forEach(s -> {
                        LOGGER.info("Loaded completed transformer: {}", s);
                        COMPLETED_TRANSFORMERS.add(s);
                    });
        } catch (IOException e) {
            LOGGER.error("Couldn't load transformer data file", e);
        }
    }

    public static void save(File dataFile) {
        File f = file(dataFile);

        if (COMPLETED_TRANSFORMERS.isEmpty()) {
            if (f.exists()) {
                f.delete();
            }

            return;
        }

        try {
            if(!f.exists()) {
                f.createNewFile();
            }

            FileWriter writer = new FileWriter(f, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            for (var v: COMPLETED_TRANSFORMERS) {
                bufferedWriter.write(v);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            writer.close();
        } catch (IOException e) {
            LOGGER.info("Couldn't save transformer data file", e);
        }
    }
}