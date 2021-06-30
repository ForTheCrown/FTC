package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.effects.CosmeticEffect;
import org.bukkit.inventory.ItemStack;

public abstract class CosmeticEmoteEffect implements CosmeticEffect {

    @Override
    public abstract String getEffectName();

    @Override
    public abstract ItemStack getEffectItem();

    public abstract String getPermission();

    @Override
    public final boolean isOwnedBy(CrownUser user) {
        return user.hasPermission(getPermission());
    }

    @Override
    public final boolean isCurrentActiveEffect(CrownUser user) { return false; }

    public ClickableOption getClickableOption(CrownUser user) {
        ClickableOption option = new ClickableOption();
        // Clicking
        option.setCooldown(0);
        option.setActionOnClick(() -> {
            // TODO: Buy effect or set it as active
            System.out.println("Clicked on + " + getEffectName());
        });

        // Item to display
        ItemStack item = getEffectItem();
        if (isOwnedBy(user)) setItemOwned(item);
        option.setItem(item);

        return option;
    }
}
