package net.forthecrown.titles.commands;

import static net.forthecrown.text.Messages.confirmButton;
import static net.forthecrown.text.Text.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.titles.TitlesPlugin;
import net.forthecrown.titles.UserRanks;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;

public class CommandBecomeBaron extends FtcCommand {

  private final TitlesPlugin plugin;

  public CommandBecomeBaron(TitlesPlugin plugin) {
    super("becomebaron");
    this.plugin = plugin;

    setDescription("Allows you to become a baron");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /becomebaron
        .executes(c -> {
          User p = getUserSender(c);

          // Validate user can purchase baron
          validate(p);

          //send message
          p.sendMessage(becomeBaronConfirm("/" + getName() + " confirm"));
          return 0;
        })

        // /becomebaron confirm
        .then(literal("confirm")
            .executes(c -> {
              User user = getUserSender(c);

              // Validate user can purchase baron
              validate(user);

              UserTitles titles = user.getComponent(UserTitles.class);

              user.removeBalance(plugin.getTitlesConfig().getBaronPrice());
              titles.addTitle(UserRanks.BARON);

              user.sendMessage(becomeBaron());
              user.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
              return 0;
            })
        );
  }

  private void validate(User user) throws CommandSyntaxException {
    var baronPrice = plugin.getTitlesConfig().getBaronPrice();
    UserTitles titles = user.getComponent(UserTitles.class);

    if (titles.hasTitle(UserRanks.BARON)) {
      throw Exceptions.create("You are already baron");
    }

    if (!user.hasBalance(baronPrice)) {
      throw Exceptions.cannotAfford(baronPrice);
    }
  }


  Component becomeBaronConfirm(String cmd) {
    return format("Are you sure you wish to become a &e{0}&r? It will cost &6{1, rhines}&r.\n{2}",
        NamedTextColor.GRAY,
        UserRanks.BARON.getTruncatedPrefix(),
        plugin.getTitlesConfig().getBaronPrice(),
        confirmButton(cmd)
    );
  }

  static Component becomeBaron() {
    return format("Congratulations! &7You are now a {0}!",
        NamedTextColor.GOLD,
        UserRanks.BARON.getTruncatedPrefix()
    );
  }
}