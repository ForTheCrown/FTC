package net.forthecrown.core.commands.emotes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.Cooldown;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.EmoteDisabledException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.jetbrains.annotations.NotNull;

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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
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
