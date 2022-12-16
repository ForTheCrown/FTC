package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;

public class CommandSpeed extends FtcCommand {

    public static float DEF_WALK = 0.2f;
    public static float DEF_FLY = 0.1f;

    public CommandSpeed(){
        super("speed");

        setPermission(Permissions.ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(arg(true))
                .then(arg(false));
    }

    private LiteralArgumentBuilder<CommandSource> arg(boolean fly){
        return literal(fly ? "fly" : "walk")
                .then(argument("value", FloatArgumentType.floatArg(0f, 5f))
                        .suggests(suggestMatching("1", "1.5", "2", "0.5", "5"))

                        .executes(c -> changeSpeed(
                                getUserSender(c),
                                c.getArgument("value", Float.class),
                                c.getSource(),
                                fly
                        ))

                        .then(argument("user", Arguments.ONLINE_USER)
                                .executes(c -> changeSpeed(
                                        Arguments.getUser(c, "user"),
                                        c.getArgument("value", Float.class),
                                        c.getSource(),
                                        fly
                                ))
                        )
                )

                .then(literal("query")
                        .executes(c -> querySpeed(getUserSender(c), c.getSource(), fly))

                        .then(argument("user", Arguments.ONLINE_USER)
                                .executes(c -> querySpeed(
                                        Arguments.getUser(c, "user"),
                                        c.getSource(),
                                        fly
                                ))
                        )
                );
    }

    private int changeSpeed(User user, float amount, CommandSource source, boolean fly) {
        var attribute = fly ? user.getPlayer().getAttribute(Attribute.GENERIC_FLYING_SPEED)
                : user.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        var modifier = fly ? DEF_FLY : DEF_WALK;

        attribute.setBaseValue(modifier * amount);

        source.sendAdmin(
                Component.text("Set " + (fly ? "fly" : "walk") + "ing speed of ")
                        .append(user.displayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" to "))
                        .append(Component.text(amount).color(NamedTextColor.YELLOW))
        );
        return 0;
    }

    private int querySpeed(User user, CommandSource source, boolean fly){
        float realValue = fly ? user.getPlayer().getFlySpeed() : user.getPlayer().getWalkSpeed();
        double value = Math.floor(realValue / (fly ? DEF_FLY : DEF_WALK));

        source.sendMessage(
                Component.text((fly ? "Fly" : "Walk") + "ing speed of ")
                        .append(user.displayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" is "))
                        .append(Component.text(value).color(NamedTextColor.YELLOW))
                        .append(Component.text(", actual is "))
                        .append(Component.text(realValue).color(NamedTextColor.YELLOW))
        );
        return 0;
    }
}