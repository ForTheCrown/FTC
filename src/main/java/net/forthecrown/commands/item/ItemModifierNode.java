package net.forthecrown.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;

public abstract class ItemModifierNode extends FtcCommand {
    public ItemModifierNode(String name, String... aliases) {
        super(name);

        setAliases(aliases);
        setPermission(ItemModCommands.PERMISSION);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        create(command);
    }

    public abstract void create(LiteralArgumentBuilder<CommandSource> command);

    protected ItemStack getHeld(CommandSource source) throws CommandSyntaxException {
        var player = source.asPlayer();
        var held = player.getInventory().getItemInMainHand();

        if (ItemStacks.isEmpty(held)) {
            throw Exceptions.MUST_HOLD_ITEM;
        }

        if (held.getItemMeta() == null) {
            throw Exceptions.ITEM_CANNOT_HAVE_META;
        }

        return held;
    }
}