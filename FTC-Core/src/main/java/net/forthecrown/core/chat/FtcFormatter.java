package net.forthecrown.core.chat;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.RankTier;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
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
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy HH:mm z");

    Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    TextComponent AFK_SUFFIX = Component.text(" [AFK]").style(nonItalic(NamedTextColor.GRAY));

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
    static String formatColorCodes(@NotNull String textToTranslate) {
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

                //1 and a half minute cooldown
                if(!Cooldown.containsOrAdd(source, "uppercase_warning", 1800)){
                    source.sendMessage(Component.translatable("user.allCaps").color(NamedTextColor.GRAY));
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
    static TranslatableComponent joinMessage(CrownUser user){
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
    static TranslatableComponent newNameJoinMessage(CrownUser user1){
        FtcUser user = (FtcUser) user1;

        return Component.translatable("multiplayer.player.joined.renamed",
                user.displayName().color(getJoinColor(user)),
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
    static TranslatableComponent leaveMessage(CrownUser user){
        return Component.translatable("multiplayer.player.left",
                NamedTextColor.YELLOW,
                user.nickDisplayName().color(getJoinColor(user))
        );
    }

    /**
     * Gets a color to use on the user's display name when they join
     * @param user The user to get the color for
     * @return The join color.
     */
    static TextColor getJoinColor(CrownUser user){
        RankTier r = user.getRankTier();

        if(r.shouldUseColor()) return r.color;
        return NamedTextColor.YELLOW;
    }

    /**
     * Formats the given input if the user has the {@link Permissions#DONATOR_2} permission
     * @param msg the input
     * @param sender The sender to check
     * @return The modified input, or the same if the user didn't meet the required permissions
     */
    static TextComponent formatIfAllowed(String msg, CommandSender sender){
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
    static TextComponent formatString(String msg, @Nullable CommandSender sender, boolean ignorePerms){
        msg = Crown.getEmotes().format(msg, sender, ignorePerms);

        return ChatUtils.convertString(msg, ignorePerms || sender == null || sender.hasPermission(Permissions.DONATOR_2));
    }

    static TextComponent formatString(String message) {
        return formatString(message, null, true);
    }

    /**
     * Creates a text with a gradient color
     * @param input The input
     * @param start The starting color, on the left
     * @param end The end color, on the right
     * @return The text colored as a gradient
     */
    static TextComponent gradientText(String input, TextColor start, TextColor end) {
        Validate.isTrue(!FtcUtils.isNullOrBlank(input), "Given string was null or blank");

        int length = input.length();
        char[] chars = input.toCharArray();

        //Not enough space for gradient
        if(length == 1) return Component.text(input).color(start);

        //Not enough space for gradient
        if(length == 2) {
            return Component.text(chars[0])
                    .color(start)
                    .append(Component.text(chars[1]).color(end));
        }

        //Step taken between each letter
        int rStep = (end.red() - start.red()) / length;
        int gStep = (end.green() - start.green()) / length;
        int bStep = (end.blue() - start.blue()) / length;

        TextComponent.Builder builder = Component.text()
                .append(Component.text(chars[0]).color(start));

        //Create text
        for (int i = 1; i < length - 1; i++) {
            int r = start.red() + (i * rStep);
            int g = start.green() + (i * gStep);
            int b = start.blue() + (i * bStep);

            builder.append(Component.text(chars[i]).color(TextColor.color(r, g, b)));
        }

        return builder
                .append(Component.text(chars[length - 1]).color(end))
                .build();
    }

    /**
     * Formats a gem message with translatable components.
     * <p></p>
     * Note: Does not work with item names, lores, signs or anything where the translatable component is not under
     * Paper's control.
     * @param amount The amount to format and decimalize
     * @return The formatted amount.
     */
    static TranslatableComponent gems(int amount){
        if(amount == 1 || amount == -1) return Component.translatable("user.gems.singular", Component.text(amount));
        return Component.translatable("user.gems.multiple", Component.text(decimalizeNumber(amount)));
    }

    static Component gemsNonTrans(int amount) {
        return GlobalTranslator.render(gems(amount), Locale.ROOT);
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

        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        Component hoverName = ChatUtils.vanillaToAdventure(nms.getHoverName());

        if(nms.hasCustomHoverName() && hoverName.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            hoverName = hoverName.decorate(TextDecoration.ITALIC);
        }

        return hoverName
                .hoverEvent(item);
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
    static TextComponent prettyLocationMessage(Location l, boolean includeWorld){
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
    static TextComponent clickableLocationMessage(Location l, boolean includeWorld){
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
    static TextComponent itemAndAmount(ItemStack itemStack, int amount){
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
    static TextComponent itemAndAmount(ItemStack item) {
        Validate.notNull(item);
        return itemAndAmount(item, item.getAmount());
    }

    static Component displayName(Entity entity){
        Component name = entity.customName() == null ? translatableEntityName(entity.getType()) : entity.customName();
        assert name != null;

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getUniqueId().toString());
        if(team != null) name = formatNameForTeam(name, team);

        return name;
    }

    static TranslatableComponent translatableEntityName(EntityType entity){
        return Component.translatable(Bukkit.getUnsafe().getTranslationKey(entity));
    }

    static TextComponent formatNameForTeam(Component initialName, Team team){
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

    static Style nonItalic() {
        return Style.style().decoration(TextDecoration.ITALIC, false).build();
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
        return rhinesInLang(amount, Locale.ROOT);
    }

    /**
     * Same as {@link FtcFormatter#rhines(long)} except in the given lang
     * @param amount The amount to format for
     * @param locale The locale to translate to
     * @return The formatted message
     */
    static Component rhinesInLang(long amount, Locale locale) {
        return GlobalTranslator.render(rhines(amount), locale);
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
    static TextComponent rhines(long amount){
        return Component.text()
                .content(decimalizeNumber(amount) + " ")
                .append(Component.translatable("economy.currency." + (amount == 1 || amount == -1 ? "singular" : "multiple")))
                .build();
    }

    static Component formatDate(long time) {
        return formatDate(new Date(time));
    }

    static Component formatDate(Date date) {
        return Component.text(DATE_FORMAT.format(date));
    }
}