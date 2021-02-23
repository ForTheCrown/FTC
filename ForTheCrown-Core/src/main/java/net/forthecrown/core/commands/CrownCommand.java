package net.forthecrown.core.commands;

import com.google.common.collect.ImmutableList;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This exists for the sole reason of my laziness of throwing CrownExceptions in command classes, instead of writing return false statements with messages
 * {@link CrownException}
 */
public abstract class CrownCommand extends Command {

    private final String prefix;
    private TabCompleter tabCompleter;

    protected static final List<String> COMMON_DURATIONS = ImmutableList.of("1", "60", "600", "3600", "86400");
    protected static final List<String> COMMON_DATE_DIFFS = ImmutableList.of("1m", "15m", "1h", "3h", "12h", "1d", "1w", "1mo", "1y");

    protected CrownCommand(String name, Plugin plugin) {
        super(name);
        if(plugin.getDescription().getPrefix() != null) prefix = plugin.getDescription().getPrefix();
        else prefix = "ftccore";

        setUsage("");
        setPermission("ftc.commands." + name);
        setPermissionMessage("&7You do not have permission to use this command!");
    }

    public abstract boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException;

    protected static List<String> getPlayerNameList(){
        List<String> toReturn = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()){
            toReturn.add(p.getName());
        }
        return toReturn;
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
        return super.setPermissionMessage(CrownUtils.translateHexCodes(permissionMessage));
    }

    @Override
    public Command setUsage(String usage) {
        return super.setUsage(CrownUtils.translateHexCodes(usage));
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
            if(!run(sender, this, commandLabel, args)){
                if(usageMessage != null && !usageMessage.isBlank()) sender.sendMessage(getUsage().split("\n"));
            }
        } catch (CrownException ignored){ }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if(getTabCompleter() != null){
            List<String> asd = getTabCompleter().onTabComplete(sender, this, alias, args);

            if(asd != null) return asd;
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], getPlayerNameList(), new ArrayList<>());
    }
}
