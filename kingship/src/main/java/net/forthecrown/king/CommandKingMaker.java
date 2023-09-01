package net.forthecrown.king;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.Permissions;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.RoyalCrown;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class CommandKingMaker extends FtcCommand {

  private final Kingship kingship;

  public CommandKingMaker(Kingship kingship) {
    super("kingmaker");

    this.kingship = kingship;

    setPermission(Permissions.ADMIN);
    setDescription("Commands related to the king title and crown");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("save", "Forces the kingship to save");
    factory.usage("reload", "Reloads the kingship plugin");

    var king = factory.withPrefix("king");
    king.usage("").addInfo("Shows who the current king/queen is");
    king.usage("set king <player>").addInfo("Makes a <player> the server's king");
    king.usage("set queen <player>").addInfo("Makes a <player> the server's queen");
    king.usage("set monarch <player>").addInfo("Makes a <player> the server's monarch");
    king.usage("unset").addInfo("Removes the current queen/king");

    var crown = factory.withPrefix("crown");
    crown.usage("make <player>").addInfo("Creates a crown that belongs to a <player>");
    crown.usage("levelup").addInfo("Levels up the crown you're holding");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("reload")
            .executes(c -> {
              kingship.load();
              c.getSource().sendSuccess(Component.text("Kingship plugin reloaded"));
              return 0;
            })
        )

        .then(literal("save")
            .executes(c -> {
              kingship.save();
              c.getSource().sendSuccess(Component.text("Kingship plugin saved"));
              return 0;
            })
        )

        .then(literal("king")
            .executes(c -> {
              if (!kingship.hasMonarch()) {
                throw Exceptions.create("No king set");
              }

              User user = kingship.getMonarch();

              c.getSource().sendMessage(
                  Text.format("The current {0} is &e{1, user}&r!",
                      NamedTextColor.GRAY,
                      kingship.getTitle(),
                      user
                  )
              );
              return 0;
            })

            .then(literal("set")
                .then(argument("gender", ArgumentTypes.enumType(MonarchGender.class))
                    .then(argument("user", Arguments.USER)
                        .executes(this::setKing)
                    )
                )
            )

            .then(literal("unset")
                .executes(this::unsetKing)
            )
        )

        .then(literal("crown")
            .then(literal("make")
                .then(argument("user", Arguments.USER)
                    .executes(this::makeCrown)
                )
            )

            .then(literal("levelup")
                .executes(this::levelupCrown)
            )
        );
  }

  private int levelupCrown(CommandContext<CommandSource> c) throws CommandSyntaxException {
    var player = c.getSource().asPlayer();

    ItemStack held = Commands.getHeldItem(player);
    RoyalCrown crown = ExtendedItems.CROWN.get(held);

    if (crown == null) {
      throw Exceptions.create("Not holding a royal sword");
    }

    crown.upgrade(held);

    c.getSource().sendSuccess(Component.text("Upgraded held crown", NamedTextColor.GRAY));
    return 0;
  }

  private int makeCrown(CommandContext<CommandSource> c) throws CommandSyntaxException {
    User target = Arguments.getUser(c, "user");
    ItemStack item = ExtendedItems.CROWN.createItem(target.getUniqueId());

    var player = c.getSource().asPlayer();
    player.getInventory().addItem(item);

    c.getSource().sendSuccess(
        Text.format("Created &f{0, item, -amount}&r for &e{1, user}&r!",
            NamedTextColor.GRAY,
            item, target
        )
    );
    return 0;
  }

  private int unsetKing(CommandContext<CommandSource> c) throws CommandSyntaxException {
    if (!kingship.hasMonarch()) {
      throw Exceptions.create("There is already no king/queen");
    }

    User user = kingship.getMonarch();
    kingship.setMonarchId(null);

    if (user.isOnline()) {
      user.updateTabName();
    }

    c.getSource().sendSuccess(Component.text("Removed the current queen/king"));
    return 0;
  }

  private int setKing(CommandContext<CommandSource> c) throws CommandSyntaxException {
    MonarchGender gender = c.getArgument("gender", MonarchGender.class);

    User target = Arguments.getUser(c, "user");

    kingship.setMonarchId(target.getUniqueId());
    kingship.setGender(gender);

    if (target.isOnline()) {
      target.updateTabName();
    }

    c.getSource().sendSuccess(
        Text.format("Made &e{0, user}&r a &e{1}&r!",
            NamedTextColor.GRAY,
            target,
            kingship.getTitle()
        )
    );
    return 0;
  }
}
