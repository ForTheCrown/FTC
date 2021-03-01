package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.UUID;

public class ProfileCommand extends CrownCommandBuilder {

    public ProfileCommand(){
        super("profile", FtcCore.getInstance());

        setAliases("playerprofile", "gameprofile", "profilepublic", "profileprivate");
        setUsage("&7Usage: &r/profile <public | private> \n&7Usage: &r/profile <player>");
        setDescription("Displays a user's profile information");

        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    sendProfileMessage(user, user);
                    return 0;
                })
                .then(argument("player", StringArgumentType.word())
                        .suggests((c, b) -> getPlayerList(b).buildFuture())

                        .executes(c ->{
                            CrownUser user = getUserSender(c);
                            String playerName = c.getArgument("player", String.class);
                            UUID id = getUUID(playerName);
                            CrownUser profile = FtcCore.getUser(id);

                            sendProfileMessage(profile, user);
                            return 0;
                        })
                );
    }

   /* @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        CrownUser user = FtcCore.getUser(((Player) sender).getUniqueId());

        if(label.equalsIgnoreCase("profilepublic")){
            user.setProfilePublic(true);
            user.sendMessage("&7Others can now see your profile.");
            return true;
        }

        if(label.equalsIgnoreCase("profileprivate")){
            user.setProfilePublic(false);
            user.sendMessage("&7Others can no longer see your profile.");
            return true;
        }

        if(args.length < 1){
            sendProfileMessage(user, user);
            return true;
        }

        UUID id = FtcCore.getOffOnUUID(args[0]);
        if(id == null) throw new InvalidPlayerInArgument(sender, args[0]);

        CrownUser profile = FtcCore.getUser(id);
        sendProfileMessage(profile, user);

        return true;
    }*/

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
        if(score.isScoreSet()) user.sendMessage("&ePirate points: &r" + score.getScore());

        Objective crown = profile.getScoreboard().getObjective("crown");
        Score crownScr = crown.getScore(profile.getName());
        if(crownScr.isScoreSet()) user.sendMessage("&eCrown score: &r" + crownScr.getScore());

        user.sendMessage("&eGems: &r" + profile.getGems());
        user.sendMessage("&eBalance: &r" + bals.getDecimalizedBalance(profile.getBase()) + " Rhines");
        user.sendMessage("&6--------------------------");
    }
}
