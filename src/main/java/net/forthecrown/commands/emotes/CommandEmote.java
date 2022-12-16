package net.forthecrown.commands.emotes;

import lombok.Getter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

/**
 * The class to make the handling of emotes easier
 * <p>Here the return value actually matters. If the returned value is below 0, they don't get added to the cooldown</p>
 */
public abstract class CommandEmote extends FtcCommand {

    @Getter
    protected final int cooldownTime;
    protected final Component cooldownMessage;
    protected final String cooldownCategory;

    protected CommandEmote(@NotNull String name, @Nonnegative int cooldownTime, @NotNull Component cooldownMessage) {
        super(name);

        setPermission(Permissions.EMOTES);

        this.cooldownCategory = "command_emote_" + name;
        this.cooldownMessage = cooldownMessage;
        this.cooldownTime = cooldownTime;
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> executeSelf(getUserSender(c)))

                .then(argument("player", Arguments.ONLINE_USER)
                        .executes(c -> {
                            User sender = getUserSender(c);

                            if (Cooldown.contains(sender, cooldownCategory)) {
                                sender.sendMessage(cooldownMessage);
                                return 0;
                            }

                            User target = Arguments.getUser(c, "player");

                            if (target.equals(sender)) {
                                //Make sure to execute on self, not on others
                                return executeSelf(sender);
                            }

                            //If either doesn't allow emotes, stop
                            if (!sender.get(Properties.EMOTES)) {
                                throw Exceptions.EMOTE_DISABLE_SELF;
                            }

                            if (!target.get(Properties.EMOTES)) {
                                throw Exceptions.emoteDisabledTarget(target);
                            }

                            //If return value is more than or equal to 0, add to cooldown
                            if (execute(sender, target) >= 0
                                    && !sender.hasPermission(Permissions.EMOTE_IGNORE)
                                    && getCooldownTime() > 0
                            ) {
                                Cooldown.add(sender, cooldownCategory, getCooldownTime());
                            }

                            return 0;
                        })
                );
    }

    public abstract int execute(User sender, User target);
    public abstract int executeSelf(User user);
}