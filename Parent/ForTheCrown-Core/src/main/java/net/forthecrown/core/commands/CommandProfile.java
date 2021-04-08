package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.utils.CrownUtils;
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
     * Main Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    sendProfileMessage(user, user);
                    return 0;
                })
                .then(argument("player", StringArgumentType.word())
                        .suggests((c, b) -> UserType.listSuggestions(b))

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

        //Header
        String header = "&6---- &e" + (self ? "Your" : (profile.getName() + "'s")) + " player profile &6----";
        user.sendMessage(header);

        //Branch
        if(profile.getBranch() != Branch.DEFAULT) user.sendMessage("&eBranch: &r" + profile.getBranch().getName());
        //Rank
        if(profile.getRank() != Rank.DEFAULT) user.sendMessage("&eRank: &r" + profile.getRank().getPrefix());

        //Branch spawn time thing
        if(!profile.getCanSwapBranch() && (self || user.hasPermission(getPermission() + ".bypass") )){
            long timeUntil = profile.getNextAllowedBranchSwap() - System.currentTimeMillis();
            String time = CrownUtils.convertMillisIntoTime(timeUntil);

            user.sendMessage("&eAllowed to swap branches in: &r" + time);
        }

        //Play time
        user.sendMessage("&ePlay time: &r" + CrownUtils.decimalizeNumber(profile.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60) + " hours");

        //Pirate points
        Objective pp = profile.getScoreboard().getObjective("PiratePoints");
        Score score = pp.getScore(profile.getName());
        if(score.isScoreSet() && score.getScore() > 0) user.sendMessage("&ePirate points: &r" + score.getScore());

        //Crown score objective
        Objective crown = profile.getScoreboard().getObjective("crown");
        Score crownScr = crown.getScore(profile.getName());
        if(crownScr.isScoreSet() && crownScr.getScore() > 0) user.sendMessage("&eCrown score: &r" + crownScr.getScore());

        //gems and balance
        if(profile.getGems() > 0) user.sendMessage("&eGems: &r" + CrownUtils.decimalizeNumber(profile.getGems()));
        user.sendMessage("&eBalance: &r" + FtcCore.getBalances().getDecimalized(profile.getBase()) + " Rhines");

        //Footer size roughly lines up with header size
        StringBuilder footer = new StringBuilder("&6");
        for (int i = 0; i < header.length()*0.75; i++){
            footer.append("-");
        }

        user.sendMessage(footer.toString());
    }
}
