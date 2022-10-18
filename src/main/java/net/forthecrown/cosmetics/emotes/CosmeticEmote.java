package net.forthecrown.cosmetics.emotes;

import lombok.Getter;
import net.forthecrown.commands.emotes.CommandEmote;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticMeta;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.text.Text.nonItalic;

public class CosmeticEmote extends Cosmetic {

    @Getter
    private final CommandEmote command;
    @Getter
    private final Permission permission;

    CosmeticEmote(int slot, CommandEmote command, String name, @Nullable Permission permission, Component... description) {
        super(name, Cosmetics.EMOTE, Slot.of(slot),
                CosmeticMeta.builder()
                        .setOption(MenuNode.builder()
                                .setItem(user -> {
                                    var builder = ItemStacks.builder(getMaterial(command, user))
                                            .setName("&e/" + command.getName());

                                    for (Component c: description) {
                                        builder.addLore(c.style(nonItalic(NamedTextColor.GRAY)));
                                    }

                                    return builder.build();
                                })
                                .build()
                        )
                        .addDescription(description)
        );

        this.command = command;
        this.permission = permission;
    }

    CosmeticEmote(int slot, CommandEmote command, String name, String desc) {
        this(slot, command, name, null, Text.renderString(desc));
    }

    private static Material getMaterial(CommandEmote emote, User user) {
        return emote.test(user.getCommandSource(emote)) ? Material.ORANGE_DYE : Material.GRAY_DYE;
    }

    public String getCommandText() {
        return '/' + getName();
    }

    public Component commandText() {
        return Component.text(getCommandText());
    }

    public boolean test(User permissible) {
        if (getPermission() == null) {
            return true;
        }

        return permissible.hasPermission(getPermission());
    }
}