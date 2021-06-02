package net.forthecrown.repop;

import java.util.HashMap;
import java.util.Map;

public class Repopulation {

    private static final Map<String, AbstractRepopulator> POPULATORS = new HashMap<>();

    public static void main(String... args) {
        POPULATORS.put("end", new EndRepopulator());
    }


}
