package net.forthecrown.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSpeed extends FtcCommand {
    public CommandSpeed(){
        super("speed", CrownCore.inst());

        setPermission(Permissions.CORE_ADMIN);
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

                        .then(argument("user", UserType.onlineUser())
                                .executes(c -> changeSpeed(
                                        UserType.getUser(c, "user"),
                                        c.getArgument("value", Float.class),
                                        c.getSource(),
                                        fly
                                ))
                        )
                )

                .then(literal("query")
                        .executes(c -> querySpeed(getUserSender(c), c.getSource(), fly))

                        .then(argument("user", UserType.onlineUser())
                                .executes(c -> querySpeed(
                                        UserType.getUser(c, "user"),
                                        c.getSource(),
                                        fly
                                ))
                        )
                );
    }

    private int changeSpeed(CrownUser user, float amount, CommandSource source, boolean fly){
        if(fly) user.getPlayer().setFlySpeed(amount * 0.1f);
        else user.getPlayer().setWalkSpeed(amount * 0.2f);

        source.sendAdmin(
                Component.text("Set " + (fly ? "fly" : "walk") + "ing speed of ")
                        .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" to "))
                        .append(Component.text(amount).color(NamedTextColor.YELLOW))
        );
        return 0;
    }

    private int querySpeed(CrownUser user, CommandSource source, boolean fly){
        float realValue = fly ? user.getPlayer().getFlySpeed() : user.getPlayer().getWalkSpeed();
        double value = Math.floor(realValue / (fly ? 0.1f : 0.2f));
        source.sendMessage(
                Component.text((fly ? "Fly" : "Walk") + "ing speed of ")
                        .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" is "))
                        .append(Component.text(realValue).color(NamedTextColor.YELLOW))
                        .append(Component.text(", actual is "))
                        .append(Component.text(value).color(NamedTextColor.YELLOW))
        );
        return 0;
    }
}
