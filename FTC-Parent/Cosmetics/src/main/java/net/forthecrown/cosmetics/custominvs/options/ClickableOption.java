package net.forthecrown.cosmetics.custominvs.options;

import net.forthecrown.cosmetics.Cosmetics;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.util.Set;
import java.util.UUID;

public class ClickableOption extends Option {

    private ClickAction actionOnClick;
    private int cd = 0;
    private Set<UUID> isOnCooldown = Set.of();

    public ClickAction getAction() { return this.actionOnClick; }
    public void setActionOnClick(ClickAction action) { this.actionOnClick = action; }

    public int getCooldown() { return this.cd; }
    public void setCooldown(int cd) { this.cd = cd; }


    @Override
    public void handleClick(HumanEntity human) {
        UUID id = human.getUniqueId();
        if (!isOnCooldown.contains(id)) {
            //CrownUser user = UserManager.getUser(id);
            if (getCooldown() == 0) getAction().run();
            else {
                isOnCooldown.add(id);
                getAction().run();
                Bukkit.getScheduler().scheduleSyncDelayedTask(Cosmetics.getPlugin(), () -> {
                    isOnCooldown.remove(id);
                }, getCooldown());
            }
        }
    }
}
