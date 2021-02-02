package net.forthecrown.core;

import net.forthecrown.core.files.FtcUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public abstract class CrownPlugin extends JavaPlugin  {

    private static final CrownCommandHandler handler = new CrownCommandHandler();
    //private static final CrownBlackMarket blackMarket = new CrownBlackMarket();

    public CrownPlugin(){
    }

    public CrownCommandHandler getCommandHandler() {
        return handler;
    }

    public void saveCore(){

    }

    public void reloadCore(){

    }

    //This method should only get called if a command has no set executor
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(handler.getRegisteredCommands().containsKey(command.getName())) return handler.executeCommand(command, sender, label, args);
        return true;
    }

    public void registerCommand(String command, CrownCommandExecutor executor){
        handler.registerCommand(command, executor);
    }

    public void registertabCompleter(String command, TabCompleter completer){
        getCommand(command).setTabCompleter(completer);
    }

    public void registerCommand(String command, CrownCommandExecutor executor, TabCompleter completer){
        handler.registerCommand(command, executor, completer);
    }

    public Set<FtcUser> getOnlineUsers(){
        return FtcUser.loadedData;
    }

    /*
    public static CrownUser getUser(OfflinePlayer player){
        return getUser(player.getUniqueId());
    }
    public static CrownUser getUser(Player player) {
        return getUser(player.getUniqueId());
    }
    public static CrownUser getUser(UUID base){
        for (FtcUser user : FtcUser.loadedData){
            if(user.getBase() == base) return user;
        }
        return null;
    }

     */
}
