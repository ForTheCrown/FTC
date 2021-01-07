package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.files.FtcUserData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FtcReloadCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * If changes were made and saved in the config(s) while the server is running,
     * use this to update the plugin without having to reload.
     *
     * Valid usages of command:
     * - /ftcreload
     * - /ftcreload <announcer | balances | userdata | economy>
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - FtcCore
     *
     * Main Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String reloadMsg = "Ftc-Core";
        if(args.length < 1) {
            FtcCore.reloadFTC(); //if no arg, reload entire plugin
        }
        else {
            switch (args[0]) {
                case "announcer":
                    FtcCore.getAnnouncer().reload();
                    reloadMsg = "AutoAnnouncer";
                    break;
                case "balances":
                    Economy.getBalances().reload();
                    reloadMsg = "Balances";
                    break;
                case "userdata":
                    for (FtcUserData data : FtcUserData.loadedData){
                        data.reload();
                    }
                    reloadMsg = "All currently loaded user data's";
                    break;
                case "economy":
                    //IDK yet
                    FtcCore.getEconomy().reloadEconomy();
                    reloadMsg = "The economy";
                    break;
                default:
                    sender.sendMessage("Invalid argument!");
                    return false;
            }
        }
        sender.sendMessage(FtcCore.getPrefix() + reloadMsg + " has been reloaded!");
        return true;
    }
}
