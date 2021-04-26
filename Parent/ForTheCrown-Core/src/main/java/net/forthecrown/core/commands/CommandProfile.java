package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.custom.UserType;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Statistic;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class CommandProfile extends CrownCommandBuilder {

    public CommandProfile(){
        super("profile", FtcCore.getInstance());

        new CommandProfilePublic(this);
        new CommandProfilePrivate(this);

        setAliases("playerprofile", "gameprofile");
        setUsage("&7Usage: &r/profile <public | private> \n&7Usage: &r/profile <player>");
        setDescription("Displays a user's profile information");

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
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    sendProfileMessage(user, user);
                    return 0;
                })
                .then(argument("player", UserType.user())
                        .suggests(UserType::suggest)

                        .executes(c ->{
                            CrownUser user = getUserSender(c);
                            CrownUser profile = UserType.getUser(c, "player");
                            sendProfileMessage(profile, user);
                            return 0;
                        })
                );
    }

    public void sendProfileMessage(CrownUser profile, CrownUser user){
        boolean self = profile.equals(user);

        //If the profile isn't public and you don't have bypass permissions
        if(!self && !profile.isProfilePublic()){
            if(!user.hasPermission(getPermission() + ".bypass")){
                user.sendMessage(profile.getName() + " profile is not public");
                return;
            }
        }

        String header = "&6&m---- &e" + (self ? "Your" : (profile.getName() + "'s")) + " player profile &6&m----";
        long playTime = playTime(profile);

        Objective pp = profile.getScoreboard().getObjective("PiratePoints");
        Score score = pp.getScore(profile.getName());

        Objective crown = profile.getScoreboard().getObjective("crown");
        Score crownScr = crown.getScore(profile.getName());

        //Footer size roughly lines up with header size
        StringBuilder footer = new StringBuilder();
        for (int i = 0; i < header.length()*0.75; i++){
            footer.append("-");
        }

        Component profileMessage = Component.text()
                .append(ComponentUtils.convertString(header))

                // args:
                // field (yellow part, has ": " added to it),
                // value (of the field),
                // check (required to actually show that line)
                .append(line("Branch", profile.getBranch().getName(), profile.getBranch() != Branch.DEFAULT))
                .append(line("Rank", profile.getRank().prefix(), profile.getRank() != Rank.DEFAULT))

                .append(line("Allowed to swap branches in", timeThing(profile), !profile.getCanSwapBranch() && (self || user.hasPermission(getPermission() + ".bypass"))))
                .append(line("Play time", CrownUtils.decimalizeNumber(playTime) + " hours", playTime > 0))

                .append(line("Pirate Points", score.getScore() + "", score.isScoreSet() && score.getScore() != 0))
                .append(line("Crown score", crownScr.getScore() + "", crownScr.isScoreSet() && crownScr.getScore() > 0))

                .append(line("Gems", profile.getGems() + "", profile.getGems() > 0))
                .append(line("Balance", FtcCore.getBalances().withCurrency(profile.getUniqueId()), true))

                .append(Component.newline())
                .append(Component.text(footer.toString()).color(NamedTextColor.GOLD).decorate(TextDecoration.STRIKETHROUGH))
                .build();

        user.sendMessage(profileMessage);
    }

    private long playTime(CrownUser profile){
        return profile.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60;
    }

    private String timeThing(CrownUser profile){
        long timeUntil = profile.getNextAllowedBranchSwap() - System.currentTimeMillis();
        return CrownUtils.convertMillisIntoTime(timeUntil);
    }

    private Component line(String field, String value, boolean check){
        return line(field, Component.text(value), check);
    }

    private Component line(String field, Component value, boolean check){
        return check ? Component.text()
                .append(Component.newline())
                .append(Component.text(field + ": ").color(NamedTextColor.YELLOW))
                .append(value)
                .build() : Component.empty();
    }
}
