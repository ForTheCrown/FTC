package net.forthecrown.core;

import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.*;

import java.util.HashMap;
import java.util.Map;

public class CrownCommandHandler {

    @Deprecated
    private static final Map<String, CrownCommandExecutor> registeredCommands = new HashMap<>();

    public void registerCommand(Command command, CrownCommandExecutor executor){
        registeredCommands.put(command.getName(), executor);
    }

    public void registerCommand(String command, CrownCommandExecutor executor){
        registeredCommands.put(command, executor);
    }

    public void registerTabCompleter(String command, TabCompleter tabCompleter){
        FtcCore.getInstance().getServer().getPluginCommand(command).setTabCompleter(tabCompleter);
    }

    public void registerCommand(String command, CrownCommandExecutor executor, TabCompleter completer){
        registeredCommands.put(command, executor);
        registerTabCompleter(command, completer);
    }

    /*
    public boolean isCoreCommand(Command label){
        return registeredCommands.containsKey(label);
    }
    public boolean isCoreCommand(String label){
        return registeredCommands.containsKey(bukkitCommands.getCommand(label));
    }

     */

    public Map<String, CrownCommandExecutor> getRegisteredCommands(){
        return registeredCommands;
    }

    public boolean executeCommand(Command command, CommandSender sender, String label, String[] args){
        if(registeredCommands.get(command.getName()) == null) throw new NullPointerException(command + " has no executor!");

        CrownCommandExecutor executor = registeredCommands.get(command.getLabel());

        try {
            return executor.run(sender, command, label, args);
        } catch (CrownException e){
            return true;
        }
    }
}
