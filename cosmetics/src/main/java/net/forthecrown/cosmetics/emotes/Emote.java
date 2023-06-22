package net.forthecrown.cosmetics.emotes;

import javax.annotation.Nonnegative;
import lombok.Getter;
import net.forthecrown.Cooldowns;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class Emote extends FtcCommand {

  @Getter
  protected final int cooldownTime;
  protected final Component cooldownMessage;
  protected final String cooldownCategory;

  protected Emote(
      @NotNull String name,
      @Nonnegative int cooldownTime,
      @NotNull Component cooldownMessage
  ) {
    super(name);

    setPermission(EmotePermissions.EMOTES);

    this.cooldownCategory = "command_emote_" + name;
    this.cooldownMessage = cooldownMessage;
    this.cooldownTime = cooldownTime;
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Emotes on yourself");
    factory.usage("<player>", "Emotes on a player");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> executeSelf(getUserSender(c)))

        .then(argument("player", Arguments.ONLINE_USER)
            .executes(c -> {
              User sender = getUserSender(c);
              var cooldown = Cooldowns.cooldowns();

              if (cooldown.onCooldown(sender.getUniqueId(), cooldownCategory)) {
                sender.sendMessage(cooldownMessage);
                return 0;
              }

              User target = Arguments.getUser(c, "player");

              if (target.equals(sender)) {
                //Make sure to execute on self, not on others
                return executeSelf(sender);
              }

              //If either doesn't allow emotes, stop
              if (!sender.get(Emotes.EMOTES_ENABLED)) {
                throw Exceptions.create(
                    "You have emotes disabled\nUse /emotetoggle to enable them."
                );
              }

              if (!target.get(Emotes.EMOTES_ENABLED)) {
                throw Exceptions.format("{0, user} has disabled emotes", target);
              }

              //If return value is more than or equal to 0, add to cooldown
              if (execute(sender, target) >= 0
                  && !sender.hasPermission(EmotePermissions.EMOTE_IGNORE)
                  && getCooldownTime() > 0
              ) {
                cooldown.cooldown(sender.getUniqueId(), cooldownCategory, getCooldownTime());
              }

              return 0;
            })
        );
  }

  public abstract int execute(User sender, User target);

  public abstract int executeSelf(User user);
}