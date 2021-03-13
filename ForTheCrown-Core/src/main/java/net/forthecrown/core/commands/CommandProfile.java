package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
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
     * - /profile [player]
     *
     * Main Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
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
        String name = profile.getName() + "'s";
        if(profile.equals(user)) name = "Your";

        if(!name.equals("Your") && !profile.isProfilePublic()){
            if(!user.hasPermission(getPermission() + ".bypass")){
                user.sendMessage(name + " profile is not public");
                return;
            }
        }

        Balances bals = FtcCore.getBalances();

        user.sendMessage("&6---- &e" + name + " player profile &6----");
        if(profile.getBranch() != Branch.DEFAULT) user.sendMessage("&eBranch: &r" + profile.getBranch().getName());
        if(profile.getRank() != Rank.DEFAULT) user.sendMessage("&eRank: &r" + profile.getRank().getPrefix());
        if(!profile.getCanSwapBranch() && (name.equals("Your") || user.hasPermission(getPermission() + ".bypass") )){
            long timeUntil = profile.getNextAllowedBranchSwap() - System.currentTimeMillis();
            String time = CrownUtils.convertMillisIntoTime(timeUntil);

            user.sendMessage("&eAllowed to swap branches in: &r" + time);
        }

        Objective pp = profile.getScoreboard().getObjective("PiratePoints");
        Score score = pp.getScore(profile.getName());
        if(score.isScoreSet() && score.getScore() > 0) user.sendMessage("&ePirate points: &r" + score.getScore());

        Objective crown = profile.getScoreboard().getObjective("crown");
        Score crownScr = crown.getScore(profile.getName());
        if(crownScr.isScoreSet() && crownScr.getScore() > 0) user.sendMessage("&eCrown score: &r" + crownScr.getScore());

        //user.sendMessage("&eFirst joined: &r" + CrownUtils.getTimeFromMillis(profile.getFirstJoin()));

        if(profile.getGems() > 0) user.sendMessage("&eGems: &r" + profile.getGems());
        user.sendMessage("&eBalance: &r" + bals.getDecimalized(profile.getBase()) + " Rhines");
        user.sendMessage("&6--------------------------");
    }
}
