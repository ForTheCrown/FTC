package net.forthecrown.cosmetics.effects.emote;

import net.forthecrown.cosmetics.effects.CosmeticConstants;
import net.forthecrown.cosmetics.effects.CosmeticMenu;
import net.forthecrown.cosmetics.effects.MainCosmeticsMenu;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.custom.CustomInventory;
import net.forthecrown.inventory.custom.CustomInvBuilder;
import net.forthecrown.inventory.custom.borders.GenericBorder;
import net.forthecrown.inventory.custom.options.ClickAction;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.inventory.custom.options.Option;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EmoteMenu implements CosmeticMenu {

    private final CustomInventory inv;
    private final Map<Integer, Option> emoteSlots;

    public EmoteMenu(CrownUser user) {
        this.emoteSlots = Map.of(
                12, CosmeticConstants.BONK.getClickableOption(user),
                13, CosmeticConstants.MWAH.getClickableOption(user),
                14, CosmeticConstants.POKE.getClickableOption(user),
                21, CosmeticConstants.SCARE.getClickableOption(user),
                22, CosmeticConstants.JUG.getClickableOption(user),
                23, CosmeticConstants.HUG.getClickableOption(user));
        this.inv = buildInventory(user);
    }

    private Option getReturnOption(CrownUser user) {
        ClickableOption returnOption = new ClickableOption();
        returnOption.setCooldown(0);
        returnOption.setItem(CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back"));
        returnOption.setActionOnClick(() -> CosmeticMenu.open(new MainCosmeticsMenu(user), user));
        return returnOption;
    }

    private Option getToggleEmoteOption(CrownUser user, int slot) {
        ClickableOption toggleEmoteOption = new ClickableOption();
        toggleEmoteOption.setCooldown(5);
        toggleEmoteOption.setItem(user.allowsEmotes() ? allowsEmoteItem : deniesEmoteItem);
        toggleEmoteOption.setActionOnClick(new ClickAction() {
            @Override
            public void run() {
                ClickableOption newOption = new ClickableOption();
                newOption.setCooldown(0);
                newOption.setItem(!user.allowsEmotes() ? allowsEmoteItem : deniesEmoteItem);
                newOption.setActionOnClick(this);

                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "sudo " + user.getName() + " toggleemotes");
                user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

                getCustomInv().updateOption(slot, newOption);
            }
        });
        return toggleEmoteOption;
    }

    @Override
    public CustomInventory buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(emoteSlots)
                .addOption(4, getReturnOption(user))
                .addOption(31, getToggleEmoteOption(user, 31))
                .build();
    }

    @Override
    public CustomInventory getCustomInv() { return this.inv; }

    @Override
    public TextComponent getTitle() { return Component.text("Emotes"); }

    @Override
    public int getSize() { return 36; }


    private static final ItemStack allowsEmoteItem = CrownItems.makeItem(
            Material.STRUCTURE_VOID, 1, true,
            ChatColor.GOLD + "Emotes Enabled",
            ChatColor.GRAY + "Right-click to disable sending and receiving emotes.");

    private static final ItemStack deniesEmoteItem = CrownItems.makeItem(
            Material.BARRIER, 1, true,
            ChatColor.GOLD + "Emotes Disabled",
            ChatColor.GRAY + "Right-click to enable sending and receiving emotes.");

}
