package net.forthecrown.core.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.InventoryStorage;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.InventoryStorageImpl;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandInvStore extends FtcCommand {

  public CommandInvStore() {
    super("InvStore");

    setPermission(CorePermissions.CMD_INVSTORE);
    setDescription("Lets you give players separate inventories");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage(
        "reload",
        "Reloads stored inventories"
    );

    factory.usage(
        "save",
        "Saves all currently stored inventories to disk"
    );

    factory.usage(
        "save <player> <category: quoted string> [-doNotClear]",

        "Saves a <player>'s inventory in a <category>",
        "If the '-doNotClear' flag is not set, the player's",
        "inventory is cleared after it's stored away"
    );

    factory.usage(
        "return <player> <category: quoted string>",

        "Returns all items a <player> has stored in a <category>",
        "This command will also remove the items from storage"
    );

    factory.usage(
        "swap <player> <category: quoted string>",

        "Swaps a <player>'s current inventory with the one stored in",
        "<category>. If the player has no stored inventory in <category>",
        "then no items are returned.",
        "In either case, the player's current items are saved in",
        "the <category>"
    );

    factory.usage(
        "give <player> <category: quoted string>",

        "Works like the 'return' argument, except, it doesn't",
        "remove the items from storage"
    );
  }

  private static final SuggestionProvider<CommandSource> SUGGEST_CATEGORIES
      = (context, builder) -> {
          var user = Arguments.getUser(context, "user");
          var categories = InventoryStorage.getInstance()
              .getExistingCategories(user.getPlayer())
              .stream()
              .map(CommandInvStore::wrapIfNeeded);

          return Completions.suggest(builder, categories);
        };

  private static String wrapIfNeeded(String s) {
    for (var c: s.toCharArray()) {
      if (!StringReader.isAllowedInUnquotedString(c)) {
        return "'" + s + "'";
      }
    }

    return s;
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("reload")
            .executes(c -> {
              InventoryStorageImpl.getStorage().load();
              c.getSource().sendSuccess(Component.text("Reloaded SavedInventories from disk"));
              return 0;
            })
        )

        .then(literal("save")
            .executes(c -> {
              InventoryStorageImpl.getStorage().save();
              c.getSource().sendSuccess(Component.text("Saved SavedInventories to disk"));
              return 0;
            })

            .then(argument("user", Arguments.ONLINE_USER)
                .then(argument("category", StringArgumentType.string())
                    .suggests(SUGGEST_CATEGORIES)

                    .executes(c -> storeInventory(c, true))

                    .then(literal("-doNotClear")
                        .executes(c -> storeInventory(c, false))
                    )
                )
            )
        )

        .then(literal("return")
            .then(argument("user", Arguments.ONLINE_USER)
                .then(argument("category", StringArgumentType.string())
                    .suggests(SUGGEST_CATEGORIES)
                    .executes(this::returnItems)
                )
            )
        )

        .then(literal("give")
            .then(argument("user", Arguments.ONLINE_USER)
                .then(argument("category", StringArgumentType.string())
                    .suggests(SUGGEST_CATEGORIES)
                    .executes(c -> giveItems(c))
                )
            )
        )

        .then(literal("swap")
            .then(argument("category", StringArgumentType.greedyString())
                .suggests(SUGGEST_CATEGORIES)
                .executes(c -> storeAndReturn(c))
            )
        );
  }

  private int storeInventory(CommandContext<CommandSource> c, boolean clearAfter)
      throws CommandSyntaxException
  {
    InventoryStorage store = InventoryStorage.getInstance();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    if (store.hasStoredInventory(user.getPlayer(), category)) {
      throw Exceptions.format(
          "Player {0, user} already has a stored inventory in {1}",
          user, category
      );
    }

    store.storeInventory(user.getPlayer(), category);

    if (clearAfter) {
      user.getInventory().clear();
    }

    c.getSource().sendSuccess(
        Text.format("Stored &e{0, user}&r's inventory in category &e{1}&r.",
            NamedTextColor.GRAY,
            user, category
        )
    );
    return 0;
  }

  private int returnItems(CommandContext<CommandSource> c) throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getInstance();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    if (!store.hasStoredInventory(user.getPlayer(), category)) {
      throw Exceptions.format(
          "Player {0, user} has NO stored inventory in category {1}",
          user, category
      );
    }

    store.returnItems(user.getPlayer(), category);

    c.getSource().sendSuccess(
        Text.format(
            "&7Returned items from category &e{0}&r to player &e{1, user}&r.",
            NamedTextColor.GRAY,
            category, user
        )
    );
    return 0;
  }

  private int storeAndReturn(CommandContext<CommandSource> c) throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getInstance();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    store.swap(user.getPlayer(), category);

    c.getSource().sendSuccess(
        Text.format(
            "Swapped &e{0, user}&r's inventory with the stored "
                + "inventory in category &e{1}&r.",
            NamedTextColor.GRAY,

            user, category
        )
    );
    return 0;
  }

  private int giveItems(CommandContext<CommandSource> c) throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getInstance();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    if (!store.giveItems(user.getPlayer(), category)) {
      throw Exceptions.format(
          "Player {0, user} has NO stored inventory in category {1}",
          user, category
      );
    }

    c.getSource().sendSuccess(
        Text.format(
            "Gave &e{0, user}&r their items back from category &e{1}&r.",
            NamedTextColor.GRAY,
            user, category
        )
    );
    return 0;
  }

  private User getUser(CommandContext<CommandSource> c) throws CommandSyntaxException {
    return Arguments.getUser(c, "user");
  }
}