package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.Packet;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

/**
 * General Utility functions as well as some useful variables, like variables for the two main worlds, world and world_void and the server's time zone lol
 */
public final class FtcUtils {
    private FtcUtils() {}

    public static final Location LOCATION_HAZELGUARD = new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5);
    public static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("GMT+01:00");

    public static <T> Predicate<T> alwaysAccept() {
        return t -> true;
    }

    public static void sendPacket(Player player, Packet packet) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().networkManager.send(packet);
    }

    public static int worldTimeToYears(World world){
        return (int) ((world.getFullTime()/1000)/24)/365;
    }

    @Deprecated
    public static int randomInRange(int min, int max) {
        return new CrownRandom().intInRange(min, max);
    }

    //True if the string is null or contains only blank spaces
    public static boolean isNullOrBlank(String str){
        return str == null || str.isBlank();
    }

    public static UUID uuidFromName(String playerName){
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(playerName);
        return player == null ? null : player.getUniqueId();
    }

    public static String addAnS(long amount){
        if(amount == 1 || amount == -1) return "";
        return "s";
    }

    //Shows a leaderboard, used by /deathtop and /crowntop
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
            //If you don't have a set score, or your score is 0, dont' show it
            if(!objective.getScore(name).isScoreSet() || objective.getScore(name).getScore() == 0) continue;

            newObj.getScore(name).setScore(objective.getScore(name).getScore());
        }

        newObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> player.setScoreboard(mainScoreboard), 300L);
    }

    //This is bad, underscores SHOULD NOT be used in file names with locations, world names can get caught up in it.
    //Need to use spaces instead
    public static String locationToFilename(Location l){
        return l.getWorld().getName() + "_" + l.getBlockX() + "_" + l.getBlockY() + "_" + l.getBlockZ();
    }

    public static Location parseFilename(String name) {
        World world = null;
        for (World w: Bukkit.getWorlds()) {
            if(name.startsWith(w.getName())) world = w;
        }

        assert world != null;

        name = name.replaceAll(world.getName() + "_", "");
        String[] cords = name.split("_");

        Validate.isTrue(cords.length == 3, "Invalid file name");

        int x = Integer.parseInt(cords[0]);
        int y = Integer.parseInt(cords[1]);
        int z = Integer.parseInt(cords[2]);

        return new Location(world, x, y, z);
    }

    public static Key parseKey(String str) throws IllegalStateException {
        return parseKey(new StringReader(str));
    }

    public static Key parseKey(StringReader reader) throws IllegalStateException {
        try {
            NamespacedKey key = FtcCommands.ftcKeyType().parse(reader);
            return Key.key(key.namespace(), key.value());
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Invalid string to parse");
        }
    }

    public static void handleSyntaxException(CommandSender sender, CommandSyntaxException exception) {
        if(exception instanceof RoyalCommandException) {
            RoyalCommandException e = (RoyalCommandException) exception;
            sender.sendMessage(e.formattedText());
            return;
        }

        sender.sendMessage(GrenadierUtils.formatCommandException(exception));
    }

    //Makes sure the given key is not a bukkit key, since different hash result
    public static Key checkNotBukkit(Key key){
        if(!(key instanceof NamespacedKey)) return key;
        return Key.key(key.namespace(), key.value());
    }

    //Gets all visible players to the source
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

    public static boolean isItemEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0;
    }

    //Gets a player from an audience, used by the chat event listener in CoreListener
    public static @Nullable Player fromAudience(Audience audience){
        if(audience instanceof Player) return (Player) audience;
        return null;
    }

    //Clears all the effects on a living entity
    public static void clearEffects(LivingEntity player){
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
    }

    public static void safeRunnable(ExceptionedRunnable runnable){
        try {
            runnable.run();
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static NamespacedKey keyToBukkit(Key key) {
        if(key instanceof NamespacedKey) return (NamespacedKey) key;
        return new NamespacedKey(key.namespace(), key.value());
    }

    public static <T> T makeIfNull(T obj, Supplier<T> supplier) {
        return obj == null ? supplier.get() : obj;
    }

    public static Location locFromPosition(Position pos, Location start) {
        return new Location(
                start.getWorld(),
                pos.isXRelative() ? pos.getX() + start.getX() : pos.getX(),
                pos.isYRelative() ? pos.getY() + start.getY() : pos.getY(),
                pos.isZRelative() ? pos.getZ() + start.getZ() : pos.getZ(),
                start.getYaw(),
                start.getPitch()
        );
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
        StringBuilder sb = new StringBuilder();

        if (number == 0) return "0";
        if (number < 0) {
            sb.append('-');
            number = -number;
        }
        if (number > 4000) {
            sb.append("MMMM".repeat(number / 4000));
            number = number % 4000;
        }

        List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

        int i = 0;

        while ((number > 0) && (i < romanNumerals.size())) {
            RomanNumeral currentSymbol = romanNumerals.get(i);
            if (currentSymbol.getValue() <= number) {
                sb.append(currentSymbol.name());
                number -= currentSymbol.getValue();
            } else {
                i++;
            }
        }

        return sb.toString();
    }

    public enum RomanNumeral {
        I(1), IV(4), V(5), IX(9), X(10),
        XL(40), L(50), XC(90), C(100),
        CD(400), D(500), CM(900), M(1000);

        private final short value;

        RomanNumeral(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return value;
        }

        public static List<RomanNumeral> getReverseSortedValues() {
            return Arrays.stream(values())
                    .sorted(Comparator.comparing((RomanNumeral e) -> e.value).reversed())
                    .collect(Collectors.toList());
        }
    }
}
