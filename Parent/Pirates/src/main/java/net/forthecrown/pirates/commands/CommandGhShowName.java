package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.pirates.Pirates;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandGhShowName extends CrownCommandBuilder {

    public CommandGhShowName() {
        super("GHTargetShowName", Pirates.inst);
        setPermission("ftc.pirates.targetshowname");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            for (Entity nearbyEntity : player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2)) {
                if (nearbyEntity.getType() != EntityType.ARMOR_STAND) continue;

                if (nearbyEntity.getCustomName() != null && nearbyEntity.getCustomName().contains("GHTargetStand")) {
                    if (nearbyEntity.isCustomNameVisible()) nearbyEntity.setCustomNameVisible(false);
                    else nearbyEntity.setCustomNameVisible(true);
                    return 0;
                }
            }
            return 0;
        });
    }
}
