package net.forthecrown.randomfeatures.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.TargetSelectorType;
import net.forthecrown.randomfeatures.RandomFeatures;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CommandChristmasGift extends CrownCommandBuilder {

    public CommandChristmasGift(RandomFeatures plugin){
        super("christmasgift", plugin);

        setPermission("ftc.admin");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("player", TargetSelectorType.player())
                        .executes(c -> {
                            CommandSender sender = c.getSource().getBukkitSender();
                            Player player = TargetSelectorType.getPlayer(c, "player");
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

                            Item giftBox = targetLoc.getWorld().dropItem(targetLoc, shulker);
                            giftBox.setVelocity(new Vector(0, 0.2, 0));
                            sender.sendMessage("Gave Christmas giftbox to " + player.getName() + " :D");
                            return 0;
                        })
                );
    }
}
