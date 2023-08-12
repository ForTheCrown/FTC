package net.forthecrown.commands.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.RoyalCrown;
import net.forthecrown.user.Kingship;
import net.forthecrown.user.Kingship.GenderPreference;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class CommandKingMaker extends FtcCommand {

  public CommandKingMaker() {
    super("kingmaker");
    setPermission(Permissions.ADMIN);
    setDescription("Commands related to the king title and crown");
    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
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
        .then(literal("king")
            .executes(c -> {
              var kingship = UserManager.get().getKingship();
              if (kingship.getKingId() == null) {
                throw Exceptions.create("No king set");
              }

              User user = Users.get(kingship.getKingId());

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
                .then(literal("king")
                    .then(argument("user", Arguments.USER)
                        .executes(c -> setKing(c, GenderPreference.KING))
                    )
                )

                .then(literal("monarch")
                    .then(argument("user", Arguments.USER)
                        .executes(c -> setKing(c, GenderPreference.MONARCH))
                    )
                )

                .then(literal("queen")
                    .then(argument("user", Arguments.USER)
                        .executes(c -> setKing(c, GenderPreference.QUEEN))
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
    Kingship kingship = UserManager.get().getKingship();

    if (!kingship.hasKing()) {
      throw Exceptions.create("There is already no king/queen");
    }

    UUID previous = kingship.getKingId();
    User user = Users.get(previous);

    if (user.isOnline()) {
      user.updateTabName();
    }

    kingship.setKingId(null);

    c.getSource().sendSuccess(Component.text("Removed the current queen/king"));
    return 0;
  }

  private int setKing(CommandContext<CommandSource> c, GenderPreference pref)
      throws CommandSyntaxException
  {
    User target = Arguments.getUser(c, "user");
    Kingship kingship = UserManager.get().getKingship();

    kingship.setKingId(target.getUniqueId());
    kingship.setPreference(pref);

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
