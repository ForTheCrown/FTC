package net.forthecrown.repop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class Repopulation {

    private static final Map<String, AbstractRepopulator> POPULATORS = new HashMap<>();
    public static final Logger logger = Logger.getLogger(Repopulation.class.getSimpleName());

    public static void main(String... args) {
        POPULATORS.put("end", new EndRepopulator());
        POPULATORS.put("village", new VillageRepopulator());
    }

    public static void runSync(String id){
        AbstractRepopulator repopulator = getFromName(id);

        try {
            repopulator.initiate(logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runAsync(String id){
        AbstractRepopulator repopulator = getFromName(id);

        CompletableFuture.runAsync(() -> {
            try {
                repopulator.initiate(logger);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static AbstractRepopulator getFromName(String id){
        return Objects.requireNonNull(POPULATORS.get(id), "Invalid repopulator");
    }
}
