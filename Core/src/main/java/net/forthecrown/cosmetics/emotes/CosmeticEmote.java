package net.forthecrown.cosmetics.emotes;

import net.forthecrown.commands.emotes.CommandEmote;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CosmeticEmote extends CosmeticEffect implements Predicate<Permissible> {

    private final CommandEmote command;
    private final Permission permission;

    CosmeticEmote(int slot, CommandEmote command, String name, @Nullable Permission permission, Component... description) {
        super(name, InventoryPos.fromSlot(slot), description);
        this.command = command;
        this.permission = permission;
    }

    CosmeticEmote(int slot, CommandEmote command, String name, String desc){
        this(slot, command, name, null, ChatUtils.convertString(desc, true));
    }

    public String getCommandText(){
        return '/' + getName();
    }

    public Component commandText(){
        return Component.text(getCommandText());
    }

    public CommandEmote getCommand() {
        return command;
    }

    public Permission getPermission() {
        return permission;
    }

    public String getPerm(){
        if(permission == null) return null;
        return getPermission().getName();
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(test(user) ? Material.ORANGE_DYE : Material.GRAY_DYE)
                .setName(commandText().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)));

        for (Component c: description){
            builder.addLore(c.style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public boolean test(Permissible permissible) {
        if(getPermission() == null) return true;
        return permissible.hasPermission(getPermission());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
    }
}
