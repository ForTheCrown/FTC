package net.forthecrown.commands.emotes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class EmotePog extends FtcCommand {

    public EmotePog(){
        super("pog");

        setAliases("pgo", "poggers", "pogchamp", "pogo");
        setPermission(Permissions.EMOTE_POG);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    var user = getUserSender(c);

                    pog(user, null);
                    return 0;
                })

                .then(argument("message", Arguments.MESSAGE)
                        .executes(c -> {
                            var user = getUserSender(c);
                            var msg = Arguments.getMessage(c, "message");

                            pog(user, msg);
                            return 0;
                        })
                );
    }

    void pog(User user, Component message) throws CommandSyntaxException {
        Cooldown.testAndThrow(user, getName(), 3 * 20);

        var formatted = Text.format("&e{0, user} &7Pogged", user);

        if (message != null) {
            if (BannedWords.checkAndWarn(user.getPlayer(), message)) {
                return;
            }

            formatted = formatted
                    .append(Component.text(": "))
                    .append(message);
        } else {
            formatted = formatted
                    .append(Component.text("!"));
        }

        Bukkit.getServer().sendMessage(formatted);
    }
}