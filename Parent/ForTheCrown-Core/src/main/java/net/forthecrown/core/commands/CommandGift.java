package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandGift extends CrownCommandBuilder {

    public CommandGift(){
        super("gift", FtcCore.getInstance());

        setPermission("ftc.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("player", EntityArgument.player())
                        .executes(c -> {
                            CommandSender sender = c.getSource().asBukkit();
                            Player player = EntityArgument.getPlayer(c, "player");
                            Location targetLoc = player.getLocation();

                            // Get shulker:
                            Location chestLoc = new Location(Bukkit.getWorld("world"), 278, 80, 964);
                            Chest chest;
                            try {
                                chest = (Chest) chestLoc.getBlock().getState();
                            }
                            catch (Exception e) {
                                sender.sendMessage("Not a chest at location: 'world', 278, 80, 964");
                                return 0;
                            }
                            ItemStack shulker = chest.getInventory().getItem(0);
                            if (shulker == null) {
                                sender.sendMessage("Not an item in slot 0 in chest at location: 'world', 278, 80, 964");
                                return 0;
                            }

                            targetLoc.getWorld().dropItemNaturally(targetLoc, shulker);
                            sender.sendMessage("Gave Christmas giftbox to " + player.getName() + " :D");
                            return 0;
                        })
                     );
    }
}
