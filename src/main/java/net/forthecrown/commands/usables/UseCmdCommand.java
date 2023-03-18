package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.UseCmdArgument;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ItemArgument.Result;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.useables.command.CommandUsable;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.useables.command.Warp;
import net.forthecrown.useables.test.TestPermission;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

public abstract class UseCmdCommand<T extends CommandUsable> extends FtcCommand {

  private final UseCmdArgument<T> argument;
  private final Permission adminPermission;
  private final String otherUserFormat;

  public UseCmdCommand(String name,
                       Permission permission,
                       Permission adminPermission,
                       UseCmdArgument<T> argument,
                       String otherUserFormat
  ) {
    super(name);

    setPermission(permission);

    this.otherUserFormat = otherUserFormat;
    this.argument = argument;
    this.adminPermission = adminPermission;

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Lists all %ss", getName());

    factory.usage("create <name>")
        .setPermission(adminPermission)
        .addInfo("Creates a new %s", getName());

    var prefixed = factory.withPrefix("<" + getName() + ">");
    prefixed.usage("")
        .addInfo("Uses a <%s>", getName());

    prefixed = prefixed.withPermission(adminPermission);

    prefixed.usage("<user>")
        .addInfo("Makes a <user> use a <%s>", getName());

    prefixed.usage("remove")
        .addInfo("Removes a <%s>", getName());

    var edit = prefixed.withPrefix("edit");

    addEditUsages(edit);
    UsableCommands.CHECK_NODE.populateUsages(edit, getName());

  }

