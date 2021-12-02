package net.forthecrown.commands.emotes;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

/**
 * The class to make the handling of emotes easier
 * <p>Here the return value actually matters. If the returned value is below 0, they don't get added to the cooldown</p>
 */
public abstract class CommandEmote extends FtcCommand {

    protected final int cooldownTime;
    protected final Component cooldownMessage;
    protected final String cooldownCategory;

    protected CommandEmote(@NotNull String name, @Nonnegative int cooldownTime, @NotNull Component cooldownMessage) {
        super(name, Crown.inst());

        setPermission(Permissions.EMOTES);

        this.cooldownCategory = "Core_Emote_" + name;
        this.cooldownMessage = cooldownMessage;
        this.cooldownTime = cooldownTime;
    }

    protected CommandEmote(@NotNull String name, @Nonnegative int cooldownTime) {
        this(name, cooldownTime, Component.translatable("emotes." + name + ".cooldown"));
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> executeSelf(getUserSender(c)))

                .then(argument("player", UserArgument.onlineUser())
                        .executes(c -> {
                            CrownUser sender = getUserSender(c);

                            if(Cooldown.contains(sender, cooldownCategory)){
                                sender.sendMessage(cooldownMessage);
                                return 0;
                            }

                            CrownUser recipient = UserArgument.getUser(c, "player");
                            if(recipient.equals(sender)) return executeSelf(sender); //Make sure to execute on self, not on others

                            //If either doesn't allow emotes, stop
                            if(!sender.allowsEmotes()) throw FtcExceptionProvider.senderEmoteDisabled();
                            if(!recipient.allowsEmotes()) throw FtcExceptionProvider.targetEmoteDisabled(recipient);

                            //If return value is more than or equal to 0, add to cooldown
                            if(execute(sender, recipient) >= 0 && !sender.hasPermission(Permissions.EMOTE_IGNORE)){
                                Cooldown.add(sender, cooldownCategory, cooldownTime);
                            }

                            //Return 0 anyway, cuz fuck u
                            return 0;
                        })
                );
    }

    public abstract int execute(CrownUser sender, CrownUser target);
    public abstract int executeSelf(CrownUser user);
}
