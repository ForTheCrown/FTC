package net.forthecrown.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CrownPlugin extends JavaPlugin  {

    private final CrownCommandHandler handler;

    public CrownPlugin(){
        handler = new CrownCommandHandler();
    }

    public CrownCommandHandler getCommandHandler() {
        return handler;
    }

    //This method should only get called if a command has no set executor
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return handler.executeCommand(command, sender, label, args);
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
