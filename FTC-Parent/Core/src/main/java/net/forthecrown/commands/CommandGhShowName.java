package net.forthecrown.pirates.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.Pirates;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandGhShowName extends FtcCommand {

    public CommandGhShowName() {
        super("GHTargetShowName", Pirates.inst);
        setPermission("ftc.pirates.targetshowname");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
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
