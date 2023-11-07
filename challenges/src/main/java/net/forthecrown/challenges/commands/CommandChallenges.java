package net.forthecrown.challenges.commands;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Predicate;
import net.forthecrown.Loggers;
import net.forthecrown.challenges.Challenge;
import net.forthecrown.challenges.ChallengeBook;
import net.forthecrown.challenges.ChallengeExceptions;
import net.forthecrown.challenges.ChallengeManager;
import net.forthecrown.challenges.ChallengePermissions;
import net.forthecrown.challenges.Challenges;
import net.forthecrown.challenges.ItemChallenge;
import net.forthecrown.challenges.ResetInterval;
import net.forthecrown.challenges.StreakCategory;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CommandChallenges extends FtcCommand {

  private static final Predicate<CommandSource> IS_ADMIN = source -> {
    return source.hasPermission(ChallengePermissions.CHALLENGES_ADMIN);
  };
  
  private final ChallengeManager manager;

  private final RegistryArguments<Challenge> challengeArgument;

  public CommandChallenges(ChallengeManager manager) {
    super("Challenges");

    this.manager = manager;
    this.challengeArgument = new RegistryArguments<>(manager.getChallengeRegistry(), "Challenge");

    setPermission(ChallengePermissions.CHALLENGES);
    setDescription("Opens the challenge book");

    register();
  }

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

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", getDescription());

    factory = factory.withCondition(IS_ADMIN);

    factory.usage("list", "Lists all loaded challenges");
    factory.usage("list_active", "Lists all active challenges");

    factory.usage("give_points <challenge> <player> [<points: number(1..)>]")
        .addInfo("Gives [points] for a <challenge> to a <player>")
        .addInfo("If [points] is not set, defaults to 1");

    factory.usage("trigger <challenge> <player>")
        .addInfo("Triggers a <challenge> for a <player>")
        .addInfo("What 'triggering' means, varies based on implementation");

    factory.usage("complete_all <category> <user>")
        .addInfo("Completes all active challenges in a <category> for a")
        .addInfo("<user>");

    factory.usage("reset [<category>]")
        .addInfo("Resets all challenges in a [category]. If a [category] is")
        .addInfo("not set, it resets all categories");

    var items = factory.withPrefix("items <challenge>");
    items.usage("set_active [<item>]")
        .addInfo("Sets an [item] to be a <challenge>'s active item")
        .addInfo("If [item] is not set, then your held item is used");

    items.usage("reroll").addInfo("Forces a <challenge> to re-roll it's chosen item");

    var chests = items.withPrefix("chests");

    chests.usage("")
        .addInfo("Lists all chests a <challenge> is getting items from")
        .addInfo("Note:")
        .addInfo("Chests are the 'item sources' from which item challenges")
        .addInfo("pick their items at random. These chests have to exist in")
        .addInfo("in the actual world");

    chests.usage("add [<block: x,y,z>]")
        .addInfo("Adds a chest item source to a <challenge>")
        .addInfo("If <block> is not set, the block you are looking at")
        .addInfo("is used instead");

    chests.usage("remove <index>")
        .addInfo("Removes a chest item source from a <challenge> at <index>")
        .addInfo("To find the <index> do /challenges items <challenge> chests");

    chests.usage("clear")
        .addInfo("Clears all chest item sources from a <challenge>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          var user = getUserSender(c);
          ChallengeBook.open(user);

          return 0;
        })

        .then(literal("list")
            .requires(IS_ADMIN)

            .executes(c -> {
              var it = manager
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
              var it = manager
                  .getActiveChallenges()
                  .listIterator();

              TextWriter writer = TextWriters.newWriter();
              var user = getUserSender(c);

              while (it.hasNext()) {
                var next = it.next();

                writer.formattedLine(
                    "{0}) {1}",
                    it.nextIndex(),
                    next.getValue().displayName(user)
                );
              }

              c.getSource().sendMessage(writer);
              return 0;
            })
        )

        .then(literal("items")
            .requires(IS_ADMIN)

            .then(argument("challenge", challengeArgument)
                .suggests((context, builder) -> {
                  var registry = manager.getChallengeRegistry();

                  return Completions.suggest(builder,
                      registry.entries()
                          .stream()
                          .filter(holder -> {
                            return holder.getValue() instanceof ItemChallenge;
                          })
                          .map(Holder::getKey)
                  );
                })

                .then(literal("reroll")
                    .executes(this::itemsReroll)
                )

                .then(literal("chests")

                    // List all chests
                    .executes(this::itemsChestsList)

                    .then(literal("add")
                        // Add chest you're facing
                        .executes(c -> itemsChestsAdd(c, false))

                        .then(argument("position", ArgumentTypes.blockPosition())
                            // Add chest at given block
                            .executes(c -> itemsChestsAdd(c, true))
                        )
                    )

                    // Removes a chest at an index
                    .then(literal("remove")
                        .then(argument("index", IntegerArgumentType.integer(1))
                            .executes(this::itemsChestsRemove)
                        )
                    )

                    // Clears all chests
                    .then(literal("clear")
                        .executes(this::itemsChestsClear)
                    )
                )

                .then(literal("set_active")
                    .executes(c -> {
                      Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
                      var item = Commands.getHeldItem(c.getSource().asPlayer());
                      return itemsSetActive(c, item, holder);
                    })

                    .then(argument("item", Arguments.ITEM_LIST)
                        .executes(c -> {
                          Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
                          var item = c.getArgument("item", ItemStack.class);
                          return itemsSetActive(c, item, holder);
                        })
                    )
                )
            )
        )

        .then(literal("reset")
            .requires(IS_ADMIN)

            .executes(c -> {
              manager.reset(ResetInterval.DAILY);
              manager.reset(ResetInterval.WEEKLY);
              manager.reset(ResetInterval.MANUAL);

              c.getSource().sendSuccess(
                  Text.format("Reset all challenges")
              );
              return 0;
            })

            .then(argument("type", ArgumentTypes.enumType(ResetInterval.class))
                .executes(c -> {
                  var type = c.getArgument("type", ResetInterval.class);
                  manager
                      .reset(type);

                  c.getSource().sendSuccess(
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

            .then(argument("category", ArgumentTypes.enumType(StreakCategory.class))
                .then(argument("user", Arguments.ONLINE_USER)
                    .executes(this::completeAll)
                )
            )
        )

        .then(literal("give_points")
            .requires(IS_ADMIN)

            .then(argument("challenge", challengeArgument)
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

            .then(argument("challenge", challengeArgument)
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
      throw ChallengeExceptions.nonActiveChallenge(holder.getValue(), user);
    }

    holder.getValue()
        .trigger(user.getPlayer());

    c.getSource().sendSuccess(
        Text.format("Invoking {0} for {1, user}",
            holder.getValue().displayName(user),
            user
        )
    );
    return 0;
  }

  private int givePoints(CommandContext<CommandSource> c, float points)
      throws CommandSyntaxException
  {
    User user = Arguments.getUser(c, "user");
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);

    if (!Challenges.isActive(holder.getValue())) {
      throw ChallengeExceptions.nonActiveChallenge(holder.getValue(), user);
    }

    manager
        .getEntry(user)
        .addProgress(holder, points);

    c.getSource().sendSuccess(
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
    var streak = c.getArgument("category", StreakCategory.class);

    var entry = manager.getEntry(user);

    for (var chal : manager.getActiveChallenges()) {
      if (chal.getValue().getStreakCategory() != streak) {
        continue;
      }

      entry.addProgress(chal, chal.getValue().getGoal(user));
    }

    c.getSource().sendSuccess(
        Text.format("Completed all {0} challenges for {1, user}",
            streak,
            user
        )
    );

    return 0;
  }

  /* ------------------------------- ITEMS -------------------------------- */

  private int itemsSetActive(CommandContext<CommandSource> c,
                             ItemStack item,
                             Holder<Challenge> holder
  ) throws CommandSyntaxException {
    item = item.clone();

    ensureItemChallenge(holder);
    ItemChallenge challenge = (ItemChallenge) holder.getValue();

    challenge.setTargetItem(item);

    var container = manager
        .getStorage()
        .loadContainer(holder);

    container.setActive(item);

    manager
        .getStorage()
        .saveContainer(container);

    c.getSource().sendSuccess(
        Text.format("Set active item of {0} to {1, item}",
            holder.getKey(),
            item
        )
    );
    return 0;
  }

  private int itemsReroll(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    ItemChallenge challenge = (ItemChallenge) holder.getValue();;
    challenge.activate(true).whenComplete((s, throwable) -> {
         if (throwable != null) {
           Loggers.getLogger().error(
               "Error rerolling item for {}",
               holder.getKey(),
               throwable
           );

           c.getSource().sendFailure(
               text("Error rerolling item, check console")
           );
           return;
         }

         if (Strings.isNullOrEmpty(s)) {
           c.getSource().sendFailure(
               text("Failed to reroll: no valid item found")
           );
           return;
         }

         c.getSource().sendSuccess(
             Text.format("Re-rolled {0}'s target item to {1}",
                 holder.getKey(),
                 s
             )
         );
    });

    return 0;
  }

  private int itemsChestsClear(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    var storage = manager.getStorage();
    var container = storage.loadContainer(holder);
    container.getChests().clear();
    storage.saveContainer(container);

    c.getSource().sendSuccess(
        Text.format("Cleared item data of {0}", holder.getKey())
    );
    return 0;
  }

  private int itemsChestsList(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    var storage = manager.getStorage();
    var container = storage.loadContainer(holder);

    var writer = TextWriters.newWriter();

    var world = container.getChestWorld();
    writer.field("World", world == null ? "NOT SET" : world.getName());
    writer.field("Chest locations", "{");

    var indented = writer.withIndent();
    var it = container.getChests().listIterator();

    while (it.hasNext()) {
      var n = it.next();
      int index = it.nextIndex();

      indented.field(index + "", n);
    }

    writer.line("}");

    c.getSource().sendMessage(writer);
    return 0;
  }

  private int itemsChestsRemove(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    var storage = manager.getStorage();
    var container = storage.loadContainer(holder);
    int index = c.getArgument("index", Integer.class);

    Commands.ensureIndexValid(index, container.getChests().size());
    var pos = container.getChests().remove(index - 1);

    c.getSource().sendSuccess(
        Text.format(
            "Removed item source at {0} (index: {1, number}) from {2}",
            pos, index, holder.getKey()
        )
    );
    return 0;
  }

  private int itemsChestsAdd(CommandContext<CommandSource> c, boolean posSet)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    var storage = manager.getStorage();
    var container = storage.loadContainer(holder);

    Location l;

    if (posSet) {
      l = ArgumentTypes.getLocation(c, "position");
    } else {
      var player = c.getSource().asPlayer();
      var facingBlock = player.getTargetBlockExact(5);

      if (facingBlock == null || facingBlock.isEmpty()) {
        throw Exceptions.format("Not looking at a container block");
      }

      l = facingBlock.getLocation();
    }

    var block = l.getBlock();
    if (!(block.getState() instanceof InventoryHolder)) {
      throw Exceptions.format("Not facing a container block");
    }

    container.getChests().add(Vectors.intFrom(l));

    if (container.getChestWorld() == null) {
      container.setChestWorld(l.getWorld());
    }

    storage.saveContainer(container);

    c.getSource().sendSuccess(
        Text.format("Added item source {0, vector} to {1}",
            l,
            holder.getKey()
        )
    );
    return 0;
  }
}