package net.forthecrown.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.InventoryStorage;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandInvStore extends FtcCommand {

  public CommandInvStore() {
    super("InvStore");

    setPermission(Permissions.ADMIN);
    setDescription("Lets you give players separate inventories");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage(
        "save <player> <category: quoted string> [-doNotClear]",

        "Saves a <player>'s inventory in a <category>",
        "If the '-doNotClear' flag is not set, the player's",
        "inventory is cleared after it's stored away"
    );

    factory.usage(
        "return <player> <category: quoted string> [-doNotClear]",

        "Returns all items a <player> has stored in a <category>",
        "If the '-doNotClear' flag is not set, the player's inventory",
        "is cleared before any items are returned",
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
        "give <player> <category: quoted string> [-doNotClear]",

        "Works like the 'return' argument, except, it doesn't",
        "remove the items from storage"
    );
  }

  private static final SuggestionProvider<CommandSource> SUGGEST_CATEGORIES
      = (context, builder) -> {
          var user = Arguments.getUser(context, "user");
          var categories = InventoryStorage.getStorage()
              .getExistingCategories(user.getPlayer())
              .stream()
              .map(CommandInvStore::wrapIfNeeded);

          return CompletionProvider.suggestMatching(builder, categories);
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
  protected void createCommand(BrigadierCommand command) {
    command
        .then(literal("save")
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

                    .executes(c -> returnItems(c, true))

                    .then(literal("-doNotClear")
                        .executes(c -> returnItems(c, false))
                    )
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

  private int storeInventory(CommandContext<CommandSource> c,
                             boolean clearAfter
  ) throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getStorage();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    if (store.hasStoredInventory(user.getPlayer(), category)) {
      throw Exceptions.format(
          "Player {0, user} already has a stored inventory in {1}",
          user, category
      );
    }

    store.storeInventory(user.getPlayer(), category, clearAfter);

    c.getSource().sendAdmin(
        Text.format("Stored &e{0, user}&r's inventory in category &e{1}&r.",
            NamedTextColor.GRAY,
            user, category
        )
    );
    return 0;
  }

  private int returnItems(CommandContext<CommandSource> c,
                          boolean clearCurrent
  ) throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getStorage();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    if (!store.hasStoredInventory(user.getPlayer(), category)) {
      throw Exceptions.format(
          "Player {0, user} has NO stored inventory in category {1}",
          user, category
      );
    }

    store.returnItems(user.getPlayer(), category, clearCurrent);

    c.getSource().sendAdmin(
        Text.format(
            "&7Returned items from category &e{0}&r to player &e{1, user}&r.",
            NamedTextColor.GRAY,
            category, user
        )
    );
    return 0;
  }

  private int storeAndReturn(CommandContext<CommandSource> c)
      throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getStorage();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    store.swap(user.getPlayer(), category);

    c.getSource().sendAdmin(
        Text.format(
            "Swapped &e{0, user}&r's inventory with the stored "
                + "inventory in category &e{1}&r.",
            NamedTextColor.GRAY,

            user, category
        )
    );
    return 0;
  }

  private int giveItems(CommandContext<CommandSource> c)
      throws CommandSyntaxException {
    InventoryStorage store = InventoryStorage.getStorage();

    User user = getUser(c);
    String category = c.getArgument("category", String.class);

    if (!store.giveItems(user.getPlayer(), category)) {
      throw Exceptions.format(
          "Player {0, user} has NO stored inventory in category {1}",
          user, category
      );
    }

    c.getSource().sendAdmin(
        Text.format(
            "Gave &e{0, user}&r their items back from category &e{1}&r.",
            NamedTextColor.GRAY,
            user, category
        )
    );
    return 0;
  }

  private User getUser(CommandContext<CommandSource> c)
      throws CommandSyntaxException {
    return Arguments.getUser(c, "user");
  }
}