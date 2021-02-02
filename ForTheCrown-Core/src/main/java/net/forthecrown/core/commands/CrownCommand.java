package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * This seems to be a better alternative to having a custom command handler, but I can't be arsed to convert every command class over to it... again
 * It does also require that I YEET every command in the commands section the plugin.yml
 *
 * A class just needs to extend this and do any setters it wants in the constructor.
 * The last line in the constructor MUST always be register();
 * Because that registers the command, and if that doesn't happen, the command will never be usable
 */
public abstract class CrownCommand extends Command implements CrownCommandExecutor {

    private final String prefix;

    protected CrownCommand(String name, Plugin plugin) {
        super(name);
        if(plugin.getDescription().getPrefix() != null) prefix = plugin.getDescription().getPrefix();
        else prefix = "ftccore";

        setPermission("ftc.commands." + name);
        setPermissionMessage("&7You do not have permission to use this command!");
    }

    protected Command setAliases(String... aliases) {
        return super.setAliases(Arrays.asList(aliases));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return super.tabComplete(sender, alias, args);
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
        try {
            return run(sender, this, commandLabel, args);
        } catch (CrownException e){
            return true;
        }
    }
}
