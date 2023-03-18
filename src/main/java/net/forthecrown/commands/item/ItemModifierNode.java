package net.forthecrown.commands.item;

import com.google.common.base.Strings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Consumer;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public abstract class ItemModifierNode extends FtcCommand {

  public ItemModifierNode(String name, String... aliases) {
    super(name);

    setAliases(aliases);
    setPermission(ItemModCommands.PERMISSION);
  }

  String getArgumentName() {
    return getName();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    create(command);
  }

  protected void getItemSuggestions(CommandSource source,
                                    Consumer<ItemStack> consumer
  ) {
    if (!source.isPlayer()) {
      return;
    }

    var player = source.asPlayerOrNull();
    var held = player.getInventory().getItemInMainHand();

    if (ItemStacks.isEmpty(held)) {
      return;
    }

    consumer.accept(held);
  }

  public abstract void create(LiteralArgumentBuilder<CommandSource> command);

  protected ItemStack getHeld(CommandSource source) throws CommandSyntaxException {
    var player = source.asPlayer();
    var held = Commands.getHeldItem(player);

    if (held.getItemMeta() == null) {
      throw Exceptions.ITEM_CANNOT_HAVE_META;
    }

    return held;
  }

  protected Component optionallyWrap(Component text,
                                     CommandContext<CommandSource> c,
                                     String argName
  ) {
    var input = Commands.findInput(argName, c);

    if (Strings.isNullOrEmpty(input)) {
      return Text.wrapForItems(text);
    }

    if (input.startsWith("{")
        || input.startsWith("\"")
        || input.startsWith("[")
    ) {
      return text;
    }

    return Text.wrapForItems(text);
  }
}