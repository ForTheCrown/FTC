package net.forthecrown.core.transformers;

import net.forthecrown.core.Crown;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to handle data transformers.
 * A data transformer is anything which trudges through
 * a relatively large amount to data to reformat, fix or change
 * it. Mostly I use these to update data to a new format, like
 * for updating users from YAML -> JSON
 * <p>
 * Data transformers need to have a specific implementation,
 * Normally, each data transformer will be checked if they've been
 * ran before, if that passes, this class will look for a
 * <code>static boolean shouldRun()</code> method, if that's found
 * and returns true, then this class will attempt to call a
 * <code>static void run()</code> method to initiate the transformer
 */
public class Transformers {
    private static final Logger LOGGER = Crown.logger();

    // A list of all current data transformers
    private static final Class[] CURRENT_TRANSFORMERS = {
            RegionResidencyTransformer.class,
            ShopJsonToTag.class
    };

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

    public static void runCurrent() {
        for (var c: CURRENT_TRANSFORMERS) {
            runTransformer(c);
        }
    }

    public static void runTransformer(Class c) {
        if (!shouldRun(c)) return;
        if (!_checkShouldRun(c)) return;

        LOGGER.info("Running {}", c.getSimpleName());

        try {
            Method run = c.getDeclaredMethod("run");
            if (!Modifier.isStatic(run.getModifiers())) {
                LOGGER.warn("Found non static run method in {}", c.getSimpleName());
                return;
            }

            run.setAccessible(true);
            run.invoke(null);

            complete(c);
        } catch (Throwable t) {
            LOGGER.error("Couldn't run data transformer " + c.getSimpleName(), t);
        }
    }

    private static boolean _checkShouldRun(Class c) {
        try {
            Method m = c.getDeclaredMethod("shouldRun");
            m.setAccessible(true);

            return (boolean) m.invoke(null);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}