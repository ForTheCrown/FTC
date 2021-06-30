package net.forthecrown.cosmetics.effects.death;

import net.forthecrown.cosmetics.effects.CosmeticConstants;
import net.forthecrown.cosmetics.effects.CosmeticMenu;
import net.forthecrown.cosmetics.effects.MainCosmeticsMenu;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.custom.CustomInventory;
import net.forthecrown.inventory.custom.CustomInvBuilder;
import net.forthecrown.inventory.custom.borders.GenericBorder;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.inventory.custom.options.Option;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Map;

public class DeathParticleMenu implements CosmeticMenu {

    private final CustomInventory inv;
    private final Map<Integer, Option> deathEffectSlots;

    public DeathParticleMenu(CrownUser user) {
        this.deathEffectSlots = Map.of(
                10, CosmeticConstants.DEATH_SOUL.getClickableOption(user),
                11, CosmeticConstants.TOTEM.getClickableOption(user),
                12, CosmeticConstants.EXPLOSION.getClickableOption(user),
                13, CosmeticConstants.ENDER_RING.getClickableOption(user),
                31, CosmeticConstants.NONE_DEATH.getClickableOption(user));
        this.inv = buildInventory(user);
    }


    private Option getReturnOption(CrownUser user) {
        ClickableOption returnOption = new ClickableOption();
        returnOption.setCooldown(0);
        returnOption.setItem(CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back"));
        returnOption.setActionOnClick(() -> CosmeticMenu.open(new MainCosmeticsMenu(user), user));
        return returnOption;
    }

    @Override
    public CustomInventory buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(deathEffectSlots)
                .addOption(4, getReturnOption(user))
                .build();
    }

    @Override
    public CustomInventory getCustomInv() { return this.inv; }

    @Override
    public int getSize() { return 36; }

    @Override
    public TextComponent getTitle() { return Component.text("Death Particles"); }

}
