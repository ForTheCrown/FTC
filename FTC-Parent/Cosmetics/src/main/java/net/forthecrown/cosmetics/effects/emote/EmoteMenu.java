package net.forthecrown.cosmetics.effects.emote;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.forthecrown.cosmetics.custominvs.CustomInvBuilder;
import net.forthecrown.cosmetics.custominvs.borders.GenericBorder;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.custominvs.options.Option;
import net.forthecrown.cosmetics.effects.CosmeticMenu;
import net.forthecrown.cosmetics.effects.Vault;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Map;

public class EmoteMenu implements CosmeticMenu {

    private final CustomInv inv;
    private final Map<Integer, Option> emoteSlots;

    public EmoteMenu(CrownUser user) {
        this.emoteSlots = Map.of(
                12, Vault.bonk.getClickableOption(user),
                13, Vault.mwah.getClickableOption(user),
                14, Vault.poke.getClickableOption(user),
                21, Vault.scare.getClickableOption(user),
                22, Vault.jingle.getClickableOption(user),
                23, Vault.hug.getClickableOption(user));
        this.inv = buildInventory(user);
    }

    private Option getReturnOption() {
        ClickableOption returnOption = new ClickableOption();
        returnOption.setCooldown(0);
        returnOption.setActionOnClick(() -> {
            // TODO: go back to main cosmetic menu
        });
        returnOption.setItem(CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back"));
        return returnOption;
    }

    private Option getToggleEmoteOption(CrownUser user) {
        boolean emoter = user.allowsEmotes();
        ClickableOption toggleEmoteOption = new ClickableOption();
        toggleEmoteOption.setCooldown(5);
        toggleEmoteOption.setActionOnClick(() -> {
            // TODO: Message? Item toggle?
            user.setAllowsEmotes(!emoter);
        });
        if (emoter) toggleEmoteOption.setItem(CrownItems.makeItem(
                Material.STRUCTURE_VOID, 1, true,
                ChatColor.GOLD + "Emotes Enabled",
                ChatColor.GRAY + "Right-click to disable sending and receiving emotes."));
        else toggleEmoteOption.setItem(CrownItems.makeItem(
                Material.BARRIER, 1, true,
                ChatColor.GOLD + "Emotes Disabled",
                ChatColor.GRAY + "Right-click to enable sending and receiving emotes."));
        return toggleEmoteOption;
    }

    @Override
    public CustomInv buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();
        return invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(emoteSlots)
                .addOption(4, getReturnOption())
                .addOption(31, getToggleEmoteOption(user))
                .build();
    }

    @Override
    public CustomInv getCustomInv() { return this.inv; }

    @Override
    public TextComponent getTitle() { return Component.text("Emotes"); }

    @Override
    public int getSize() { return 36; }
}
