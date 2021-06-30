package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.cosmetics.effects.CosmeticEffect;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.user.CrownUser;
import org.bukkit.inventory.ItemStack;

public abstract class CosmeticEmoteEffect implements CosmeticEffect {

    @Override
    public int getGemCost() { return 0; }

    @Override
    public abstract String getEffectName();

    @Override
    public ItemStack getEffectItem(boolean ignored) { return getEffectItem(); }

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
        option.setActionOnClick(() -> {}); // Clicking on emote doesn't do anything

        // Item to display
        ItemStack item = getEffectItem();
        if (isOwnedBy(user)) setItemOwned(item);
        option.setItem(item);

        return option;
    }
}
