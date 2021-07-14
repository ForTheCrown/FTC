package net.forthecrown.commands;

import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.economy.Balances;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.utils.FtcUtils;
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
        super("profile", CrownCore.inst());

        new CommandProfilePublic(this);
        new CommandProfilePrivate(this);

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
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    sendProfileMessage(user, user);
                    return 0;
                })
                .then(argument("player", UserType.user())

                        .executes(c ->{
                            CrownUser user = getUserSender(c);
                            CrownUser profile = UserType.getUser(c, "player");
                            sendProfileMessage(profile, user);
                            return 0;
                        })
                );
    }

    public void sendProfileMessage(CrownUser profile, CrownUser user) throws RoyalCommandException {
        boolean self = profile.equals(user);

        //If the profile isn't public and you don't have bypass permissions
        if(!self && !profile.isProfilePublic()){
            if(!user.hasPermission(Permissions.PROFILE_BYPASS)) throw FtcExceptionProvider.translatable("commands.profileNotPublic", profile.nickDisplayName());
        }

        user.sendMessage(profileComponent(profile, user));
    }

    public static Component profileComponent(CrownUser profile, CrownUser user) {
        boolean self = profile.equals(user);

        String header = "&6&m----&e " + (self ? "Your" : (profile.getNickOrName() + "'s")) + " player profile &6&m----";
        long playTime = playTime(profile);

        Objective pp = profile.getScoreboard().getObjective("PiratePoints");
        Score score = pp.getScore(profile.getName());

        Objective crown = profile.getScoreboard().getObjective("crown");
        Score crownScr = crown.getScore(profile.getName());

        Component scrDisplay = Component.text(CrownCore.isEventTimed() ? EventTimer.getTimerCounter(crownScr.getScore()).toString() : crownScr.getScore() + "");
        Component marriedTo = marriedMessage(profile);

        //Footer size roughly lines up with header size
        StringBuilder footer = new StringBuilder();
        for (int i = 0; i < header.length()*0.75; i++){
            footer.append("-");
        }

        return Component.text()
                .append(ChatUtils.convertString(header))

                // args:
                // field (yellow part, has ": " added to it),
                // value (of the field),
                // check (required to actually show that line)
                .append(line("Branch", profile.getBranch().getName(), profile.getBranch() != Branch.DEFAULT))
                .append(line("Rank", profile.getRank().prefix(), profile.getRank() != Rank.DEFAULT))

                .append(line("Allowed to swap branches in", timeThing(profile), !profile.canSwapBranch() && (self || user.hasPermission(Permissions.PROFILE_BYPASS))))
                .append(line("Play time", ChatFormatter.decimalizeNumber(playTime) + " hours", playTime > 0))
                .append(line("Married to", marriedTo, marriedTo != null))

                .append(line("Pirate Points", score.getScore() + "", score.isScoreSet() && score.getScore() != 0))
                .append(line("Crown score", scrDisplay, crownScr.isScoreSet() && crownScr.getScore() > 0 && CrownCore.isEventActive()))

                .append(line("Gems", profile.getGems() + "", profile.getGems() > 0))
                .append(line("Balance", Balances.formatted(CrownCore.getBalances().get(profile.getUniqueId())), true))

                .append(adminInfo(user, profile))

                .append(Component.newline())
                .append(Component.text(footer.toString()).color(NamedTextColor.GOLD).decorate(TextDecoration.STRIKETHROUGH))
                .build();
    }

    private static long playTime(CrownUser profile){
        return profile.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60;
    }

    private static String timeThing(CrownUser profile){
        long timeUntil = profile.getNextAllowedBranchSwap() - System.currentTimeMillis();
        return ChatFormatter.convertMillisIntoTime(timeUntil);
    }

    private static Component line(String field, String value, boolean check){
        return line(field, Component.text(value), check);
    }

    private static Component line(String field, Component value, boolean check){
        return check ? Component.text()
                .append(Component.newline())
                .append(Component.text(field + ": ").color(NamedTextColor.YELLOW))
                .append(value)
                .build() : Component.empty();
    }

    private static Component adminInfo(CrownUser sender, CrownUser profile1){
        if(!sender.hasPermission(Permissions.PROFILE_BYPASS)) return Component.empty();
        FtcUser profile = (FtcUser) profile1;

        PunishmentManager list = CrownCore.getPunishmentManager();
        PunishmentEntry entry = list.getEntry(profile.getUniqueId());

        Component locMessage = profile.getLocation() == null ? null : ChatFormatter.clickableLocationMessage(profile.getLocation(), true);
        Component punishmentDisplay = entry == null ? Component.empty() : Component.newline().append(entry.display(false));
        Component marriageCooldown = marriageCooldown(profile.interactions);
        Component ignored = profile.interactions.blocked.isEmpty() ?
                null :
                Component.text(ListUtils.join(profile.interactions.blocked, id -> UserManager.getUser(id).getName()));

        Component separated = profile.interactions.separated.isEmpty() ?
                null :
                Component.text(ListUtils.join(profile.interactions.separated, id -> UserManager.getUser(id).getName()));

        return Component.newline()
                .append(Component.text("\nAdmin Info:").color(NamedTextColor.YELLOW))
                .append(line(" Ranks", ListUtils.join(profile.getAvailableRanks(), r -> r.name().toLowerCase()), true))

                .append(Component.newline())
                .append(timeSinceOnlineOrOnlineTime(profile))
                .append(line(" IP", profile.ip, !FtcUtils.isNullOrBlank(profile.ip)))
                .append(line(" Ignored: ", ignored, ignored != null))
                .append(line(" Separated", separated, separated != null))

                .append(line(" MarriageCooldown", marriageCooldown, marriageCooldown != null))
                .append(line(profile.isOnline() ? " Location" : " Last seen", locMessage, locMessage != null))

                .append(punishmentDisplay);
    }

    private static Component marriedMessage(CrownUser user){
        if(user.getInteractions().getMarriedTo() == null) return null;

        return user.getInteractions().marriedToUser().nickDisplayName();
    }

    private static Component marriageCooldown(UserInteractions interactions){
        if(interactions.canChangeMarriageStatus()) return null;

        long time = interactions.getLastMarriageChange();
        return Component.text(ChatFormatter.getDateFromMillis(time))
                .hoverEvent(Component.text(new Date(time).toString()));
    }

    private static Component timeSinceOnlineOrOnlineTime(CrownUser user){
        if(user.isOnline()){
            return Component.text(" Has been online for ")
                    .color(NamedTextColor.YELLOW)
                    .append(ChatFormatter.millisIntoTime(System.currentTimeMillis() - user.getPlayer().getLastLogin()).color(NamedTextColor.WHITE));
        } else {
            return Component.text(" Has been offline for ")
                    .color(NamedTextColor.YELLOW)
                    .hoverEvent(Component.text(new Date(user.getOfflinePlayer().getLastLogin()).toString()))
                    .append(ChatFormatter.millisIntoTime(System.currentTimeMillis() - user.getOfflinePlayer().getLastLogin()).color(NamedTextColor.WHITE));
        }
    }
}
