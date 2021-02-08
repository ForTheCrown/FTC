package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public abstract class CrownCommand extends Command implements CrownCommandExecutor {

    private final String prefix;
    private TabCompleter tabCompleter;

    protected CrownCommand(String name, Plugin plugin) {
        super(name);
        if(plugin.getDescription().getPrefix() != null) prefix = plugin.getDescription().getPrefix();
        else prefix = "ftccore";

        setPermission("ftc.commands." + name);
        setPermissionMessage("&7You do not have permission to use this command!");
    }

    protected void setTabCompleter(TabCompleter tabCompleter){
        this.tabCompleter = tabCompleter;
    }

    protected TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    protected Command setAliases(String... aliases) {
        return super.setAliases(Arrays.asList(aliases));
    }

    @Override
    public Command setPermissionMessage(String permissionMessage) {
        return super.setPermissionMessage(FtcCore.translateHexCodes(permissionMessage));
    }

    @Override
    public Command setUsage(String usage) {
        return super.setUsage(FtcCore.translateHexCodes(usage));
    }

    public boolean register(){
        CommandMap map = FtcCore.getInstance().getServer().getCommandMap();
        map.register(getName(), prefix, this);
        return super.register(map);
    }

    public boolean unregister(){
        return super.unregister(FtcCore.getInstance().getServer().getCommandMap());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(!testPermission(sender)) return true;

        try {
            return run(sender, this, commandLabel, args);
        } catch (CrownException e){
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if(getTabCompleter() != null){
            TabCompleter tab = getTabCompleter();
            List<String> asd = tab.onTabComplete(sender, this, alias, args);

            if(asd != null) return asd;
        }
        return super.tabComplete(sender, alias, args);
    }
}
