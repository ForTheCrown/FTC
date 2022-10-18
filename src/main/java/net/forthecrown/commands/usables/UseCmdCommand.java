package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.UseCmdArgument;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.grenadier.types.item.ItemArgument;
import net.forthecrown.grenadier.types.item.ParsedItemStack;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.useables.command.CommandUsable;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.useables.command.Warp;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

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

        // Create list command too, name only requires
        // s at the end to pluralize it
        new CommandUseCmdList<>(name, argument);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .requires(source -> source.hasPermission(adminPermission))

                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> {
                                    var player = c.getSource().asPlayer();
                                    var t = create(player, c.getArgument("name", String.class), c);

                                    argument.getManager().add(t);

                                    c.getSource().sendAdmin(
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

                            t.interact(c.getSource().asPlayer());

                            return 0;
                        })

                        .then(argument("user", Arguments.ONLINE_USER)
                                .requires(source -> source.hasPermission(adminPermission))

                                .executes(c -> {
                                    var t = argument.get(c, "usable");
                                    var user = Arguments.getUser(c, "user");

                                    t.onInteract(user.getPlayer());

                                    c.getSource().sendAdmin(
                                            Text.format(otherUserFormat,
                                                    user,
                                                    t.displayName()
                                            )
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("edit")
                                .requires(source -> source.hasPermission(Permissions.ADMIN))

                                .then(UsableCommands.CHECK_NODE.createArguments(this::get))
                                .then(editArgument())
                        )
                );
    }

    protected T get(CommandContext<CommandSource> c) {
        return argument.get(c, "usable");
    }

    protected abstract LiteralArgumentBuilder<CommandSource> editArgument();
    protected abstract T create(Player player, String name, CommandContext<CommandSource> context) throws CommandSyntaxException;

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

                        c.getSource().sendAdmin(
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

                                for (var i: kit.getItems()) {
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

                                c.getSource().sendAdmin(
                                        Text.format(
                                                "Added {0, item} to {1}",
                                                heldItem,
                                                kit.displayName()
                                        )
                                );
                                return 0;
                            })

                            // Add item by parsed value and quantity
                            .then(argument("item", ItemArgument.itemStack())
                                    .then(argument("amount", IntegerArgumentType.integer(1, 64))
                                            .executes(c -> {
                                                var item = c.getArgument("item", ParsedItemStack.class);
                                                var amount = c.getArgument("amount", Integer.class);

                                                var stack = item.create(amount, true);

                                                var kit = get(c);
                                                kit.getItems().add(stack);

                                                c.getSource().sendAdmin(
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

                                        c.getSource().sendAdmin(
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
        protected Kit create(Player player, String name, CommandContext<CommandSource> context) throws CommandSyntaxException {
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
        static final Argument<Position> position = Argument.of("pos", PositionArgument.position());
        static final Argument<Float> yaw = Argument.of("yaw", FloatArgumentType.floatArg(-180, 180), 0f);
        static final Argument<Float> pitch = Argument.of("pitch", FloatArgumentType.floatArg(-90, 90), 0f);
        static final Argument<World> world = Argument.of("world", WorldArgument.world());

        static final ArgsArgument args = ArgsArgument.builder()
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
        }

        @Override
        protected LiteralArgumentBuilder<CommandSource> editArgument() {
            return literal("destination")
                    .executes(c -> {
                        var player = c.getSource().asPlayer();
                        var warp = get(c);

                        var location = player.getLocation();
                        warp.setDestination(location);

                        c.getSource().sendAdmin(
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
                                var parsedArgs = c.getArgument("args", ParsedArgs.class);

                                var location = c.getSource().getLocation();

                                if (parsedArgs.has(position)) {
                                    var pos = parsedArgs.get(position);
                                    pos.apply(location);
                                }

                                location.setYaw(parsedArgs.getOrDefault(yaw, location.getYaw()));
                                location.setPitch(parsedArgs.getOrDefault(pitch, location.getPitch()));
                                location.setWorld(parsedArgs.getOrDefault(world, location.getWorld()));

                                warp.setDestination(location);

                                c.getSource().sendAdmin(
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
        protected Warp create(Player player, String name, CommandContext<CommandSource> context) throws CommandSyntaxException {
            return new Warp(name, player.getLocation());
        }
    }
}