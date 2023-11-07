package net.forthecrown.usables.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.Usables;
import net.forthecrown.usables.objects.UsableItem;
import org.bukkit.permissions.Permission;

public class UsableItemCommand extends InWorldUsableCommand<UsableItem> {

  public UsableItemCommand() {
    super("usableitem", "item");
    setAliases("usable_item");
    setPermission(UPermissions.ITEM);
  }

  @Override
  public Permission getAdminPermission() {
    return UPermissions.ITEM;
  }

  @Override
  protected ArgumentType<?> getArgumentType() {
    return null;
  }

  @Override
  protected UsableProvider<UsableItem> getProvider(String argument) {
    return new UsableProvider<>() {
      @Override
      public UsableItem get(CommandContext<CommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().asPlayer();
        var held = Commands.getHeldItem(player);

        var usable = Usables.item(held);

        if (Usables.isUsable(held)) {
          usable.load();
        }

        return usable;
      }

      @Override
      public void postEdit(UsableItem holder) {
        holder.save();
      }
    };
  }
}
