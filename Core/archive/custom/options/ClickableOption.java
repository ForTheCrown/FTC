package net.forthecrown.inventory.custom.options;

import net.forthecrown.utils.Cooldown;
import org.bukkit.entity.HumanEntity;

public class ClickableOption extends Option {

    private ClickAction actionOnClick;
    private int cd = 0;

    public ClickAction getAction() { return this.actionOnClick; }
    public void setActionOnClick(ClickAction action) { this.actionOnClick = action; }

    public int getCooldown() { return this.cd; }
    public void setCooldown(int cd) { this.cd = cd; }


    @Override
    public void handleClick(HumanEntity human) {
        if (Cooldown.contains(human, getClass().getSimpleName())) return;

        getAction().run();
        Cooldown.add(human, getClass().getSimpleName(), getCooldown());
    }
}
