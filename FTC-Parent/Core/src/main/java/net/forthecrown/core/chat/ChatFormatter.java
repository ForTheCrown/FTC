package net.forthecrown.core.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class meant for methods which, in some way, format a string or component
 */
public class ChatFormatter {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#.##"), format -> {
        format.setGroupingUsed(true);
        format.setGroupingSize(3);
    });

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static final Component AFK_SUFFIX = Component.text(" [AFK]").style(nonItalic(NamedTextColor.GRAY));

    /* If you wanna welcome someone, just welcome them, this is dumb
    public static String[] RANDOM_AFK_GREETINGS = {
            "Wb!", "wb", "wb!", "welcome back", "Welcome back!", "hey"
    };

    public static String[] RANDOM_GREETINGS = {
            "hey", "hi", "wb", "hello", "hiya", "heyo", "yo",           //Incorrect spelling
            "Hey!", "Hi!", "Welcome back!", "Hello!", "Heyo!", "Yo!",   //Grammar good :D
            "Hey %s!", "Hello %s!", "Hi %s!"                            //Formattable
    };*/

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

    public static Component formatChat(Player source, Component displayName, Component message, Audience audience){
        CrownUser user = UserManager.getUser(source);

        String strMessage = ChatUtils.getString(message);
        boolean inSenateWorld = source.getWorld().equals(Worlds.SENATE);
        boolean staffChat = StaffChat.toggledPlayers.contains(source);

        if(source.hasPermission(Permissions.DONATOR_2) || inSenateWorld || staffChat) strMessage = translateHexCodes(strMessage);
        if(source.hasPermission(Permissions.DONATOR_3) || inSenateWorld || staffChat) strMessage = CrownCore.getEmotes().format(strMessage, source, true);

        strMessage = checkUppercase(source, strMessage);
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

    public static String checkUppercase(CommandSender source, String input){
        if(input.length() > 8 && !source.hasPermission(Permissions.IGNORE_CHAT_CASE)){
            int upCastCharNumber = 0;
            for(int i = 0; i < input.length(); i++){
                if(Character.isUpperCase(input.charAt(i))) upCastCharNumber++;
            }
            if(upCastCharNumber > (input.length()/2)) {
                input = StringUtils.capitalize(input.toLowerCase());
                input += "!";

                if(!Cooldown.contains(source, "uppercase_warning")){
                    source.sendMessage(Component.translatable("user.allCaps").color(NamedTextColor.GRAY));
                    Cooldown.add(source, "uppercase_warning", 1800); //1 and a half mins
                }
            }
        }

        return input;
    }

    public static Component joinMessage(CrownUser user){
        return Component.translatable("multiplayer.player.joined", user.nickDisplayName().color(getJoinColor(user)))
                //.hoverEvent(Component.text("Click to say hello!"))
                //.clickEvent(ClickEvent.runCommand(hello(user.getNickOrName())))
                .color(NamedTextColor.YELLOW);
    }

    /*private static String hello(String nickOrName){
        int randomIndex = CrownUtils.getRandomNumberInRange(0, RANDOM_GREETINGS.length-1);
        return String.format(RANDOM_GREETINGS[randomIndex], nickOrName);
    }

    public static ClickEvent unAfkClickEvent(){
        return ClickEvent.runCommand(RANDOM_AFK_GREETINGS[CrownUtils.getRandomNumberInRange(0, RANDOM_AFK_GREETINGS.length-1)]);
    }*/

    public static Component newNameJoinMessage(CrownUser user1){
        FtcUser user = (FtcUser) user1;

        return Component.translatable("multiplayer.player.joined.renamed",
                user.nickDisplayName().color(getJoinColor(user)),
                Component.text(user.previousNames.get(user.previousNames.size()-1))
        )
                //.hoverEvent(Component.text("Click to say hello!"))
                //.clickEvent(ClickEvent.runCommand("Hey " + user.getNickOrName() + '!'))
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
        return formatString(msg, sender, false);
    }

    public static Component formatString(String msg, @Nullable CommandSender sender, boolean ignorePerms){
        msg = CrownCore.getEmotes().format(msg, sender, ignorePerms);

        return ChatUtils.convertString(msg, ignorePerms || sender.hasPermission(Permissions.DONATOR_2));
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

    public static Component itemMessage(ItemStack itemStack, int amount){
        Validate.notNull(itemStack);
        return Component.text()
                .hoverEvent(itemStack)
                .append(Component.text(amount))
                .append(Component.text(" "))
                .append(itemName(itemStack))
                .build();
    }

    public static Component itemMessage(ItemStack item) {
        Validate.notNull(item);
        return itemMessage(item, item.getAmount());
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
        if(days != 0) stringBuilder.append(days).append(" day").append(s(days)).append(", ");
        if(hours != 0) stringBuilder.append(hours).append(" hour").append(s(hours)).append(", ");
        if(minutes != 0) stringBuilder.append(minutes).append(" minute").append(s(minutes)).append(" and ");
        if(seconds != 0) stringBuilder.append(seconds).append(" second").append(s(seconds));
        else if(millis < 1000 && millis > -1000) stringBuilder.append(millis).append(" millisecond").append(s(millis));

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

    public static Component entityDisplayName(Entity entity){
        Component name = entity.customName() == null ? translatableEntityName(entity.getType()) : entity.customName();
        assert name != null;

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getUniqueId().toString());
        if(team != null) name = formatForTeam(name, team);

        return name;
    }

    public static TranslatableComponent translatableEntityName(EntityType entity){
        return Component.translatable(Bukkit.getUnsafe().getTranslationKey(entity));
    }

    public static Component formatForTeam(Component initialName, Team team){
        TextColor color;
        try { //tEaM cOloRs mUsT hAvE hEX vAlUeS, what a fucking retarded place to throw an exception, in a getter
            color = team.color();
        } catch (IllegalStateException e){
            color = NamedTextColor.WHITE;
        }

        Component prefix = Component.empty();
        Component suffix = Component.empty();

        try {
            prefix = team.prefix().append(Component.space());
        } catch (IllegalStateException ignored) {}

        try {
            suffix = Component.space().append(team.suffix());
        } catch (IllegalStateException ignored) {}

        return Component.text()
                .append(prefix)
                .append(initialName.color(color))
                .append(suffix)
                .build();
    }

    public static Component sourceDisplayName(CommandSource source){
        if(source.isPlayer()) return UserManager.getUser(source.textName()).nickDisplayName();
        return source.displayName();
    }

    public static Style nonItalic(TextColor color){
        return Style.style(color).decoration(TextDecoration.ITALIC, false);
    }
}
