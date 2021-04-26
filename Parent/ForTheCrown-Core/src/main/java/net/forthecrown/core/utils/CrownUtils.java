package net.forthecrown.core.utils;

import net.forthecrown.core.FtcCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

/**
 * General Utility functions as well as some useful variables, like variables for the two main worlds, world and world_void and the server's time zone lol
 */
public final class CrownUtils {

    public static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("GMT+01:00");
    public static final Location LOCATION_HAZELGUARD = new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5);
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final DecimalFormat DECIMAL_FORMAT;

    public static final World WORLD = Objects.requireNonNull(Bukkit.getWorld("world"));
    public static final World WORLD_VOID = Objects.requireNonNull(Bukkit.getWorld("world_void"));

    private CrownUtils() {}
    static {
        DECIMAL_FORMAT = new DecimalFormat("#.##");
        DECIMAL_FORMAT.setGroupingUsed(true);
        DECIMAL_FORMAT.setGroupingSize(3);
    }

    public static int worldTimeToYears(World world){
        return (int) ((world.getFullTime()/1000)/24)/365;
    }

    public static String formatEmojis(String string){ //replaces every emoji in the given string
        String message = string;
        message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
        message = message.replaceAll(":ughcry:", "(ಥ﹏ಥ)");
        message = message.replaceAll(":hug:", "༼ つ ◕_◕ ༽つ");
        message = message.replaceAll(":hugcry:", "༼ つ ಥ_ಥ ༽つ");
        message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
        message = message.replaceAll(":smooch:", "( ^ 3^) ❤");
        message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
        message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻");
        message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
        message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
        message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
        message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
        message = message.replaceAll(":sad:", "(._. )");
        message = message.replaceAll(":pleased:", "(ᵔᴥᵔ)");
        message = message.replaceAll(":fedup:", "(¬_¬)");
        return message;
    }

    //Yeah, no clue
    public static String translateHexCodes(@NotNull String textToTranslate) {
        Validate.notNull(textToTranslate, "Text to translate cannot be null");

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            final boolean isEscaped = matcher.group(0) == null;
            if(!isEscaped){
                matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
            } //isEscaped variable should prevent this from throwing exceptions when faced with special characters... maybe
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String capitalizeWords(String str){
        String[] words = str.split("\\s");
        String capitalizeWord = "";
        for(String w: words){
            String first = w.substring(0,1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizeWord.trim();
    }

    public static Integer getRandomNumberInRange(int min, int max) {
        return new CrownRandom().intInRange(min, max);
    }

    public static String getDateFromMillis(long millis){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);

        int day = c.get(Calendar.DAY_OF_MONTH);
        String s = day + "";

        DateFormatSymbols sF = DateFormatSymbols.getInstance();

        s += getDaySuffix(day);
        s += "of ";
        s += sF.getMonths()[c.get(Calendar.MONTH)];
        s += " " + c.get(Calendar.YEAR);

        return s;
    }

    private static String getDaySuffix(int day){
        if (day >= 11 && day <= 13) {
            return "th ";
        }
        switch (day%10){
            case 1:
                return "st ";
            case 2:
                return "nd ";
            case 3:
                return "rd ";
            default:
                return "th ";
        }
    }

    public static String getItemNormalName(@NotNull ItemStack stack){
        return normalMaterialName(stack.getType());
    }

    public static String normalMaterialName(@NotNull Material material){
        return normalEnum(material);
    }

    public static String normalEnum(@NotNull Enum<?> anum){
        Validate.notNull(anum, "Provided Enum was null");
        return capitalizeWords(anum.toString().replaceAll("_", " ").toLowerCase());
    }

    public static String decimalizeNumber(@NotNull Number number){
        Validate.notNull(number, "Number was null");
        return DECIMAL_FORMAT.format(number);
    }

    public static String convertTicksIntoTime(long ticks){
        return convertMillisIntoTime(ticks * 500);
    }

    public static String convertMillisIntoTime(long millis){
        long hours = (millis / 3600000);
        long minutes = (millis /60000) % 60;
        long seconds = (millis / 1000) % 60;
        long days = hours / 24;
        hours -= days*24;

        StringBuilder stringBuilder = new StringBuilder();
        if(days > 0) stringBuilder.append(days).append(" day").append(s(days)).append(", ");
        if(hours > 0) stringBuilder.append(hours).append(" hour").append(s(hours)).append(", ");
        if(minutes > 0) stringBuilder.append(minutes).append(" minute").append(s(minutes)).append(" and ");

        stringBuilder.append(seconds).append(" second").append(s(seconds));

        return stringBuilder.toString();
    }

    private static String s(long l){
        return l == 1 ? "" : "s";
    }

    public static boolean isNullOrBlank(String str){
        return str == null || str.isBlank();
    }

    public static UUID uuidFromName(String playerName){
        try {
            return Bukkit.getOfflinePlayerIfCached(playerName).getUniqueId();
        } catch (NullPointerException ignored) { return null; }
    }

    public static void showLeaderboard(Player player, String objectiveName){
        Scoreboard mainScoreboard = getServer().getScoreboardManager().getMainScoreboard();
        Objective objective = mainScoreboard.getObjective(objectiveName);

        TextComponent displayName = Component.text()
                .color(NamedTextColor.GOLD)
                .append(Component.text("---"))
                .append(Component.text("Leaderboard").color(NamedTextColor.YELLOW))
                .append(Component.text("---"))
                .build();

        Scoreboard scoreboard = getServer().getScoreboardManager().getNewScoreboard();
        Objective newObj = scoreboard.registerNewObjective(player.getName(), "dummy", displayName);

        for(String name : objective.getScoreboard().getEntries()) {
            if(!objective.getScore(name).isScoreSet() || objective.getScore(name).getScore() == 0) continue;
            newObj.getScore(name).setScore(objective.getScore(name).getScore());
        }

        newObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), () -> player.setScoreboard(mainScoreboard), 300L);
    }

    public static Component prettyLocationMessage(Location l, boolean includeWorld){
        return Component.text("X=" + l.getBlockX() +
                " Y=" + l.getBlockY() +
                " Z=" + l.getBlockZ() +
                (includeWorld ? " world: " + l.getWorld().getName() : ""));
    }

    /*
     * The following RomanNumeral converters and code was made by Baeldung
     * Link: https://www.baeldung.com/java-convert-roman-arabic
     */

    public static int romanToArabic(String input) {
        String romanNumeral = input.toUpperCase();
        int result = 0;

        List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

        int i = 0;

        while ((romanNumeral.length() > 0) && (i < romanNumerals.size())) {
            RomanNumeral symbol = romanNumerals.get(i);
            if (romanNumeral.startsWith(symbol.name())) {
                result += symbol.getValue();
                romanNumeral = romanNumeral.substring(symbol.name().length());
            } else {
                i++;
            }
        }

        if (romanNumeral.length() > 0) {
            throw new IllegalArgumentException(input + " cannot be converted to a Roman Numeral");
        }

        return result;
    }

    public static String arabicToRoman(int number) {
        if (number == 0) return "0";
        String prefix = "";
        if (number < 0) {
            prefix = "-";
            number = -number;
        }
        if (number > 4000) {
            for (int i = 0; i < number / 4000; i++) prefix += "MMMM";
            number = number % 4000;
        }

        List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

        int i = 0;
        StringBuilder sb = new StringBuilder();

        while ((number > 0) && (i < romanNumerals.size())) {
            RomanNumeral currentSymbol = romanNumerals.get(i);
            if (currentSymbol.getValue() <= number) {
                sb.append(currentSymbol.name());
                number -= currentSymbol.getValue();
            } else {
                i++;
            }
        }

        return prefix + sb.toString();
    }

    public enum RomanNumeral {
        I(1), IV(4), V(5), IX(9), X(10),
        XL(40), L(50), XC(90), C(100),
        CD(400), D(500), CM(900), M(1000);

        private final int value;

        RomanNumeral(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static List<RomanNumeral> getReverseSortedValues() {
            return Arrays.stream(values())
                    .sorted(Comparator.comparing((RomanNumeral e) -> e.value).reversed())
                    .collect(Collectors.toList());
        }
    }
}
