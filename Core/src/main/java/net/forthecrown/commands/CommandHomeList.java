package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserHomes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.Map;

public class CommandHomeList extends FtcCommand {
    public CommandHomeList(){
        super("homelist");

        setAliases("homes");
        setPermission(Permissions.HOME);
        setDescription("Lists all your homes");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    return listHomes(user.getHomes(), c.getSource(), true);
                })

                .then(argument("user", UserArgument.user())
                        .requires(s -> s.hasPermission(Permissions.HOME_OTHERS))

                        .executes(c -> {
                            CrownUser user = UserArgument.getUser(c, "user");
                            boolean self = user.getName().equalsIgnoreCase(c.getSource().textName());

                            return listHomes(user.getHomes(), c.getSource(), self);
                        })
                );
    }

    private int listHomes(UserHomes homes, CommandSource source, boolean self) throws CommandSyntaxException {
        if(homes.isEmpty()) throw FtcExceptionProvider.noHomesToList();

        TextComponent.Builder builder = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(self ? Component.text("Your") : homes.getUser().coloredNickDisplayName())
                .append(Component.text((self ? "" : "'s") + " homes: "));

        String prefix = self ? "" : homes.getUser().getName() + ":";
        for (Map.Entry<String, Location> e: homes.getHomes().entrySet()){
            builder
                    .append(Component.text("[" + e.getKey() + "] ")
                            .color(NamedTextColor.GOLD)
                            .hoverEvent(FtcFormatter.prettyLocationMessage(e.getValue(), false))
                            .clickEvent(ClickEvent.runCommand("/home " + prefix + e.getKey()))
                    );
        }

        source.sendMessage(builder.build());
        return 0;
    }
}
