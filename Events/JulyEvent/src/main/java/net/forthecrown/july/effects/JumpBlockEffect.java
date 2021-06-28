package net.forthecrown.july.effects;

import net.forthecrown.july.JulyMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class JumpBlockEffect implements BlockEffect {
    @Override
    public void apply(Player player) {
        Vector velocity = player.getVelocity();
        velocity.setY(velocity.getY() + JulyMain.jumpBoost())
                .add(player.getLocation().getDirection().multiply(1.5f));

        player.setVelocity(velocity);
    }

    @Override
    public Material getMaterial() {
        return Material.RED_GLAZED_TERRACOTTA;
    }
}
