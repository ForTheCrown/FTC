package net.forthecrown.commands;

import com.google.common.base.Joiner;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.data.Rank;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Statistic;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.Date;

public class CommandProfile extends FtcCommand {

    public CommandProfile(){
        super("profile", Crown.inst());

        setAliases("playerprofile", "gameprofile");
        setDescription("Displays a user's profile information");
        setPermission(Permissions.PROFILE);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Shows some basic info about a user.
     *
     * Valid usages of command:
     * - /profile
     * - /profile [player]
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    sendProfileMessage(user, c.getSource());
                    return 0;
                })
                .then(argument("player", UserArgument.user())

                        .executes(c ->{
                            CrownUser profile = UserArgument.getUser(c, "player");
                            sendProfileMessage(profile, c.getSource());
                            return 0;
                        })
                );
    }

    public void sendProfileMessage(CrownUser profile, CommandSource sender) throws RoyalCommandException {
        boolean self = profile.getName().equalsIgnoreCase(sender.textName());

        //If the profile isn't public and you don't have bypass permissions
        if(!self && !profile.isProfilePublic()){
            if(!sender.hasPermission(Permissions.PROFILE_BYPASS)) throw FtcExceptionProvider.translatable("commands.profileNotPublic", profile.nickDisplayName());
        }

        sender.sendMessage(profileComponent(profile, sender, self));
    }

    public static Component profileComponent(CrownUser profile, CommandSource sender, boolean self) {
        String header = "&6&m----&e " + (self ? "Your" : (profile.getNickOrName() + "'s")) + " player profile &6&m----";
        long playTime = playTime(profile);

        Objective pp = profile.getScoreboard().getObjective("PiratePoints");
        Score score = pp.getScore(profile.getName());

        Objective crown = profile.getScoreboard().getObjective("crown");
        Score crownScr = crown.getScore(profile.getName());

        Component scrDisplay = Component.text(ComVars.isEventTimed() ? EventTimer.getTimerCounter(crownScr.getScore()).toString() : crownScr.getScore() + "");
        Component marriedTo = marriedMessage(profile);

        Component afkMessage = profile.getAfkReason() == null || !profile.isAfk() ? null : Component.text(profile.getAfkReason());

        //Footer size roughly lines up with header size
        String footer = "-".repeat((int) (header.length() * 0.75));

        return Component.text()
                .append(ChatUtils.convertString(header))

                .append(line("Branch", profile.getFaction().getName(), profile.getFaction() != Faction.DEFAULT))
                .append(line("Rank", profile.getRank().prefix(), profile.getRank() != Rank.DEFAULT))

                .append(line("AFK", afkMessage, afkMessage != null))

                .append(line("Allowed to swap branches in", timeThing(profile), !profile.canSwapFaction() && (self || sender.hasPermission(Permissions.PROFILE_BYPASS))))
                .append(line("Play time", FtcFormatter.decimalizeNumber(playTime) + " hours", playTime > 0))
                .append(line("Married to", marriedTo, marriedTo != null))

                .append(line("Pirate Points", score.getScore() + "", score.isScoreSet() && score.getScore() != 0))
                .append(line("Crown score", scrDisplay, crownScr.isScoreSet() && crownScr.getScore() > 0 && ComVars.isEventActive()))

                .append(line("Gems", profile.getGems() + "", profile.getGems() > 0))
                .append(line("Balance", FtcFormatter.rhines(Crown.getEconomy().get(profile.getUniqueId())), true))

                .append(adminInfo(sender, profile))

                .append(Component.newline())
                .append(Component.text(footer).color(NamedTextColor.GOLD).decorate(TextDecoration.STRIKETHROUGH))
                .build();
    }

    private static long playTime(CrownUser profile){
        return profile.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60;
    }

    private static String timeThing(CrownUser profile){
        long timeUntil = profile.getNextAllowedBranchSwap() - System.currentTimeMillis();
        return FtcFormatter.convertMillisIntoTime(timeUntil);
    }

    private static Component line(String field, String value, boolean check){
        return line(field, check ? Component.text(value) : null, check);
    }

    private static Component line(String field, Component value, boolean check){
        return check ? Component.text()
                .append(Component.newline())
                .append(Component.text(field + ": ").color(NamedTextColor.YELLOW))
                .append(value)
                .build() : Component.empty();
    }

    private static Component adminInfo(CommandSource sender, CrownUser profile1){
        if(!sender.hasPermission(Permissions.PROFILE_BYPASS)) return Component.empty();
        FtcUser profile = (FtcUser) profile1;

        PunishmentManager list = Crown.getPunishmentManager();
        PunishmentEntry entry = list.getEntry(profile.getUniqueId());

        Component locMessage = profile.getLocation() == null ? null : FtcFormatter.clickableLocationMessage(profile.getLocation(), true);
        Component punishmentDisplay = entry == null ? Component.empty() : Component.newline().append(entry.display(false));
        Component marriageCooldown = marriageCooldown(profile.interactions);
        Component ignored = profile.interactions.blocked.isEmpty() ?
                null :
                Component.text(ListUtils.join(profile.interactions.blocked, id -> UserManager.getUser(id).getName()));

        Component separated = profile.interactions.separated.isEmpty() ?
                null :
                Component.text(ListUtils.join(profile.interactions.separated, id -> UserManager.getUser(id).getName()));

        Component ip = profile.ip == null ? null : Component.text(profile.ip);

        Component lastNames = ListUtils.isNullOrEmpty(profile.previousNames) ? null : Component.text(
                Joiner.on(' ').join(profile.previousNames)
        );

        Markets markets = Crown.getMarkets();
        Component marketDisplay = profile.marketOwnership.currentlyOwnsShop() ?
                MarketDisplay.displayName(markets.get(profile.getUniqueId())) :
                null;

        return Component.newline()
                .append(Component.text("\nAdmin Info:").color(NamedTextColor.YELLOW))
                .append(line(" Ranks", ListUtils.join(profile.getAvailableRanks(), r -> r.name().toLowerCase()), true))

                .append(Component.newline())
                .append(timeSinceOnlineOrOnlineTime(profile))
                .append(line(" IP", ip, ip != null))
                .append(line(" PreviousNames", lastNames, lastNames != null))
                .append(line(" Ignored: ", ignored, ignored != null))
                .append(line(" Separated", separated, separated != null))

                .append(line(" OwnedShop", marketDisplay, marketDisplay != null))

                .append(line(" MarriageCooldown", marriageCooldown, marriageCooldown != null))
                .append(line(profile.isOnline() ? " Location" : " Last seen", locMessage, locMessage != null))

                .append(punishmentDisplay);
    }

    private static Component marriedMessage(CrownUser user){
        if(user.getInteractions().getSpouse() == null) return null;

        return user.getInteractions().spouseUser().nickDisplayName();
    }

    private static Component marriageCooldown(UserInteractions interactions){
        if(interactions.canChangeMarriageStatus()) return null;

        long time = interactions.getLastMarriageChange();
        return Component.text(FtcFormatter.getDateFromMillis(time))
                .hoverEvent(Component.text(new Date(time).toString()));
    }

    private static Component timeSinceOnlineOrOnlineTime(CrownUser user){
        if(user.isOnline()){
            return Component.text(" Has been online for ")
                    .color(NamedTextColor.YELLOW)
                    .append(FtcFormatter.millisIntoTime(System.currentTimeMillis() - user.getPlayer().getLastLogin()).color(NamedTextColor.WHITE));
        } else {
            return Component.text(" Has been offline for ")
                    .color(NamedTextColor.YELLOW)
                    .hoverEvent(Component.text(new Date(user.getOfflinePlayer().getLastLogin()).toString()))
                    .append(FtcFormatter.millisIntoTime(System.currentTimeMillis() - user.getOfflinePlayer().getLastLogin()).color(NamedTextColor.WHITE));
        }
    }
}
