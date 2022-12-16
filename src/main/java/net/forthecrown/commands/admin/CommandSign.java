package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.util.Map;
import java.util.UUID;

public class CommandSign extends FtcCommand {
    public CommandSign(){
        super("sign");

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

                                    SignLines.EMPTY.apply(sign);
                                    sign.update();

                                    c.getSource().sendAdmin("Cleared sign");
                                    return 0;
                                })
                        )

                        .then(literal("copy")
                                .executes(c -> {
                                    User user = getUserSender(c);
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
                                    User user = getUserSender(c);
                                    Sign sign = get(c);

                                    SignLines lines = copies.get(user.getUniqueId());
                                    if (lines == null) {
                                        throw Exceptions.NO_SIGN_COPY;
                                    }

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

                                .then(literal("set")
                                        .then(argument("line", Arguments.CHAT)
                                                .suggests((c, b) -> {
                                                    Sign sign = get(c);
                                                    int line = c.getArgument("index", Integer.class);
                                                    var token = b.getRemainingLowerCase();

                                                    var lineComponent = sign.line(line - 1);

                                                    String lineText = LegacyComponentSerializer
                                                            .legacyAmpersand()
                                                            .serialize(lineComponent);

                                                    if (CompletionProvider.startsWith(token, lineText)) {
                                                        b.suggest(lineText, toTooltip(lineComponent));
                                                        return b.buildFuture();
                                                    }

                                                    return Arguments.CHAT.listSuggestions(c, b);
                                                })

                                                .executes(c -> set(c, c.getArgument("line", Component.class)))
                                        )
                                )

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

        if (!(l.getBlock().getState() instanceof Sign)) {
            throw Exceptions.notSign(l);
        }

        return (Sign) l.getBlock().getState();
    }

    /**
     * A small data class to store the data written on a
     * sign or data which can be applied to a sign
     */
    record SignLines(Component line0,
                     Component line1,
                     Component line2,
                     Component line3
    ) {
        public static final SignLines EMPTY = new SignLines(Component.empty(), Component.empty(), Component.empty(), Component.empty());

        public SignLines(Sign sign) {
            this(
                    sign.line(0),
                    sign.line(1),
                    sign.line(2),
                    sign.line(3)
            );
        }

        /**
         * Sets the given sign's lines this instance's lines
         * @param sign The sign to edit
         */
        public void apply(Sign sign) {
            sign.line(0, emptyIfNull(line0));
            sign.line(1, emptyIfNull(line1));
            sign.line(2, emptyIfNull(line2));
            sign.line(3, emptyIfNull(line3));
        }

        private static Component emptyIfNull(Component component) {
            return component == null ? Component.empty() : component;
        }
    }
}