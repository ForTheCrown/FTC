package net.forthecrown.core;

import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final Set<CrownCommand> registeredCommands = new HashSet<>();

    protected CrownCommand(String name) {
        super(name);
        prefix = "ftccore";
        registeredCommands.add(this);
    }

    protected CrownCommand(String name, String cmdPrefix) {
        super(name);
        prefix = cmdPrefix;
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

    protected void register(){
        CommandMap map = FtcCore.getInstance().getServer().getCommandMap();
        map.register(getName(), prefix, this);
        super.register(map);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        try {
            return run(sender, this, commandLabel, args);
        } catch (CrownException e){
            return true;
        }
    }

    public static Set<CrownCommand> getRegisteredCommands(){
        return registeredCommands;
    }
}
