package net.forthecrown.emperor.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserHomes;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.Map;

public class CommandHomeList extends FtcCommand {
    public CommandHomeList(){
        super("homelist", CrownCore.inst());

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

                .then(argument("user", UserType.user())
                        .requires(s -> s.hasPermission(Permissions.HOME_OTHERS))

                        .executes(c -> {
                            CrownUser user = UserType.getUser(c, "user");
                            boolean self = user.getName().equalsIgnoreCase(c.getSource().textName());

                            return listHomes(user.getHomes(), c.getSource(), self);
                        })
                );
    }

    private int listHomes(UserHomes homes, CommandSource source, boolean self) throws CommandSyntaxException {
        if(homes.isEmpty()) throw FtcExceptionProvider.noHomesToList();

        TextComponent.Builder builder = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(homes.getOwner().coloredNickDisplayName())
                .append(Component.text("'s homes: "));

        String prefix = self ? "" : homes.getOwner().getName() + ":";
        for (Map.Entry<String, Location> e: homes.getHomes().entrySet()){
            builder
                    .append(Component.text("[" + e.getKey() + "] ")
                            .color(NamedTextColor.GOLD)
                            .hoverEvent(ChatFormatter.prettyLocationMessage(e.getValue(), false))
                            .clickEvent(ClickEvent.runCommand("/home " + prefix + e.getKey()))
                    );
        }

        source.sendMessage(builder.build());
        return 0;
    }
}
