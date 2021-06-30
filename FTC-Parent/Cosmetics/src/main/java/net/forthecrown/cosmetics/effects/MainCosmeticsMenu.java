package net.forthecrown.cosmetics.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.forthecrown.cosmetics.custominvs.CustomInvBuilder;
import net.forthecrown.cosmetics.custominvs.borders.GenericBorder;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.custominvs.options.Option;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class MainCosmeticsMenu implements CosmeticMenu {

    private final CustomInv inv;
    private final Map<Integer, Option> menuSlots;

    public MainCosmeticsMenu(CrownUser user) {
        this.menuSlots = Map.of(
                20, Vault.getArrowMenu(user),
                22, Vault.getEmoteMenu(user),
                24, Vault.getDeathMenu(user)
        );
        this.inv = buildInventory(user);
    }

    private Option getHeaderOption(CrownUser user) {
        Option result = new Option();
        result.setItem(CrownItems.makeItem(Material.NETHER_STAR, 1, true,
                ChatColor.YELLOW + "Menu",
                ChatColor.DARK_GRAY + "ulala",
                ChatColor.GRAY + "You have " + ChatColor.GOLD + user.getGems() + ChatColor.GRAY + " Gems."));
        return result;
    }

    private ClickableOption getRideOption(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(0);
        option.setActionOnClick(() -> {
//            TODO: toggle to allowRiding
        });
        ItemStack item;
        if (user.allowsRidingPlayers())
            item = CrownItems.makeItem(Material.SADDLE, 1, true,
                ChatColor.YELLOW + "You can ride other players!",
                "",
                ChatColor.GRAY + "Right-click someone to jump on top of them.",
                ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
                ChatColor.GRAY + "Click to disable this feature.");
        else
            item = CrownItems.makeItem(Material.BARRIER, 1, true,
                ChatColor.YELLOW + "You've disabled riding other players.",
                "",
                ChatColor.GRAY + "Right-click someone to jump on top of them.",
                ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
                ChatColor.GRAY + "Click to enable this feature.");
        return option;
    }

    @Override
    public CustomInv buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(menuSlots)
                .addOption(4, getHeaderOption(user))
                .addOption(40, getRideOption(user))
                .build();
    }

    @Override
    public CustomInv getCustomInv() {
        return this.inv;
    }

    @Override
    public int getSize() { return 54; }

    @Override
    public TextComponent getTitle() {
        return Component.text()
                .append(Component.text("C").decorate(TextDecoration.BOLD))
                .append(Component.text("osmetics")).build();
    }

}