  protected void addEditUsages(UsageFactory factory) {

  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          var user = getUserSender(c);
          var list = argument.getManager().getUsable(user.getPlayer());

          if (list.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
          }

          user.sendMessage(
              Text.format("{0, class}s: &e{1}",
                  NamedTextColor.GRAY,

                  argument.getTypeClass(),
                  TextJoiner.onComma()
                      .add(list.stream().map(CommandUsable::displayName))
              )
          );
          return 0;
        })

        .then(literal("create")
            .requires(source -> source.hasPermission(adminPermission))

            .then(argument("name", StringArgumentType.word())
                .executes(c -> {
                  var player = c.getSource().asPlayer();
                  var t = create(player, c.getArgument("name", String.class), c);

                  argument.getManager().add(t);

                  c.getSource().sendSuccess(
                      Text.format(
                          "Created {0, class, -simple} {1}",

                          argument.getTypeClass(),
                          t.displayName()
                      )
                  );
                  return 0;
                })
            )
        )

        .then(argument("usable", argument)
            .executes(c -> {
              var t = argument.get(c, "usable");

              var player = c.getSource().asPlayer();
              t.interact(player, player.hasPermission(adminPermission));

              return 0;
            })

            .then(argument("user", Arguments.ONLINE_USER)
                .requires(source -> source.hasPermission(adminPermission))

                .executes(c -> {
                  var t = argument.get(c, "usable");
                  var user = Arguments.getUser(c, "user");

                  t.onInteract(user.getPlayer(), true);

                  c.getSource().sendSuccess(
                      Text.format(otherUserFormat,
                          user,
                          t.displayName()
                      )
                  );
                  return 0;
                })
            )

            .then(literal("remove")
                .requires(source -> source.hasPermission(Permissions.ADMIN))

                .executes(c -> {
                  var value = argument.get(c, "usable");
                  argument.getManager().remove(value);

                  c.getSource().sendSuccess(
                      Text.format("Removed {0, class, -simple} named '{1}'",
                          argument.getTypeClass(),
                          value.getName()
                      )
                  );
                  return 0;
                })
            )

            .then(literal("edit")
                .requires(source -> source.hasPermission(Permissions.ADMIN))

                .then(
                    UsableCommands.CHECK_NODE
                        .createArguments(this::get, UsableSaveCallback.empty())
                )

                .then(editArgument())
            )
        );
  }

  protected T get(CommandContext<CommandSource> c) {
    return argument.get(c, "usable");
  }

  protected abstract LiteralArgumentBuilder<CommandSource> editArgument();

  protected abstract T create(Player player, String name, CommandContext<CommandSource> context)
      throws CommandSyntaxException;

  public static void createCommands() {
    new WarpCommand();
    new KitCommand();
  }

  private static class KitCommand extends UseCmdCommand<Kit> {

    public KitCommand() {
      super(
          "kit",
          Permissions.KIT,
          Permissions.KIT_ADMIN,
          Arguments.KITS,
          "Giving {0, user} kit {1}"
      );

      setDescription("Obtains the specified kit or views all available kits.");
    }

    @Override
    protected void addEditUsages(UsageFactory factory) {
      var prefixed = factory.withPrefix("items");

      prefixed.usage("")
          .addInfo("Sets the items of a <kit> to the items in your inventory");

      prefixed.usage("list")
          .addInfo("Lists all the items in a <kit>");

      prefixed.usage("add")
          .addInfo("Adds the item you're holding to a <kit>");

      prefixed.usage("add <item> <amount: number(1..64)>")
          .addInfo("Adds an <item> to a <kit>");

      prefixed.usage("remove <index: number(1..)>")
          .addInfo("Removes an item at <index> from a <kit>");
    }

    @Override
    protected LiteralArgumentBuilder<CommandSource> editArgument() {
      return literal("items")
          // Set from player inventory
          .executes(c -> {
            var player = c.getSource().asPlayer();
            var kit = get(c);

            List<ItemStack> items = listInventory(player);

            // Clear the kit's items and then
            // add all the new items
            kit.getItems().clear();
            kit.getItems().addAll(items);

            c.getSource().sendSuccess(
                Text.format("Set items of {0}",
                    kit.displayName()
                )
            );
            return 0;
          })

          // List items in the kit
          .then(literal("list")
              .executes(c -> {
                var kit = get(c);

                var writer = TextWriters.newWriter();
                writer.formatted("Kit {0} items:", kit.displayName());

                for (var i : kit.getItems()) {
                  writer.formatted("\n- {0, item, -amount}", i);
                }

                c.getSource().sendMessage(writer.asComponent());
                return 0;
              })
          )

          // Add item to kit
          .then(literal("add")
              // Add held item
              .executes(c -> {
                var player = c.getSource().asPlayer();
                var kit = get(c);

                var heldItem = player.getInventory().getItemInMainHand();

                if (ItemStacks.isEmpty(heldItem)) {
                  throw Exceptions.MUST_HOLD_ITEM;
                }

                kit.getItems().add(heldItem.clone());

                c.getSource().sendSuccess(
                    Text.format(
                        "Added {0, item} to {1}",
                        heldItem,
                        kit.displayName()
                    )
                );
                return 0;
              })

              // Add item by parsed value and quantity
              .then(argument("item", ArgumentTypes.item())
                  .then(argument("amount", IntegerArgumentType.integer(1, 64))
                      .executes(c -> {
                        var item = c.getArgument("item", Result.class);
                        var amount = c.getArgument("amount", Integer.class);

                        var stack = item.create(amount, true);

                        var kit = get(c);
                        kit.getItems().add(stack);

                        c.getSource().sendSuccess(
                            Text.format("Added {0, item} to {1}",
                                stack,
                                kit.displayName()
                            )
                        );
                        return 0;
                      })
                  )
              )
          )

          .then(literal("remove")
              .then(argument("index", IntegerArgumentType.integer(1))
                  .executes(c -> {
                    var kit = get(c);
                    var index = c.getArgument("index", Integer.class) - 1;

                    if (index < 0 || index >= kit.getItems().size()) {
                      throw Exceptions.invalidIndex(index + 1, kit.getItems().size());
                    }

                    var removed = kit.getItems().remove(index);

                    c.getSource().sendSuccess(
                        Text.format("Removed {0, item} from {1}",
                            removed,
                            kit.displayName()
                        )
                    );
                    return 0;
                  })
              )
          );
    }

    @Override
    protected Kit create(Player player, String name, CommandContext<CommandSource> context)
        throws CommandSyntaxException
    {
      return new Kit(name, listInventory(player));
    }

    private List<ItemStack> listInventory(Player player) throws CommandSyntaxException {
      List<ItemStack> items = new ArrayList<>();

      ItemStacks.forEachNonEmptyStack(player.getInventory(), itemStack -> {
        items.add(itemStack.clone());
      });

      if (items.isEmpty()) {
        throw Exceptions.INVENTORY_EMPTY;
      }

      return items;
    }
  }

  private static class WarpCommand extends UseCmdCommand<Warp> {

    static final ArgumentOption<ParsedPosition> position
        = Options.argument(ArgumentTypes.position(), "pos");

    static final ArgumentOption<Float> yaw
        = Options.argument(FloatArgumentType.floatArg(-180, 180))
        .setDefaultValue(0F)
        .addLabel("yaw")
        .build();

    static final ArgumentOption<Float> pitch
        = Options.argument(FloatArgumentType.floatArg(-90, 90))
        .setDefaultValue(0F)
        .addLabel("pitch")
        .build();

    static final ArgumentOption<World> world
        = Options.argument(ArgumentTypes.world(), "world");

    static final OptionsArgument args = OptionsArgument.builder()
        .addOptional(position)
        .addOptional(yaw)
        .addOptional(pitch)
        .addOptional(world)
        .build();

    private WarpCommand() {
      super(
          "warp",
          Permissions.WARP,
          Permissions.WARP_ADMIN,
          Arguments.WARPS,
          "Warping {0, user} to {1}"
      );

      setDescription("List all warps or warp to the specified location.");
    }

    @Override
    protected void addEditUsages(UsageFactory factory) {
      var dest = factory.withPrefix("destination");
      dest.usage("")
          .addInfo("Sets a <warp>'s destination to where you're standing");

      dest.usage("[world=<world>] [pos=<x,y,z>] [yaw=<number>] [pitch=<pitch>]")
          .addInfo("Sets the destination to the given options");
    }

    @Override
    protected LiteralArgumentBuilder<CommandSource> editArgument() {
      return literal("destination")
          .executes(c -> {
            var player = c.getSource().asPlayer();
            var warp = get(c);

            var location = player.getLocation();
            warp.setDestination(location);

            c.getSource().sendSuccess(
                Text.format("Set destination of {0} to {1}",
                    warp.displayName(),
                    Text.clickableLocation(location, true)
                )
            );
            return 0;
          })

          .then(argument("args", args)
              .executes(c -> {
                var warp = get(c);
                var parsedArgs = c.getArgument("args", ParsedOptions.class);

                var location = c.getSource().getLocation();

                if (parsedArgs.has(position)) {
                  var pos = parsedArgs.getValue(position);
                  pos.apply(location);
                }

                if (parsedArgs.has(yaw)) {
                  location.setYaw(parsedArgs.getValue(yaw));
                }

                if (parsedArgs.has(pitch)) {
                  location.setPitch(parsedArgs.getValue(pitch));
                }

                if (parsedArgs.has(world)) {
                  location.setWorld(parsedArgs.getValue(world));
                }

                warp.setDestination(location);

                c.getSource().sendSuccess(
                    Text.format("Set destination of {0} to {1, location, -clickable -world}",
                        warp.displayName(),
                        location
                    )
                );
                return 0;
              })
          );
    }

    @Override
    protected Warp create(Player player, String name, CommandContext<CommandSource> context)
        throws CommandSyntaxException
    {
      Warp warp = new Warp(name, player.getLocation());

      Permission p = Permissions.register("ftc.warps." + name);
      warp.getChecks().add(new TestPermission(p.getName()));

      return warp;
    }
  }
}