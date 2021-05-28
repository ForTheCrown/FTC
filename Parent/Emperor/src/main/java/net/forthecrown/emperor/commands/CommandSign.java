package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class CommandSign extends CrownCommandBuilder {
    public CommandSign(){
        super("sign", CrownCore.inst());

        setAliases("editsign");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("pos", PositionArgument.position())
                        .then(argument("index", IntegerArgumentType.integer(1, 4))
                                .suggests(suggestMatching("1", "2", "3", "4"))

                                .then(CommandLore.compOrStringArg(argument("set"), this::set))

                                .then(argument("clear")
                                        .executes(c -> set(c, Component.empty()))
                                )
                        )
                );
    }

    private int set(CommandContext<CommandSource> c, Component text) throws CommandSyntaxException {
        int index = c.getArgument("index", Integer.class);
        Sign sign = get(c);

        sign.line(index-1, text);
        sign.update();

        c.getSource().sendAdmin(
                Component.text("Set line " + index + " to: ")
                        .append(text)
        );
        return 0;
    }

    private Sign get(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Location l = c.getArgument("pos", Position.class).getLocation(c.getSource());
        if(!(l.getBlock().getState() instanceof Sign)) throw FtcExceptionProvider.create("Given coords do not point to sign");

        return (Sign) l.getBlock().getState();
    }
}
