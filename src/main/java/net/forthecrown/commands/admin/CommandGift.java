package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandGift extends FtcCommand {

    public CommandGift(){
        super("gift");

        setPermission(Permissions.ADMIN);
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
                            Location chestLoc = new Location(Worlds.overworld(), 278, 80, 964);
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