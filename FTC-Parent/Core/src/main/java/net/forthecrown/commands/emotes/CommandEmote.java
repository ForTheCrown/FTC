package net.forthecrown.commands.emotes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

/**
 * The class to make the handling of emotes easier
 * <p>Here the return value actually matters. If the returned value is below 0, they don't get added to the cooldown</p>
 */
public abstract class CommandEmote extends FtcCommand {

    protected final int cooldownTime;
    protected final String cooldownMessage;
    protected final String cooldownCategory;

    protected CommandEmote(@NotNull String name, @Nonnegative int cooldownTime, @NotNull String cooldownMessage) {
        super(name, CrownCore.inst());

        setPermission("ftc.emotes." + name);

        this.cooldownCategory = "Core_Emote_" + name;
        this.cooldownMessage = cooldownMessage;
        this.cooldownTime = cooldownTime;
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> executeSelf(getUserSender(c)))

                .then(argument("player", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser sender = getUserSender(c);

                            if(Cooldown.contains(sender, cooldownCategory)){
                                sender.sendMessage(ChatFormatter.formatString(cooldownMessage, null, true));
                                return 0;
                            }

                            CrownUser recipient = UserType.getUser(c, "player");
                            if(recipient.equals(sender)) return executeSelf(sender);

                            if(!sender.allowsEmotes()) throw FtcExceptionProvider.senderEmoteDisabled();
                            if(!recipient.allowsEmotes()) throw FtcExceptionProvider.targetEmoteDisabled(recipient);

                            if(execute(sender, recipient) >= 0 && !sender.hasPermission(Permissions.CORE_ADMIN)) Cooldown.add(sender, cooldownCategory, cooldownTime);
                            return 0;
                        })
                );
    }

    protected abstract int execute(CrownUser sender, CrownUser recipient) throws CommandSyntaxException;
    protected abstract int executeSelf(CrownUser user) throws CommandSyntaxException;
}
