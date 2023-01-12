package net.forthecrown.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Predicate;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.challenge.Challenge;
import net.forthecrown.core.challenge.ChallengeBook;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.challenge.Challenges;
import net.forthecrown.core.challenge.ItemChallenge;
import net.forthecrown.core.challenge.ResetInterval;
import net.forthecrown.core.challenge.StreakCategory;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CommandChallenges extends FtcCommand {

  private static final Predicate<CommandSource> IS_ADMIN = source -> {
    return source.hasPermission(Permissions.CHALLENGES_ADMIN);
  };

  public CommandChallenges() {
    super("Challenges");

    setPermission(Permissions.CHALLENGES);
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

    // This lookup entry null check occurs due to new users joining This method
    // will be called async when the server is building the command packets, so
    // this may or may not be called before the firstJoining user's lookup entry
    // has been created
    UserLookupEntry entry = UserManager.get()
        .getUserLookup()
        .getEntry(player.getUniqueId());

    if (entry == null) {
      return false;
    }

    var user = Users.get(entry);

    return user.getGuild() != null;
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

    var chests = items.withPrefix("chests");

    chests.usage("")
        .addInfo("Lists all chests a <challenge> is getting items from");

    chests.usage("add [<block: x,y,z>]")
        .addInfo("Adds a chest item source to a <challenge>")
        .addInfo("If <block> is not set, the block you are looking at")
        .addInfo("is used instead");
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
                .suggests((context, builder) -> {
                  var registry = ChallengeManager.getInstance()
                      .getChallengeRegistry();

                  return CompletionProvider.suggestMatching(builder,
                      registry.entries()
                          .stream()
                          .filter(holder -> {
                            return holder.getValue() instanceof ItemChallenge;
                          })
                          .map(Holder::getKey)
                  );
                })

                .then(literal("chests")

                    // List all chests
                    .executes(this::itemsChestsList)

                    .then(literal("add")
                        // Add chest you're facing
                        .executes(c -> itemsChestsAdd(c, false))

                        .then(argument("position", PositionArgument.blockPos())
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
                      Holder<Challenge> holder
                          = c.getArgument("challenge", Holder.class);

                      var item = Commands.getHeldItem(c.getSource().asPlayer());
                      return itemsSetActive(c, item, holder);
                    })

                    .then(argument("item", UsageUtil.ITEM_ARGUMENT)
                        .executes(c -> {
                          Holder<Challenge> holder
                              = c.getArgument("challenge", Holder.class);

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
      throws CommandSyntaxException {
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

    for (var chal : manager.getActiveChallenges()) {
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

  private int itemsSetActive(CommandContext<CommandSource> c,
                             ItemStack item,
                             Holder<Challenge> holder
  ) throws CommandSyntaxException {
    ensureItemChallenge(holder);
    ItemChallenge challenge = (ItemChallenge) holder.getValue();

    challenge.setTargetItem(item);

    var container = ChallengeManager.getInstance()
        .getStorage()
        .loadContainer(holder);

    container.setActive(item);

    ChallengeManager.getInstance()
        .getStorage()
        .saveContainer(container);

    c.getSource().sendAdmin(
        Text.format("Set active item of {0} to {1, item}",
            holder.getKey(),
            item
        )
    );
    return 0;
  }

  private int itemsChestsClear(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    var storage = ChallengeManager.getInstance().getStorage();
    var container = storage.loadContainer(holder);
    container.getChests().clear();
    storage.saveContainer(container);

    c.getSource().sendAdmin(
        Text.format("Cleared item data of {0}", holder.getKey())
    );
    return 0;
  }

  private int itemsChestsList(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    Holder<Challenge> holder = c.getArgument("challenge", Holder.class);
    ensureItemChallenge(holder);

    var storage = ChallengeManager.getInstance().getStorage();
    var container = storage.loadContainer(holder);

    var writer = TextWriters.newWriter();

    var world = container.getChestWorld();
    writer.field("World", world == null ? "NOT SET" : world.getName());
    writer.field("Chest locations", "{");

    var indented = writer.withIndent();
    var it = container.getChests().listIterator();

    while (it.hasNext()) {
      int index = it.nextIndex();
      var n = it.next();

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

    var storage = ChallengeManager.getInstance().getStorage();
    var container = storage.loadContainer(holder);
    int index = c.getArgument("index", Integer.class);

    Commands.ensureIndexValid(index, container.getChests().size());
    var pos = container.getChests().remove(index - 1);

    c.getSource().sendAdmin(
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

    var storage = ChallengeManager.getInstance().getStorage();
    var container = storage.loadContainer(holder);

    Location l;

    if (posSet) {
      l = PositionArgument.getLocation(c, "position");
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

    c.getSource().sendAdmin(
        Text.format("Added item source {0, vector} to {1}",
            l,
            holder.getKey()
        )
    );
    return 0;
  }
}