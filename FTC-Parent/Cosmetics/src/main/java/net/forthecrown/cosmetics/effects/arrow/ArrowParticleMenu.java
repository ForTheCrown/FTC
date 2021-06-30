package net.forthecrown.cosmetics.effects.arrow;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.forthecrown.cosmetics.custominvs.CustomInvBuilder;
import net.forthecrown.cosmetics.custominvs.borders.GenericBorder;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.custominvs.options.Option;
import net.forthecrown.cosmetics.effects.CosmeticMenu;
import net.forthecrown.cosmetics.effects.MainCosmeticsMenu;
import net.forthecrown.cosmetics.effects.Vault;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Map;

public class ArrowParticleMenu implements CosmeticMenu {

    private final CustomInv inv;
    private final Map<Integer, Option> arrowEffectSlots;

    public ArrowParticleMenu(CrownUser user) {
        this.arrowEffectSlots = Map.of(
                10, Vault.flame.getClickableOption(user),
                11, Vault.snowball.getClickableOption(user),
                12, Vault.sneeze.getClickableOption(user),
                13, Vault.heart.getClickableOption(user),
                14, Vault.damageIndicator.getClickableOption(user),
                15, Vault.drippingHoney.getClickableOption(user),
                16, Vault.campfireCozySmoke.getClickableOption(user),
                19, Vault.arrowSoul.getClickableOption(user),
                20, Vault.fireworkSpark.getClickableOption(user),
                31, Vault.arrowNone.getClickableOption(user));
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
    public CustomInv buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(arrowEffectSlots)
                .addOption(4, getReturnOption(user))
                .build();
    }

    @Override
    public CustomInv getCustomInv() { return this.inv; }

    @Override
    public TextComponent getTitle() { return Component.text(" Arrow Effects"); }

    @Override
    public int getSize() { return 36; }
}
