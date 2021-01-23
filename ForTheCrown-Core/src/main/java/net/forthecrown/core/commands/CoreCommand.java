package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.files.SignShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CoreCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) return false;

        switch (args[0]){
            case "save":
                if(args.length < 2){
                    FtcCore.saveFTC();
                    sender.sendMessage("FTC-Core has been saved");
                    return true;
                }

                switch (args[1]){
                    default: return false;

                    case "balances":
                        FtcCore.getBalances().save();
                        sender.sendMessage("Balances have been saved");
                        return true;

                    case "announcer":
                        FtcCore.getAnnouncer().save();
                        sender.sendMessage("The AutoAnnouncer has been saved");
                        return true;

                    case "users":
                        for (FtcUser user : FtcUser.loadedData){ user.save(); }
                        sender.sendMessage("All loaded user data's have been saved");
                        return true;

                    case "signshops":
                        for(SignShop shop : SignShop.loadedShops){ shop.save(); }
                        sender.sendMessage("All loaded SignShops have been saved");
                        return true;
                }

            case "reload":
                if(args.length < 2){
                    FtcCore.reloadFTC();
                    sender.sendMessage("FTC-Core has been reloaded");
                    return true;
                }

                switch (args[1]){
                    default: return false;

                    case "balances":
                        FtcCore.getBalances().reload();
                        sender.sendMessage("Balances have been reloaded");
                        return true;

                    case "announcer":
                        FtcCore.getAnnouncer().reload();
                        sender.sendMessage("The AutoAnnouncer has been reloaded");
                        return true;

                    case "users":
                        for (FtcUser user : FtcUser.loadedData){ user.reload(); }
                        sender.sendMessage("All loaded user data's have been reloaded");
                        return true;

                    case "signshops":
                        for(SignShop shop : SignShop.loadedShops){ shop.reload(); }
                        sender.sendMessage("All loaded SignShops have been reloaded");
                        return true;
                }

            case "announcer":
                if(args.length < 2) return false;

                if(args[1].contains("start")){
                    FtcCore.getAnnouncer().startAnnouncer();
                    sender.sendMessage("Announcer has been started!");
                    return true;
                } else {
                    FtcCore.getAnnouncer().stopAnnouncer();
                    sender.sendMessage("Announcer has been stopped!");
                    return true;
                }

            case "user":
                if(args.length < 3) return false;

                FtcUser user;
                try {
                    user = FtcCore.getUser(FtcCore.getOffOnUUID(args[1]));
                } catch (NullPointerException e){ return false; }

                switch (args[2]){
                    case "addpet":
                        if(args.length < 4) return false;
                        List<String> tempList = user.getPets();
                        tempList.add(args[3]);
                        user.setPets(tempList);
                        return true;

                    case "rank":
                        if(args.length < 4 || args[3].contains("list")){
                            sender.sendMessage(args[1] + " has the following ranks: " + user.getAvailableRanks().toString());
                            return true;
                        }

                        if(args.length < 5) return false;

                        Rank rank;
                        try {
                            rank = Rank.valueOf(args[4].toUpperCase());
                        } catch (Exception e) { return false; }

                        if(args[3].contains("add")){
                            user.addRank(rank);
                            sender.sendMessage(args[4] + " was added to " + args[1]);
                        } else if (args[3].contains("remove")){
                            user.removeRank(rank);
                            sender.sendMessage(args[4] + " was removed from " + args[1]);
                        } else if(args[3].contains("set")){
                            user.setRank(rank, true);
                            sender.sendMessage(args[1] + " is now a " + rank.toString());
                        } else return false;
                        return true;

                    case "makebaron":
                        if(args.length < 4 || args[3].contains("true")) {
                            user.setBaron(true);
                            sender.sendMessage(args[1] + " was made into a baron!");
                            return true;
                        }

                        if(args[3].contains("false")){
                            user.setBaron(false);
                            sender.sendMessage(args[1] + " is no longer a baron");
                            return true;
                        }
                        break;

                    case "canswapbranch":
                        if(args.length < 4 || args[3].contains("true")){
                            user.setCanSwapBranch(true);
                            sender.sendMessage(args[1] + " is now allowed to swap branches");
                            return true;
                        }

                        if(args[3].contains("false")){
                            user.setCanSwapBranch(false);
                            sender.sendMessage(args[1] + " is no longer allowed to swap branches");
                            return true;
                        }
                        break;

                    case "branch":
                        if(args.length < 4){
                            sender.sendMessage(args[1] + "'s branch is: " + user.getBranch().toString());
                            return true;
                        }
                        try {
                            user.setBranch(Branch.valueOf(args[3].toUpperCase()));
                            sender.sendMessage(args[1] + " is now a " + user.getBranch().toString());
                        } catch (Exception e){ return false; }
                        break;

                    case "addgems":
                        if(args.length < 4) return false;

                        int gems;
                        try {
                            gems = Integer.parseInt(args[3]);
                        } catch (NullPointerException e) { return false; }

                        user.addGems(gems);
                        sender.sendMessage("Added " + gems + " gems to " + args[1]);

                }


            default: return false;
        }
    }
}

//TODO getters and setters for everything in the FtcUser class
/* Needed args:
 * addpet, rank <remove | add | list>, makebaron, canswapbranch [set], branch <get | set>,
 */
