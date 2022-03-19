package net.forthecrown.utils;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * General Utility functions as well as some useful variables, like variables for the two main worlds, world and world_void and the server's time zone lol
 */
public final class FtcUtils {
    private FtcUtils() {}

    //The max and min Y level constants
    public static final int
            MAX_Y   = 312,
            MIN_Y   = -64,
            Y_SIZE  = MAX_Y - MIN_Y;

    public static final CrownRandom RANDOM = new CrownRandom();

    public static long worldTimeToYears(World world){
        return worldTimeToDays(world) / 365;
    }

    public static long worldTimeToDays(World world) {
        return ((world.getFullTime() / 1000) / 24);
    }

    //True if the string is null or contains only blank spaces
    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    public static UUID uuidFromName(String playerName){
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(playerName);
        return player == null ? null : player.getUniqueId();
    }

    public static String addAnS(long amount){
        if(amount == 1) return "";
        return "s";
    }

    public static InputStream getFileOrResource(String path)  {
        File file = new File(Crown.dataFolder(), path);
        if(file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        return Crown.resource(path);
    }

    public static CraftPlayerProfile profileWithTextureID(@Nullable String name, @Nullable UUID id, String textureID) {
        return profileWithTexture(name, id, "http://textures.minecraft.net/texture/" + textureID);
    }

    public static CraftPlayerProfile profileWithTexture(@Nullable String name, @Nullable UUID id, String textureLink) {
        CraftPlayerProfile profile = new CraftPlayerProfile(id, name);

        profile.setProperty(
                new ProfileProperty(
                        "textures",
                        Base64.getEncoder().encodeToString(
                                ("{\"textures\":{\"SKIN\":{\"url\":\"" + textureLink + "\"}}}").getBytes()
                        )
                )
        );

        return profile;
    }

    public static Team getNoClipTeam() {
        return Bukkit.getScoreboardManager().getMainScoreboard().getTeam("NoClip");
    }

    public static boolean isClearAbove(Location location) {
        return isClearAbove0(WorldVec3i.of(location).mutable());
    }

    public static boolean isClearAbove(WorldVec3i pos1) {
        return isClearAbove0(pos1.mutable());
    }

    private static boolean isClearAbove0(WorldVec3i pos) {
        for (int i = pos.getY(); i < MAX_Y; i++) {
            pos.above();

            if(!isClear(pos.getMaterial())) return false;
        }

        return true;
    }

    private static boolean isClear(Material material) {
        if(!material.isBlock()) return true;
        if(material.isCollidable()) return false;

        return material.isAir();
    }

    public static void handleSyntaxException(Audience sender, CommandSyntaxException exception) {
        if(exception instanceof RoyalCommandException e) {
            sender.sendMessage(e.formattedText());
            return;
        }

        sender.sendMessage(GrenadierUtils.formatCommandException(exception));
    }

    //Clears all the effects on a living entity
    public static void clearEffects(LivingEntity player){
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
    }

    public static void safeRunnable(ExceptionedRunnable runnable){
        try {
            runnable.run();
        } catch (Throwable e){
            Crown.logger().error("Error while running safeRunnable", e);
        }
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

    public static void grantAdvancement(Advancement advancement, Player player) {
        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String s: progress.getRemainingCriteria()) {
            progress.awardCriteria(s);
        }
    }

    public static Location findHazelLocation() {
        PopulationRegion region = Crown.getRegionManager().get(FtcVars.spawnRegion.get());
        return region.getPoleBottom().add(0, 6, 0).toLocation();
    }

    public static boolean isNaturallyPlaced(Block b) {
        // This could be a List<BlockLookupResult> but no, everything
        // must become string, so says the almighty dumdum that made
        // CoreProtect
        CoreProtectAPI api = CoreProtect.getInstance().getAPI();
        List<String[]> lookup = api.blockLookup(b, 0);

        return ListUtils.isNullOrEmpty(lookup);
    }

    public static Region getSelectionSafe(com.sk89q.worldedit.entity.Player wePlayer) throws CommandSyntaxException {
        Region selection;

        try {
            selection = wePlayer.getSelection();
        } catch (IncompleteRegionException e) {
            throw FtcExceptionProvider.create("You must select a region to scan");
        }

        return selection;
    }

    public static Region getSelectionNoExc(com.sk89q.worldedit.entity.Player wePlayer) {
        try {
            return getSelectionSafe(wePlayer);
        } catch (CommandSyntaxException e) {
            return null;
        }
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

    public static String arabicToRoman(long number) {
        StringBuilder sb = new StringBuilder();

        if (number == 0) return "0";
        if (number < 0) {
            sb.append('-');
            number = -number;
        }
        if (number > 4000) {
            sb.append("MMMM".repeat((int) (number / 4000)));
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

    public static NamespacedKey ensureBukkit(Key key) {
        return key instanceof NamespacedKey ? (NamespacedKey) key : new NamespacedKey(key.namespace(), key.value());
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
