package net.forthecrown.cosmetics.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.cosmetics.Cosmetics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmeticsCommand extends CrownCommand {

    public CosmeticsCommand(){
        super("cosmetics", Cosmetics.plugin);

        setPermission(null);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the Cosmetics menu
     *
     *
     * Valid usages of command:
     * - /cosmetics
     *
     * Referenced other classes:
     * - Main: Main.plugin
     *
     * Author: Wout
     */

    @Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        CrownUser user = FtcCore.getUser(player.getUniqueId());
        player.openInventory(Cosmetics.plugin.getMainCosmeticInventory(user));

        return true;
    }
}
