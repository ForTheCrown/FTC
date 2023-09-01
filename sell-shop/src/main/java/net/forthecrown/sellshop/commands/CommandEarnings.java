package net.forthecrown.sellshop.commands;

import static net.forthecrown.text.Text.format;

import com.google.common.collect.Streams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Locale;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.sellshop.UserShopData;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

@CommandData("file = commands/earnings.gcn")
public class CommandEarnings {

  void getEarnings(CommandSource source, @Argument("player") User user)
      throws CommandSyntaxException
  {
    var earnings = user.getComponent(UserShopData.class);

    if (earnings.isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    source.sendMessage(
        format("{0, user}'s earned data:\n{1}",
            user,
            TextJoiner.onNewLine()
                .add(
                    Streams.stream(earnings)
                        .map(entry -> format("&7{0}&8:&r {1, rhines}",
                            entry.getMaterial(),
                            entry.getValue()
                        ))
                )
        )
    );
  }

  void clearEarnings(CommandSource source, @Argument("players") Collection<User> users) {
    for (User user : users) {
      var earnings = user.getComponent(UserShopData.class);
      earnings.clear();
    }

    if (users.size() == 1) {
      var user = users.iterator().next();
      source.sendSuccess(format("Cleared {0, user}'s earnings", user));
    } else {
      source.sendSuccess(format("Cleared {0, number} users' earnings", users.size()));
    }
  }

  void addEarnings(
      CommandSource source,
      @Argument("player") User user,
      @Argument("amount") int amount,
      @Argument("material") Material material
  ) {
    var earnings = user.getComponent(UserShopData.class);
    int newAmount = earnings.get(material) + amount;

    earnings.set(material, newAmount);

    source.sendSuccess(format(
        "Added {0, rhines} to {1} earnings of {2, user}, now {3, rhines}",
        amount, material, user, newAmount
    ));
  }

  void removeEarnings(
      CommandSource source,
      @Argument("player") User user,
      @Argument("amount") int amount,
      @Argument("material") Material material
  ) throws CommandSyntaxException {
    var earnings = user.getComponent(UserShopData.class);
    int newAmount = earnings.get(material) - amount;

    earnings.set(material, newAmount);

    source.sendSuccess(format(
        "Removed {0, rhines} from {1} earnings of {2, user}, now {3, rhines}",
        amount, material, user, newAmount
    ));
  }

  void setEarnings(
      CommandSource source,
      @Argument("player") User user,
      @Argument("amount") int amount,
      @Argument("material") Material material
  ) throws CommandSyntaxException {
    var earnings = user.getComponent(UserShopData.class);
    earnings.set(material, amount);

    if (amount == 0) {
      source.sendSuccess(removedMessage(material, user));
    } else {
      source.sendSuccess(
          format("Set {0} earnings of {1, user} to {2, rhines}",
              material.name().toLowerCase(Locale.ROOT),
              user, amount
          )
      );
    }
  }

  Component removedMessage(Material material, User user) {
    return format("Removed {0} earnings from {1, user}",
        material.name().toLowerCase(), user
    );
  }
}
