package net.forthecrown.core.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
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
import net.kyori.adventure.translation.GlobalTranslator;
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
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for methods which take an input and format it into a Component
 */
public interface FtcFormatter {
    DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#.##"), format -> {
        format.setGroupingUsed(true);
        format.setGroupingSize(3);
    });

    Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    Component AFK_SUFFIX = Component.text(" [AFK]").style(nonItalic(NamedTextColor.GRAY));

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
    static String translateHexCodes(@NotNull String textToTranslate) {
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

    /**
     * Takes every word in a string like "hello world!" and turns it into "Hello World!"
     * @param str The input to capitalize
     * @return The capitalized result
     */
    static String capitalizeWords(String str){
        String[] words = str.split("\\s");
        StringBuilder capitalizeWord = new StringBuilder();

        for(String w: words){
            String first = w.substring(0,1);
            String afterfirst = w.substring(1);

            capitalizeWord
                    .append(first.toUpperCase())
                    .append(afterfirst)
                    .append(" ");
        }

        return capitalizeWord.toString().trim();
    }

    /**
     * Formats a chat message.
     * <p></p>
     * Parameters needed cuz FtcFormatter::formatChat is way easier to write than creating
     * a lambda lol.
     * @param source The source of the chat message
     * @param displayName Their display name, irrelevant since we use {@link CrownUser#nickDisplayName()}
     * @param message The message they sent
     * @param audience The viewer of the message, allows us to make the message different for each person
     * @return The formatted chat message
     */
    static Component formatChat(Player source, Component displayName, Component message, Audience audience){
        CrownUser user = UserManager.getUser(source);

        String strMessage = ChatUtils.getString(message);
        boolean inSenateWorld = source.getWorld().equals(Worlds.SENATE);
        boolean staffChat = StaffChat.toggledPlayers.contains(source);

        if(source.hasPermission(Permissions.DONATOR_2) || inSenateWorld || staffChat) strMessage = translateHexCodes(strMessage);
        if(source.hasPermission(Permissions.DONATOR_3) || inSenateWorld || staffChat) strMessage = Crown.getEmotes().format(strMessage, source, true);

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

    /**
     * Checks whether the given string is more than half uppercase, if it is, lower cases it
     * @param source The source of the input
     * @param input The input to check and potentially change
     * @return The modified input, or the just the input if the message was not more than 50% uppercase
     */
    static String checkUppercase(CommandSender source, String input){
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

    /**
     * Formats a normal join message
     * @param user The joining user
     * @return The join message
     */
    static Component joinMessage(CrownUser user){
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

    /**
     * Formats a join message with a new name
     * @param user1 The joining user
     * @return The formatted message
     */
    static Component newNameJoinMessage(CrownUser user1){
        FtcUser user = (FtcUser) user1;

        return Component.translatable("multiplayer.player.joined.renamed",
                user.nickDisplayName().color(getJoinColor(user)),
                Component.text(user.previousNames.get(user.previousNames.size()-1))
        )
                //.hoverEvent(Component.text("Click to say hello!"))
                //.clickEvent(ClickEvent.runCommand("Hey " + user.getNickOrName() + '!'))
                .color(NamedTextColor.YELLOW);
    }

    /**
     * Formats a simple leave message
     * @param user The leaving user
     * @return the formatted message
     */
    static Component leaveMessage(CrownUser user){
        return Component.translatable("multiplayer.player.left",
                NamedTextColor.YELLOW,
                user.nickDisplayName().color(getJoinColor(user))
        );
    }

    static Component banMessage(PunishmentRecord record){
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

    /**
     * Gets a color to use on the user's display name when they join
     * @param user The user to get the color for
     * @return The join color.
     */
    static TextColor getJoinColor(CrownUser user){
        Rank r = user.getHighestTierRank();

        if(r.getTier().shouldUseColor()) return r.getTier().color;
        return NamedTextColor.YELLOW;
    }

    /**
     * Formats the given input if the user has the {@link Permissions#DONATOR_2} permission
     * @param msg the input
     * @param sender The sender to check
     * @return The modified input, or the same if the user didn't meet the required permissions
     */
    static Component formatIfAllowed(String msg, CommandSender sender){
        return formatString(msg, sender, false);
    }

    /**
     * Formats the given input into a component by replacing emojis and translating ampersand color codes.
     * <p></p>
     * Will only format if ignorePerms is true, the source is null or if the source has the {@link Permissions#DONATOR_2} permission.
     * @param msg The input to format
     * @param sender The source of the input
     * @param ignorePerms Whether to ignore permissions when formatting
     * @return The formatted input, or the input itself if formatting checks were not passed
     */
    static Component formatString(String msg, @Nullable CommandSender sender, boolean ignorePerms){
        msg = Crown.getEmotes().format(msg, sender, ignorePerms);

        return ChatUtils.convertString(msg, ignorePerms || sender == null || sender.hasPermission(Permissions.DONATOR_2));
    }

    /**
     * Formats a gem message with translatable components.
     * <p></p>
     * Note: Does not work with item names, lores, signs or anything where the translatable component is not under
     * Paper's control.
     * @param amount The amount to format and decimalize
     * @return The formatted amount.
     */
    static Component queryGems(int amount){
        if(amount == 1 || amount == -1) return Component.translatable("user.gems.singular", Component.text(amount));
        return Component.translatable("user.gems.multiple", Component.text(decimalizeNumber(amount)));
    }

    static Component gems(int amount) {
        return GlobalTranslator.render(queryGems(amount), Locale.ROOT);
    }

    /**
     * Gets a date from milliseconds
     * <p></p>
     * This sucks ass, too vague of a result. Use {@link Date#toString()} for precision.
     * @param millis The millis
     * @return The date
     */
    static String getDateFromMillis(long millis){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);

        int day = c.get(Calendar.DAY_OF_MONTH);
        String s = day + "";

        DateFormatSymbols sF = DateFormatSymbols.getInstance();

        s += daySuffix(day);
        s += "of ";
        s += sF.getMonths()[c.get(Calendar.MONTH)];
        s += " " + c.get(Calendar.YEAR);

        return s;
    }

    private static String daySuffix(int day){
        if (day >= 11 && day <= 13) {
            return "th ";
        }

        return switch (day % 10) {
            case 1 -> "st ";
            case 2 -> "nd ";
            case 3 -> "rd ";
            default -> "th ";
        };
    }

    /**
     * Gets an items display name.
     * <p></p>
     * If the item has a custom name, returns that, otherwise it'll return a translatable component for
     * the item's type.
     * @param item The item to get the display name for
     * @return The item's display name
     */
    static Component itemDisplayName(ItemStack item){
        Validate.notNull(item, "Item was null");

        return item.displayName();
    }

    /**
     * Outdated, use {@link FtcFormatter#itemDisplayName(ItemStack)}
     * @param item The item to get the name for
     * @deprecated Use {@link FtcFormatter#itemDisplayName(ItemStack)}
     * @return The item's name, as a string
     */
    @Deprecated
    static String getItemNormalName(ItemStack item){
        return normalEnum(item.getType());
    }

    /**
     * Takes an enum input like OAK_SIGN and returns "Oak Sign"
     * @param anum The enum to normalize
     * @return The normalized input.
     */
    static String normalEnum(@NotNull Enum<?> anum){
        Validate.notNull(anum, "Provided Enum was null");
        return capitalizeWords(anum.toString().replaceAll("_", " ").toLowerCase());
    }

    /**
     * Decimalize a number, eg: 1000 -> "1,000"
     * @param number The number to decimalize.
     * @return The decimalized number.
     */
    static String decimalizeNumber(@NotNull Number number){
        Validate.notNull(number, "Number was null");
        return DECIMAL_FORMAT.format(number);
    }

    /**
     * Creates a pretty location message for easy readability.
     * <p></p>
     * Note: Does not use exact decimal cords, rather uses block cords
     * @param l The location to format for
     * @param includeWorld Whether to include the world's name in the message
     * @return The formatted easily readable location message
     */
    static Component prettyLocationMessage(Location l, boolean includeWorld){
        return Component.text("" +
                "X: " + l.getBlockX() + " " +
                "Y: " + l.getBlockY() + " " +
                "Z: " + l.getBlockZ() +
                (includeWorld ? " world: " + l.getWorld().getName() : ""));
    }

    /**
     * Creates a location message that when clicked teleports you to the location.
     * @see FtcFormatter#prettyLocationMessage(Location, boolean)
     * @param l The location to fomrat for
     * @param includeWorld Whether to include the world in the message
     * @return The formatted and clickable message
     */
    static Component clickableLocationMessage(Location l, boolean includeWorld){
        return prettyLocationMessage(l, includeWorld)
                .hoverEvent(Component.text("Click to teleport!"))
                .clickEvent(ClickEvent.runCommand("/tp_exact " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + " " + l.getPitch() + " " + l.getYaw() + " " + l.getWorld().getName()));
    }

    /**
     * Formats an item's name and amount into a message, eg: "12 Oak Sign".
     * <p></p>
     * If you wanna figure out how to pluralize this mess, have fun
     * @param itemStack The itemstack to format for
     * @param amount The amount to show
     * @return The formatted message
     */
    static Component itemAndAmount(ItemStack itemStack, int amount){
        Validate.notNull(itemStack);
        return Component.text()
                .hoverEvent(itemStack)
                .append(Component.text(amount))
                .append(Component.text(" "))
                .append(itemDisplayName(itemStack))
                .build();
    }

    /**
     * Same as {@link FtcFormatter#itemAndAmount(ItemStack, int)} except uses the item's amount
     * @param item The item to format for
     * @return The formatted message with the item's amount.
     */
    static Component itemAndAmount(ItemStack item) {
        Validate.notNull(item);
        return itemAndAmount(item, item.getAmount());
    }

    static String timeFromMillisMinusTime(long millis){
        return convertMillisIntoTime(millis - System.currentTimeMillis());
    }

    static String convertTicksIntoTime(long ticks){
        return convertMillisIntoTime(ticks * 50);
    }

    static String convertMillisIntoTime(long millis){
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

    static Component millisIntoTime(long millis){
        return Component.text(convertMillisIntoTime(millis));
    }

    private static String s(long l){
        return l == 1 ? "" : "s";
    }

    /**
     * Checks if the given nickname is allowed
     * @param nick The nickname to check for
     * @throws CommandSyntaxException If the nickname is invalid
     */
    static void checkNickAllowed(String nick) throws CommandSyntaxException {
        if(ComVars.getMaxNickLength() < nick.length()) throw FtcExceptionProvider.nickTooLong(nick.length());

        if(!ComVars.allowOtherPlayerNameNicks()){
            if(Bukkit.getOfflinePlayerIfCached(nick) != null) throw FtcExceptionProvider.create("Nickname cannot be the name of another player");
        }
    }

    static Component entityDisplayName(Entity entity){
        Component name = entity.customName() == null ? translatableEntityName(entity.getType()) : entity.customName();
        assert name != null;

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getUniqueId().toString());
        if(team != null) name = formatNameForTeam(name, team);

        return name;
    }

    static TranslatableComponent translatableEntityName(EntityType entity){
        return Component.translatable(Bukkit.getUnsafe().getTranslationKey(entity));
    }

    static Component formatNameForTeam(Component initialName, Team team){
        TextColor color;
        try { //tEaM cOloRs mUsT hAvE hEX vAlUeS, what a fucking retarded place to throw an exception, in a getter
            color = team.color();
        } catch (IllegalStateException e){
            color = null;
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

    static Component sourceDisplayName(CommandSource source){
        if(source.isPlayer()) return UserManager.getUser(source.textName()).nickDisplayName();
        return source.displayName();
    }

    static Style nonItalic(TextColor color){
        return Style.style(color).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a formatted currency messaage
     * @param amount The amount to format
     * @return The message, will look like: "1,000,000 Rhines"
     */
    static String getRhines(long amount){
        return decimalizeNumber(amount) + " Rhine" + FtcUtils.addAnS(amount);
    }

    /**
     * Same as {@link FtcFormatter#rhines(long)} except not translatable.
     * @param amount The amount to format for
     * @return The formatted message
     */
    static Component rhinesNonTrans(long amount) {
        return GlobalTranslator.render(rhines(amount), Locale.ROOT);
    }

    /**
     * Same thing as getFormatted but for components, is also translatable.
     * <p></p>
     * <b>WARNING</b>: Do not use in signs, entity names, item names or lores! Translatable Components need to be under the control
     * of Paper's Adventure API to be translated properly, and they cannot do that if the component is in the aforementioned
     * places.
     * <p></p>
     * If you need a Rhine component for non server-side translatability, use {@link FtcFormatter#rhinesNonTrans(long)}
     * @param amount The amount to format
     * @return A formatted, translatable, component
     */
    static Component rhines(long amount){
        return Component.text()
                .content(decimalizeNumber(amount) + " ")
                .append(Component.translatable("economy.currency." + (amount == 1 || amount == -1 ? "singular" : "multiple")))
                .build();
    }

    static Component piratePoints(int amount) {
        return Component.text()
                .content(decimalizeNumber(amount) + " ")
                .append(Component.translatable("pirates.pp." + (amount == 1 || amount == -1 ? "single" : "multiple")))
                .build();
    }
}