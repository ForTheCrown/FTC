package net.forthecrown.usables.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.usables.CmdUsables;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.objects.Kit;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class KitCommand extends CmdUsableCommand<Kit> {

  public KitCommand(CmdUsables<Kit> usables) {
    super("kit", usables, Kit.class);
    setAliases("kits");
    setPermission(UPermissions.KIT);
  }

  @Override
  protected String usagePrefix() {
    return "<kit>";
  }

  @Override
  public Permission getAdminPermission() {
    return UPermissions.KIT_ADMIN;
  }

  @Override
  protected Kit create(String name, CommandSource source) throws CommandSyntaxException {
    Player player = source.asPlayer();
    ItemList items = ItemLists.fromInventory(player.getInventory());

    Kit kit = new Kit(name);
    kit.setItems(items);

    return kit;
  }

  @Override
  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<Kit> provider
  ) {
    super.createEditArguments(argument, provider);

    argument.then(literal("items")
        .requires(hasAdminPermission())

        .executes(c -> {
          Kit kit = provider.get(c);
          ItemList list = kit.getItems();

          TextJoiner joiner = TextJoiner.newJoiner()
              .setPrefix(Text.format("{0} items:\n", kit.name()))
              .setDelimiter(Component.text("\n- "));

          c.getSource().sendMessage(
              joiner.add(list.stream().map(Text::itemAndAmount))
                  .asComponent()
          );
          return 0;
        })

        .then(literal("set")
            .executes(c -> {
              Player player = c.getSource().asPlayer();
              ItemList list = ItemLists.fromInventory(player.getInventory());
              return setItem(c, provider, list);
            })

            .then(argument("item_list", Arguments.ITEM_LIST)
                .executes(c -> {
                  ItemList list = Arguments.getItemList(c, "item_list");
                  return setItem(c, provider, list);
                })
            )
        )
    );
  }

  private int setItem(CommandContext<CommandSource> c, UsableProvider<Kit> provider, ItemList list)
      throws CommandSyntaxException
  {
    Kit kit = provider.get(c);
    kit.setItems(list);

    c.getSource().sendSuccess(
        Text.format("Update items for {0} {1}", displayName, kit.displayName())
    );
    return 0;
  }
}
