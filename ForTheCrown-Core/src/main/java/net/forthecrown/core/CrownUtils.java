package net.forthecrown.core;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrownUtils {

    public static ItemStack getCoins(int amount){
        return makeItem(Material.SUNFLOWER, 1, true, "&eRhines", "&6Worth " + amount + " Rhines", "&8Do /deposit to add this to your balance");
    }
    //TODO getRoyalSword, getCrown, getCutlass, getVikingAxe

    public static ItemStack makeItem(Material material, int amount, boolean hideFlags, String name, String... loreStrings) {
        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        if (name != null) meta.setDisplayName(ChatColor.RESET + translateHexCodes(name));
        if (loreStrings != null) {
            List<String> lore = new ArrayList<>();
            for(String s : loreStrings){ lore.add(ChatColor.RESET + translateHexCodes(s)); }
            meta.setLore(lore);
        }
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        result.setItemMeta(meta);
        return result;
    }

    public static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public static String translateHexCodes (String textToTranslate) {

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String replaceEmojis(String string){ //replaces every emoji in the given string
        String message = string;
        message = message.replaceAll(":shrug:", "¯\\_(ツ)_/¯");
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

    private static final Set<Player> ON_COOLDOWN = new HashSet<>();

    public static boolean isOnCooldown(Player player){
        return ON_COOLDOWN.contains(player);
    }
    public static void addToCooldown(Player player, int timeinDelay, boolean permissionIgnore){
        if(player.hasPermission("ftc.cooldownignore") && permissionIgnore) return;

        ON_COOLDOWN.add(player);
        Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> ON_COOLDOWN.remove(player), timeinDelay);
    }

    public static UUID getOffOnUUID(String playerName){
        UUID toReturn;
        try{
            toReturn = Bukkit.getPlayer(playerName).getUniqueId();
        } catch (NullPointerException e){
            toReturn = Bukkit.getOfflinePlayerIfCached(playerName).getUniqueId();
        }
        return toReturn;
    }

    public static Integer getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            return 0;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
