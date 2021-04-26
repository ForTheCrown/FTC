package net.forthecrown.vikings;

import org.apache.commons.lang.Validate;

import java.util.Random;

public class VikingBuilds {

    private static final Random random = new Random();

    public static final String[] LARGE_HOUSES = {"raid_barn_1", "raid_house_6", "raid_house_1", "raid_house_2"};
    public static final String[] SMALL_HOUSES = {"raid_house_4", "raid_house_5", "raid_house_3"};
    public static final String[] CHURCH = {"raid_church_1"};
    public static final String[] MISC_BUILDINGS = {"raid_well_1"};

    public static String randomFromArray(String[] array){
        Validate.notNull(array, "Array cannot be null");
        return array[array.length-1 == 0 ? 0 : random.nextInt(array.length)];
    }

    public static String replacePlaceholders(String str){
        if(str.isBlank()) str = randomFromArray(SMALL_HOUSES);
        else {
            str = str.replaceAll("placeholder_large", randomFromArray(LARGE_HOUSES));
            str = str.replaceAll("placeholder_small", randomFromArray(SMALL_HOUSES));
            str = str.replaceAll("placeholder_misc", randomFromArray(MISC_BUILDINGS));
            str = str.replaceAll("placeholder_church", randomFromArray(CHURCH));
        }
        return str;
    }
}
