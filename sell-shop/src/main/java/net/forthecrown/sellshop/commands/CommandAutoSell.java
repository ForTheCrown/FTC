package net.forthecrown.sellshop.commands;

import static net.forthecrown.text.Text.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.sellshop.UserShopData;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

@CommandData("file = commands/auto_sell.gcn")
public class CommandAutoSell {

  void listOther(CommandSource source, @Argument("player") User user)
      throws CommandSyntaxException
  {
    UserShopData data = user.getComponent(UserShopData.class);

    if (data.getAutoSelling().isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    source.sendMessage(
        format("{0, user}'s auto sell materials: {1}",
            user,
            TextJoiner.onComma()
                .add(data.getAutoSelling()
                    .stream()
                    .map(material -> Component.text(material.name().toLowerCase()))
                )
                .asComponent()
        )
    );
  }

  void add(
      CommandSource source,
      @Argument("player") User user,
      @Argument("material") Material material
  ) {
    var earnings = user.getComponent(UserShopData.class);
    earnings.getAutoSelling().add(material);
    source.sendSuccess(format("Added {0} to {1, user}'s auto sell list", material, user));
  }

  void remove(
      CommandSource source,
      @Argument("player") User user,
      @Argument("material") Material material
  ) {
    var earnings = user.getComponent(UserShopData.class);
    earnings.getAutoSelling().remove(material);
    source.sendSuccess(format("Removed {0} from {1, user}'s auto sell list", material, user));
  }

  void clear(CommandSource source, @Argument("player") User user) {
    UserShopData data = user.getComponent(UserShopData.class);
    data.getAutoSelling().clear();
    source.sendSuccess(format("Cleared {0, user}'s auto sell materials", user));
  }
}
