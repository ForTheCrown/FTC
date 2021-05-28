package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.CrownBoundingBox;
import net.forthecrown.emperor.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.List;

public class CommandNear extends CrownCommandBuilder {

    public CommandNear(){
        super("near", CrownCore.inst());

        setAliases("nearby");
        setPermission(Permissions.DONATOR_2);
        setDescription("Shows nearby players");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    return showNearby(user.getLocation(), CrownCore.getNearRadius(), c.getSource());
                })

                .then(argument("radius", IntegerArgumentType.integer(1, 100000))
                        .requires(s -> s.hasPermission(Permissions.HELPER))

                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            return showNearby(user.getLocation(), c.getArgument("radius", Integer.class), c.getSource());
                        })
                )

                .then(argument("user", UserType.onlineUser())
                        .requires(s -> s.hasPermission(Permissions.HELPER))

                        .executes(c -> {
                            CrownUser user = UserType.getUser(c, "user");

                            return showNearby(user.getLocation(), CrownCore.getNearRadius(), c.getSource());
                        })

                        .then(argument("radius", IntegerArgumentType.integer(1, 100000))
                                .requires(s -> s.hasPermission(Permissions.HELPER))

                                .executes(c -> {
                                    CrownUser user = UserType.getUser(c, "user");
                                    int radius = c.getArgument("radius", Integer.class);

                                    return showNearby(user.getLocation(), radius, c.getSource());
                                })
                        )
                );
    }

    private int showNearby(Location loc, int radius, CommandSource source) throws CommandSyntaxException {
        CrownBoundingBox box = CrownBoundingBox.of(loc, radius);
        List<CrownUser> players = ListUtils.convertToList(box.getPlayers(), UserManager::getUser);

        if(source.isPlayer()){
            players.remove(UserManager.getUser(source.asPlayer()));

            if(players.isEmpty()) throw FtcExceptionProvider.create("No nearby players");
        }

        TextComponent.Builder builder = Component.text()
                .append(Component.text("Nearby players: ").color(NamedTextColor.GOLD));


        for (CrownUser u: players){
            builder
                    .append(u.nickDisplayName().color(u.getHighestTierRank().tier.color))
                    .append(Component.text(" (" + dist(u.getLocation(), box.getCenterLocation()) + ")").color(NamedTextColor.GRAY))
                    .append(Component.text(" "));
        }

        source.sendMessage(builder.build());
        return 0;
    }

    private String dist(Location from, Location to){
        return Math.floor(from.distance(to)) + "m";
    }
}
