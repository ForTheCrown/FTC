package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.EmoteDisabledException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.CrownUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The class to make the handling of emotes easier
 * <p>Here the return value actually matters. If the returned value is below 0, they don't get added to the cooldown</p>
 */
public abstract class CommandEmote extends CrownCommandBuilder {

    protected final int cooldownTime;
    protected final String cooldownMessage;
    protected final String cooldownCategory;

    protected CommandEmote(@NotNull String name, int cooldownTime, String cooldownMessage) {
        super(name, FtcCore.getInstance());

        setPermission("ftc.emotes." + name);

        this.cooldownCategory = "Core_Emote_" + name;
        this.cooldownMessage = cooldownMessage;
        this.cooldownTime = cooldownTime;
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> executeSelf(getUserSender(c)))

                .then(argument("player", UserType.onlinePlayer())
                        .suggests((c, b) -> UserType.listSuggestions(b))

                        .executes(c -> {
                            CrownUser sender = getUserSender(c);

                            if(Cooldown.contains(sender, cooldownCategory)){
                                sender.sendMessage(CrownUtils.translateHexCodes(CrownUtils.formatEmojis(cooldownMessage)));
                                return 0;
                            }

                            CrownUser recipient = UserType.getOnlineUser(c, "player");
                            if(recipient.getName().equalsIgnoreCase(sender.getName())) return executeSelf(sender);

                            //If anyone's got emotes disabled, stop em
                            if(!sender.allowsEmotes()) throw EmoteDisabledException.senderDisabled();
                            if(!recipient.allowsEmotes()) throw EmoteDisabledException.targetDisabled();

                            if(execute(sender, recipient) >= 0 && !sender.hasPermission("ftc.admin")) Cooldown.add(sender, cooldownCategory, cooldownTime);
                            return 0;
                        })
                );
    }

    protected abstract int execute(CrownUser sender, CrownUser recipient);
    protected abstract int executeSelf(CrownUser user);
}
