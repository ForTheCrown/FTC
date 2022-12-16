package net.forthecrown.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.challenge.*;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Predicate;

public class CommandChallenges extends FtcCommand {

    public CommandChallenges() {
        super("Challenges");

        setPermission(Permissions.CHALLENGES);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Challenges
     *
     * Permissions used:
     *
     * Main Author:
     */

    private static final Predicate<CommandSource> IS_ADMIN = source -> {
        return source.hasPermission(Permissions.CHALLENGES_ADMIN);
    };

    @Override
    public boolean test(CommandSource source) {
        if (!super.test(source)) {
            return false;
        }

        if (!source.isPlayer()
                || source.hasPermission(Permissions.CHALLENGES_ADMIN)
        ) {
            return true;
        }

        var player = source.asPlayerOrNull();
        var user = Users.get(player);

        return user.getGuild() != null;
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    var user = getUserSender(c);
                    ChallengeBook.open(user);

                    return 0;
                })

                .then(literal("list")
                        .requires(IS_ADMIN)

                        .executes(c -> {
                            var it = ChallengeManager.getInstance()
                                    .getChallengeRegistry()
                                    .entries()
                                    .iterator();

                            TextWriter writer = TextWriters.newWriter();
                            var user = getUserSender(c);

                            while (it.hasNext()) {
                                var next = it.next();

                                writer.formattedLine(
                                        "key={0} id={1}, name={2}",
                                        next.getKey(),
                                        next.getId(),
                                        next.getValue().displayName(user)
                                );
                            }

                            c.getSource().sendMessage(writer);
                            return 0;
                        })
                )

                .then(literal("list_active")
                        .requires(IS_ADMIN)

                        .executes(c -> {
                            var it = ChallengeManager.getInstance()
                                    .getActiveChallenges()
                                    .listIterator();

                            TextWriter writer = TextWriters.newWriter();
                            var user = getUserSender(c);

                            while (it.hasNext()) {
                                var next = it.next();

                                writer.formattedLine(
                                        "{0}) {1}",
                                        it.nextIndex(),
                                        next.displayName(user)
                                );
                            }

                            c.getSource().sendMessage(writer);
                            return 0;
                        })
                )

                .then(literal("items")
                        .requires(IS_ADMIN)

                        .then(argument("challenge", RegistryArguments.CHALLENGE)
                                .then(literal("list")
                                        .executes(c -> itemsList(c, false))

                                        .then(literal("-with_nbt")
                                                .executes(c -> itemsList(c, true))
                                        )
                                )

                                .then(literal("remove")
                                        .then(argument("index", IntegerArgumentType.integer(1))
                                                .executes(this::itemsRemove)
                                        )
                                )

                                .then(literal("fill")
                                        .executes(this::itemsFill)
                                )

                                .then(literal("clear")
                                        .executes(this::itemsClear)
                                )
                        )
                )

