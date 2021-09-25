package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.SignLines;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.util.Map;
import java.util.UUID;

public class CommandSign extends FtcCommand {
    public CommandSign(){
        super("sign", Crown.inst());

        setAliases("editsign");
        register();
    }

    private final Map<UUID, SignLines> copies = new Object2ObjectOpenHashMap<>();

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("pos", PositionArgument.blockPos())
                        .then(literal("clear")
                                .executes(c -> {
                                    Sign sign = get(c);

                                    Component empty = Component.empty();

                                    sign.line(0, empty);
                                    sign.line(1, empty);
                                    sign.line(2, empty);
                                    sign.line(3, empty);
                                    sign.update();

                                    c.getSource().sendAdmin("Cleared sign");
                                    return 0;
                                })
                        )

                        .then(literal("copy")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Sign sign = get(c);

                                    SignLines lines = new SignLines(sign);
                                    copies.put(user.getUniqueId(), lines);

                                    user.sendMessage(
                                            Component.text("Copied sign")
                                    );

                                    return 0;
                                })
                        )

                        .then(literal("paste")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Sign sign = get(c);

                                    SignLines lines = copies.get(user.getUniqueId());
                                    if(lines == null) throw FtcExceptionProvider.create("No sign copied");

                                    lines.apply(sign);
                                    sign.update();

                                    user.sendMessage(
                                            Component.text("Pasted sign")
                                    );

                                    return 0;
                                })
                        )

                        .then(argument("index", IntegerArgumentType.integer(1, 4))
                                .suggests(suggestMatching("1", "2", "3", "4"))

                                .then(CommandLore.compOrStringArg(
                                        literal("set"),

                                        (c, b) -> {
                                            try {
                                                Sign sign = get(c);
                                                int line = c.getArgument("index", Integer.class);
                                                if(line < 0 || line > 4) return Suggestions.empty();

                                                String lineText = LegacyComponentSerializer.legacyAmpersand().serialize(sign.line(line-1));

                                                return CompletionProvider.suggestMatching(b, lineText);
                                            } catch (CommandSyntaxException ignored) {}
                                            return Suggestions.empty();
                                        },

                                        this::set
                                ))

                                .then(literal("clear")
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
