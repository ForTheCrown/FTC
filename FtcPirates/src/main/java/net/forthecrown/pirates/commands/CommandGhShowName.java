package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.pirates.Pirates;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandGhShowName extends CrownCommand {

    public CommandGhShowName() {
        super("GHTargetShowName", Pirates.plugin);
        setPermission("ftc.pirates.targetshowname");
        register();
    }

    @Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) throw new CrownException(sender, "Only players may execute this command!");
        Player player = (Player) sender;

        for (Entity nearbyEntity : player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2)) {
            if (nearbyEntity.getType() != EntityType.ARMOR_STAND) continue;

            if (nearbyEntity.getCustomName() != null && nearbyEntity.getCustomName().contains("GHTargetStand")) {
                if (nearbyEntity.isCustomNameVisible()) nearbyEntity.setCustomNameVisible(false);
                else nearbyEntity.setCustomNameVisible(true);
                return true;
            }
        }
        return false;
    }
}