                .then(literal("reset")
                        .requires(IS_ADMIN)

                        .executes(c -> {
                            var manager = ChallengeManager.getInstance();
                            manager.reset(ResetInterval.DAILY);
                            manager.reset(ResetInterval.WEEKLY);
                            manager.reset(ResetInterval.MANUAL);

                            c.getSource().sendAdmin(
                                    Text.format("Reset all challenges")
                            );
                            return 0;
                        })

                        .then(argument("type", EnumArgument.of(ResetInterval.class))
                                .executes(c -> {
                                    var type = c.getArgument("type", ResetInterval.class);
                                    ChallengeManager.getInstance()
                                            .reset(type);

                                    c.getSource().sendAdmin(
                                            Text.format("Reset all {0} challenges",
                                                    type.getDisplayName()
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("complete_all")
                        .requires(IS_ADMIN)

                        .then(argument("category", EnumArgument.of(StreakCategory.class))
                                .then(argument("user", Arguments.ONLINE_USER)
                                        .executes(this::completeAll)
                                )
                        )
                )

                .then(literal("give_points")
                        .requires(IS_ADMIN)

                        .then(argument("challenge", RegistryArguments.CHALLENGE)
                                .then(argument("user", Arguments.ONLINE_USER)
                                        .executes(c -> givePoints(c, 1))

                                        .then(argument("points", FloatArgumentType.floatArg(1))
                                                .executes(c -> {
                                                    float points = c.getArgument("points", Float.class);
                                                    return givePoints(c, points);
                                                })
                                        )
                                )
                        )
                )

                .then(literal("trigger")
                        .requires(IS_ADMIN)

                        .then(argument("challenge", RegistryArguments.CHALLENGE)
                                .then(argument("user", Arguments.ONLINE_USER)
                                        .executes(this::trigger)
                                )
                        )
                );
    }

    private int trigger(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        User user = Arguments.getUser(c, "user");
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);

        if (!Challenges.isActive(holder.getValue())) {
            throw Exceptions.nonActiveChallenge(holder.getValue());
        }

        holder.getValue()
                .trigger(user.getPlayer());

        c.getSource().sendAdmin(
                Text.format("Invoking {0} for {1, user}",
                        holder.getValue().displayName(user),
                        user
                )
        );
        return 0;
    }

    private int givePoints(CommandContext<CommandSource> c,
                           float points
    ) throws CommandSyntaxException {
        User user = Arguments.getUser(c, "user");
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);

        if (!Challenges.isActive(holder.getValue())) {
            throw Exceptions.nonActiveChallenge(holder.getValue());
        }

        ChallengeManager.getInstance()
                .getOrCreateEntry(user.getUniqueId())
                .addProgress(holder, points);

        c.getSource().sendAdmin(
                Text.format("Gave &e{0, user} &6{1, number}&r points for &f{2}&r.",
                        NamedTextColor.GRAY,
                        user,
                        points,
                        holder.getValue().displayName(user)
                )
        );
        return 0;
    }

    private int completeAll(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        var user = Arguments.getUser(c, "user");
        var manager = ChallengeManager.getInstance();
        var streak = c.getArgument("category", StreakCategory.class);

        var entry = manager.getOrCreateEntry(user.getUniqueId());

        for (var chal: manager.getActiveChallenges()) {
            if (chal.getStreakCategory() != streak) {
                continue;
            }

            Challenges.apply(chal, holder -> {
                entry.addProgress(holder, chal.getGoal(user));
            });
        }

        c.getSource().sendAdmin(
                Text.format("Completed all {0} challenges for {1, user}",
                        streak,
                        user
                )
        );

        return 0;
    }

    /* ------------------------------- ITEMS -------------------------------- */

    private static void ensureItemChallenge(Holder<Challenge> holder)
            throws CommandSyntaxException
    {
        if (holder.getValue() instanceof ItemChallenge) {
            return;
        }

        throw Exceptions.format("{0} is not an item challenge",
                holder.getKey()
        );
    }

    private int itemsRemove(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        int index = IntegerArgumentType.getInteger(c, "index");
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
        ensureItemChallenge(holder);

        ChallengeItemContainer container = ChallengeManager.getInstance()
                .getStorage()
                .loadContainer(holder);

        Commands.ensureIndexValid(index, container.getPotentials().size());

        var removed = container.getPotentials().remove(index - 1);

        c.getSource().sendAdmin(
                Text.format(
                        "Removed challenge item {0, item} at index {1, number}",
                        removed, index
                )
        );
        return 0;
    }

    private int itemsList(CommandContext<CommandSource> c, boolean addNbtTag)
            throws CommandSyntaxException
    {
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
        ensureItemChallenge(holder);

        ChallengeItemContainer container = ChallengeManager.getInstance()
                .getStorage()
                .loadContainer(holder);

        if (container.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        var it = container.getPotentials().listIterator();
        while (it.hasNext()) {
            var n = it.next();

            Component line = Text.format(
                    "{0, number}) {1, item}",
                    it.nextIndex(), n
            );

            if (addNbtTag) {
                line = line
                        .append(Component.text(": "))
                        .append(Text.displayTag(ItemStacks.save(n), false));
            }

            c.getSource().sendMessage(line);
        }

        return 0;
    }

    private int itemsFill(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        var player = c.getSource().asPlayer();
        var target = player.getTargetBlock(5);

        if (target == null
                || !(target.getState() instanceof InventoryHolder invHolder)
        ) {
            throw Exceptions.format("Not looking at an inventory-holder block");
        }

        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
        ensureItemChallenge(holder);
        ItemChallenge item = (ItemChallenge) holder.getValue();

        var storage = ChallengeManager.getInstance().getStorage();
        var container = storage.loadContainer(holder);

        container.fillFrom(invHolder.getInventory());

        if (item.getTargetItem().isEmpty()) {
            var next = container.next(Util.RANDOM);
            container.setActive(next);
            container.getUsed().add(next);
            item.setTargetItem(next);
        }

        storage.saveContainer(container);

        c.getSource().sendAdmin(
                Text.format("Filled {0}'s potential items", holder.getKey())
        );
        return 0;
    }

    private int itemsClear(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
        ensureItemChallenge(holder);

        var storage = ChallengeManager.getInstance()
                .getStorage();

        var container = storage.loadContainer(holder);
        container.clear();
        storage.saveContainer(container);

        c.getSource().sendAdmin(
                Text.format("Cleared item container of {0}",
                        holder.getKey()
                )
        );
        return 0;
    }
}