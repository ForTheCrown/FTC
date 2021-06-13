package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.commands.arguments.UserType;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.List;

public class CommandNear extends FtcCommand {

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

            if(players.isEmpty()) throw FtcExceptionProvider.noNearbyPlayers();
        }

        TextComponent.Builder builder = Component.text()
                .append(Component.translatable("commands.near")
                        .color(NamedTextColor.GOLD)
                        .append(Component.text(": "))
                );


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
