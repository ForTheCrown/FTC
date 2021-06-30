package net.forthecrown.cosmetics.effects;

import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.custom.CustomInvBuilder;
import net.forthecrown.inventory.custom.CustomInventory;
import net.forthecrown.inventory.custom.borders.GenericBorder;
import net.forthecrown.inventory.custom.options.ClickAction;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.inventory.custom.options.Option;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class MainCosmeticsMenu implements CosmeticMenu {

    private final CustomInventory inv;
    private final Map<Integer, Option> menuSlots;

    public MainCosmeticsMenu(CrownUser user) {
        this.menuSlots = Map.of(
                20, CosmeticConstants.getArrowMenu(user),
                22, CosmeticConstants.getEmoteMenu(user),
                24, CosmeticConstants.getDeathMenu(user)
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

    private ClickableOption getRideOption(CrownUser user, int slot) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(0);
        option.setItem(user.allowsRidingPlayers() ? allowsRidingItem : deniesRidingItem);
        option.setActionOnClick(new ClickAction() {
            @Override
            public void run() {
                boolean newOpinion = !user.allowsRidingPlayers();
                user.setAllowsRidingPlayers(newOpinion);

                ClickableOption newOption = new ClickableOption();
                newOption.setCooldown(0);
                newOption.setItem(newOpinion ? allowsRidingItem : deniesRidingItem);
                newOption.setActionOnClick(this);

                getCustomInv().updateOption(slot, newOption);
            }
        });
        return option;
    }


    @Override
    public CustomInventory buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(menuSlots)
                .addOption(4, getHeaderOption(user))
                .addOption(40, getRideOption(user, 40))
                .build();
    }

    @Override
    public CustomInventory getCustomInv() {
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

    private static final ItemStack allowsRidingItem = CrownItems.makeItem(Material.SADDLE, 1, true,
            ChatColor.YELLOW + "You can ride other players!",
            "",
            ChatColor.GRAY + "Right-click someone to jump on top of them.",
            ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
            ChatColor.GRAY + "Click to disable this feature.");

    private static final ItemStack deniesRidingItem = CrownItems.makeItem(Material.BARRIER, 1, true,
            ChatColor.YELLOW + "You've disabled riding other players.",
            "",
            ChatColor.GRAY + "Right-click someone to jump on top of them.",
            ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
            ChatColor.GRAY + "Click to enable this feature.");
}
