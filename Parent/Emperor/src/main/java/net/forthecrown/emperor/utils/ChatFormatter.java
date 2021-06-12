package net.forthecrown.emperor.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.StaffChat;
import net.forthecrown.emperor.admin.record.PunishmentRecord;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.events.ChatEvents;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.FtcUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.enums.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class meant for methods which, in some way, format a string or component
 */
public class ChatFormatter {
    private static final DecimalFormat DECIMAL_FORMAT;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    static {
        DECIMAL_FORMAT = new DecimalFormat("#.##");
        DECIMAL_FORMAT.setGroupingUsed(true);
        DECIMAL_FORMAT.setGroupingSize(3);
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

    public static Component formatChat(Player source, Component displayName, Component message){
        CrownUser user = UserManager.getUser(source);

        String strMessage = ChatUtils.getString(message);
        boolean inSenateWorld = source.getWorld().equals(ChatEvents.SENATE_WORLD);
        boolean staffChat = StaffChat.toggledPlayers.contains(source);

        if(source.hasPermission(Permissions.DONATOR_2) || inSenateWorld || staffChat) strMessage = translateHexCodes(strMessage);
        if(source.hasPermission(Permissions.DONATOR_3) || inSenateWorld || staffChat) strMessage = formatEmojis(strMessage);

        if(strMessage.length() > 8 && !source.hasPermission(Permissions.IGNORE_CHAT_CASE)){
            int upCastCharNumber = 0;
            for(int i = 0; i < strMessage.length(); i++){
                if(Character.isUpperCase(strMessage.charAt(i))) upCastCharNumber++;
            }
            if(upCastCharNumber > (strMessage.length()/2)) {
                strMessage = StringUtils.capitalize(strMessage.toLowerCase());
                strMessage += "!";
                source.sendMessage(Component.text("Refrain from using all caps messages.").color(NamedTextColor.RED));
            }
        }

        message = ChatUtils.convertString(strMessage);

        TextColor playerColor = inSenateWorld ? NamedTextColor.YELLOW : TextColor.color(240, 240, 240);
        if(staffChat) playerColor = NamedTextColor.GRAY;

        return Component.text()
                .append(StaffChat.toggledPlayers.contains(source) ? StaffChat.PREFIX : Component.empty())
                .append(user.nickDisplayName().color(playerColor))
                .append(Component.text(" > ").style(
                        Style.style(NamedTextColor.GRAY, TextDecoration.BOLD)
                ))
                .append(message)
                .build();
    }

    public static Component joinMessage(CrownUser user){
        return Component.translatable("multiplayer.player.joined", user.nickDisplayName().color(getJoinColor(user)))
                .hoverEvent(Component.text("Click to say hello!"))
                .clickEvent(ClickEvent.runCommand(hello() + user.getNickOrName() + '!'))
                .color(NamedTextColor.YELLOW);
    }

    private static String hello(){
        int c_int = CrownUtils.getRandomNumberInRange(0, 2);

        if(c_int == 0) return "Hey ";
        if(c_int == 1) return "Hello ";

        return "Hi ";
    }

    public static Component newNameJoinMessage(CrownUser user1){
        FtcUser user = (FtcUser) user1;

        return Component.translatable("multiplayer.player.joined.renamed",
                user.nickDisplayName().color(getJoinColor(user)),
                Component.text(user.previousNames.get(user.previousNames.size()-1))
        )
                .hoverEvent(Component.text("Click to say hello!"))
                .clickEvent(ClickEvent.runCommand("Hey " + user.getNickOrName() + '!'))
                .color(NamedTextColor.YELLOW);
    }

    public static Component formatLeaveMessage(CrownUser user){
        return Component.translatable("multiplayer.player.left", user.nickDisplayName().color(getJoinColor(user))).color(NamedTextColor.YELLOW);
    }

    public static Component banMessage(PunishmentRecord record){
        Validate.isTrue(record.type == PunishmentType.BAN, "Given record was not a ban record");

        TextComponent.Builder builder = Component.text()
                .append(Component.text("You are banned").color(NamedTextColor.DARK_RED))

                .append(Component.newline())
                .append(Component.text("Banned by: " + record.punisher))

                .append(Component.newline())
                .append(Component.text("Reason: "))
                .append(record.hasReason() ? ChatUtils.convertString(record.reason) : Component.text("This server is not for you"));

        if(!record.isPermanent()){
            builder
                    .append(Component.newline())
                    .append(Component.text("Ban expires in: "))
                    .append(Component.text(timeFromMillisMinusTime(record.expiresAt)));
        }

        return builder.build();
    }

    public static TextColor getJoinColor(CrownUser user){
        Rank r = user.getHighestTierRank();

        if(r.getTier().shouldUseColor()) return r.getTier().color;
        return NamedTextColor.YELLOW;
    }

    public static Component formatStringIfAllowed(String msg, CommandSender sender){
        if(sender.hasPermission(Permissions.DONATOR_3)) msg = formatEmojis(msg);

        return ChatUtils.convertString(msg, sender.hasPermission(Permissions.DONATOR_2));
    }

    public static Component queryGems(int amount){
        if(amount == 1 || amount == -1) return Component.translatable("user.gems.singular", Component.text(amount));
        return Component.translatable("user.gems.multiple", Component.text(decimalizeNumber(amount)));
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

    public static Component itemName(ItemStack item){
        Validate.notNull(item, "Item was null");

        ItemMeta meta = item.getItemMeta();
        if(meta.hasDisplayName()) return meta.displayName().hoverEvent(item);

        return Component.translatable(item.getType().getTranslationKey()).hoverEvent(item);
    }

    public static String getItemNormalName(ItemStack item){
        return normalEnum(item.getType());
    }

    public static String normalEnum(@NotNull Enum<?> anum){
        Validate.notNull(anum, "Provided Enum was null");
        return capitalizeWords(anum.toString().replaceAll("_", " ").toLowerCase());
    }

    public static String decimalizeNumber(@NotNull Number number){
        Validate.notNull(number, "Number was null");
        return DECIMAL_FORMAT.format(number);
    }

    public static Component prettyLocationMessage(Location l, boolean includeWorld){
        return Component.text("" +
                "X=" + l.getBlockX() + " " +
                "Y=" + l.getBlockY() + " " +
                "Z=" + l.getBlockZ() +
                (includeWorld ? " world: " + l.getWorld().getName() : ""));
    }

    public static Component clickableLocationMessage(Location l, boolean includeWorld){
        return prettyLocationMessage(l, includeWorld)
                .hoverEvent(Component.text("Click to teleport!"))
                .clickEvent(ClickEvent.runCommand("/tp_exact " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + " " + l.getPitch() + " " + l.getYaw() + " " + l.getWorld().getName()));
    }

    public static Component itemMessage(ItemStack itemStack){
        Validate.notNull(itemStack);
        return Component.text()
                .append(Component.text(itemStack.getAmount()))
                .append(Component.text(" "))
                .append(itemName(itemStack))
                .build();
    }

    public static String formatEmojis(String string){ //replaces every emoji in the given string
        return string
                .replaceAll(":shrug:", "¯\\\\_(ツ)_/¯")
                .replaceAll(":ughcry:", "(ಥ﹏ಥ)")
                .replaceAll(":hug:", "༼ つ ◕_◕ ༽つ")
                .replaceAll(":hugcry:", "༼ つ ಥ_ಥ ༽つ")
                .replaceAll(":bear:", "ʕ• ᴥ •ʔ")
                .replaceAll(":smooch:", "( ^ 3^) ❤")
                .replaceAll(":why:", "ლ(ಠ益ಠლ)")
                .replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻")
                .replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)")
                .replaceAll(":pretty:", "(◕‿◕ ✿)")
                .replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧")
                .replaceAll(":blush:", "(▰˘◡˘▰)")
                .replaceAll(":sad:", "(._. )")
                .replaceAll(":pleased:", "(ᵔᴥᵔ)")
                .replaceAll(":fedup:", "(¬_¬)")
                .replaceAll(":reallysad:", "(◉︵◉ )");
    }

    public static String timeFromMillisMinusTime(long millis){
        return convertMillisIntoTime(millis - System.currentTimeMillis());
    }

    public static String convertTicksIntoTime(long ticks){
        return convertMillisIntoTime(ticks * 50);
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
        if(seconds > 0) stringBuilder.append(seconds).append(" second").append(s(seconds));
        else if(millis < 1000) stringBuilder.append(millis).append(" millisecond").append(s(millis));

        return stringBuilder.toString();
    }

    public static Component millisIntoTime(long millis){
        return Component.text(convertMillisIntoTime(millis));
    }

    private static String s(long l){
        return l == 1 ? "" : "s";
    }

    public static void checkNickAllowed(String nick) throws CommandSyntaxException {
        if(CrownCore.getMaxNickLength() < nick.length()) throw FtcExceptionProvider.nickTooLong(nick.length());

        if(!CrownCore.allowOtherPlayerNameNicks()){
            if(Bukkit.getOfflinePlayerIfCached(nick) != null) throw FtcExceptionProvider.create("Nickname cannot be the name of another player");
        }
    }
}
