package net.forthecrown.cosmetics.custominvs;

import net.forthecrown.cosmetics.Cosmetics;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.util.Set;
import java.util.UUID;

public class ClickableOption extends Option {

    private Runnable actionOnClick;
    private int cd = 0;
    private Set<UUID> isOnCooldown = Set.of();

    public Runnable getAction() { return this.actionOnClick; }
    public void setActionOnClick(Runnable action) { this.actionOnClick = action; }

    public int getCooldown() { return this.cd; }
    public void setCooldown(int cd) { this.cd = cd; }


    @Override
    public void handleClick(HumanEntity user) {
        UUID id = user.getUniqueId();
        if (!isOnCooldown.contains(id)) {
            isOnCooldown.add(id);
            getAction().run();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Cosmetics.getPlugin(), () -> {
                isOnCooldown.remove(id);
            }, getCooldown());
        }
    }
}
