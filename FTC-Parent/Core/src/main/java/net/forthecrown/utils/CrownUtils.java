package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

/**
 * General Utility functions as well as some useful variables, like variables for the two main worlds, world and world_void and the server's time zone lol
 */
public final class CrownUtils {

    public static final Location LOCATION_HAZELGUARD = new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5);
    public static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("GMT+01:00");

    private CrownUtils() {}

    public static int worldTimeToYears(World world){
        return (int) ((world.getFullTime()/1000)/24)/365;
    }

    public static Integer randomIntInRange(int min, int max) {
        return new CrownRandom().intInRange(min, max);
    }

    public static boolean isNullOrBlank(String str){
        return str == null || str.isBlank();
    }

    public static UUID uuidFromName(String playerName){
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(playerName);
        return player == null ? null : player.getUniqueId();
    }

    public static String addAnS(int amount){
        if(amount == 1 || amount == -1) return "";
        return "s";
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), () -> player.setScoreboard(mainScoreboard), 300L);
    }

    public static String locationToFilename(Location l){
        return l.getWorld().getName() + "_" + l.getBlockX() + "_" + l.getBlockY() + "_" + l.getBlockZ();
    }

    public static Key parseKey(String str) {
        return parseKey(new StringReader(str));
    }

    public static Key parseKey(StringReader reader) {
        String first = reader.readUnquotedString();
        if(reader.canRead() && reader.peek() == ':'){
            reader.skip();
            return Key.key(first, reader.readUnquotedString());
        }
        return Key.key(CrownCore.inst(), first);
    }

    public static Key checkNotBukkit(Key key){
        if(!(key instanceof NamespacedKey)) return key;
        return Key.key(key.namespace(), key.value());
    }

    public static CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder builder, Iterable<Key> keys){
        return CompletionProvider.suggestKeys(builder, keys);
    }

    public static CompletableFuture<Suggestions> suggestKeysNoNamespace(SuggestionsBuilder builder, Iterable<Key> keys){
        return CompletionProvider.suggestMatching(builder, ListUtils.fromIterable(keys, Key::value));
    }

    public static Collection<? extends Player> getVisiblePlayers(CommandSource source){
        if(source.isPlayer()){
            try {
                Player player = source.asPlayer();
                List<Player> returnVal = new ArrayList<>();

                for (Player p : Bukkit.getOnlinePlayers()){
                    if(player.canSee(p)) returnVal.add(p);
                }

                return returnVal;
            } catch (CommandSyntaxException ignored) {}
        }
        return Bukkit.getOnlinePlayers();
    }

    public static CompletableFuture<Suggestions> suggestPlayernamesAndEmotes(CommandContext<CommandSource> c, SuggestionsBuilder builder, boolean ignorePerms){
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ')+1);

        CompletionProvider.suggestPlayerNames(builder);
        if(c.getSource().hasPermission(Permissions.DONATOR_3) || ignorePerms){
            return CrownCore.getEmotes().getSuggestions(c, builder, ignorePerms);
        }

        return builder.buildFuture();
    }

    public static boolean isItemEmpty(ItemStack itemStack){
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public static boolean isInRange(int check, int min, int max){
        if(check < min) return false;
        return check <= max;
    }

    public static Component entityDisplayName(Entity entity){
        return ChatUtils.vanillaToAdventure(((CraftEntity) entity).getHandle().getDisplayName());
    }

    public static Component sourceDisplayName(CommandSource source){
        if(source.isPlayer()) return UserManager.getUser(source.textName()).nickDisplayName();
        return source.displayName();
    }

    public static Player fromAudience(Audience audience){
        if(audience instanceof Player) return (Player) audience;
        return null;
    }

    public static void clearEffects(LivingEntity player){
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
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