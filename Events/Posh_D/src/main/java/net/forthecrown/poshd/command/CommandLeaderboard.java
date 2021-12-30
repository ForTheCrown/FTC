package net.forthecrown.poshd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.crown.ArmorStandLeaderboard;
import net.forthecrown.crown.ObjectiveLeaderboard;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.types.ByteArgument;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.poshd.EventUtil;
import net.forthecrown.poshd.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.scoreboard.Objective;

import javax.annotation.Nullable;

public class CommandLeaderboard extends AbstractCommand {

    public CommandLeaderboard() {
        super("leaderboard", Main.inst);

        setPermission("ftc.commands.leaderboard");
        register();
    }

    public static final DynamicCommandExceptionType NO_LEADERBOARD = new DynamicCommandExceptionType(o -> {
        return new LiteralMessage(o + " has no leaderboard assigned to it");
    });

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("obj", ObjectiveArgument.objective())
                                .then(argument("pos", PositionArgument.position())
                                        .then(argument("title", ChatArgument.chat())
                                                .executes(c -> {
                                                    Component title = c.getArgument("title", Component.class);
                                                    Location loc = PositionArgument.getLocation(c, "pos");
                                                    Objective obj = ObjectiveArgument.getObjective(c, "obj");

                                                    ObjectiveLeaderboard leaderboard = Main.leaderboards.create(loc, obj, title);
                                                    leaderboard.create();

                                                    c.getSource().sendAdmin("Created leaderboard for objective " + obj.getName());
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                .then(literal("update_all")
                        .executes(c -> {
                            if(Main.leaderboards.isEmpty()) {
                                c.getSource().sendMessage(Component.text("No leaderboards have been created").style(RoyalCommandException.ERROR_MESSAGE_STYLE));
                                return 0;
                            }

                            Main.leaderboards.forEach(ObjectiveLeaderboard::create);

                            c.getSource().sendAdmin("Updated all leaderboards");
                            return 0;
                        })
                )

                .then(literal("remove_all")
                        .executes(c -> {
                            if(Main.leaderboards.isEmpty()) {
                                c.getSource().sendMessage(Component.text("No leaderboards have been created").style(RoyalCommandException.ERROR_MESSAGE_STYLE));
                                return 0;
                            }

                            Main.leaderboards.clear();

                            c.getSource().sendAdmin("Removed all leaderboards");
                            return 0;
                        })
                )

                .then(literal("save")
                        .executes(c -> {
                            Main.leaderboards.save();

                            c.getSource().sendAdmin("Saved leaderboards");
                            return 0;
                        })
                )

                .then(literal("reload")
                        .executes(c -> {
                            Main.leaderboards.reload();

                            c.getSource().sendAdmin("Reloaded leaderboards");
                            return 0;
                        })
                )

                .then(literal("existing")
                        .then(argument("obj", ObjectiveArgument.objective())
                                .suggests(Main.leaderboards)

                                .then(literal("update")
                                        .executes(c -> {
                                            ObjectiveLeaderboard l = get(c);
                                            l.create();

                                            c.getSource().sendAdmin("Updated leaderboard " + l.getObjective().getName());
                                            return 0;
                                        })
                                )

                                .then(literal("remove")
                                        .executes(c -> {
                                            ObjectiveLeaderboard l = get(c);

                                            l.destroy();
                                            Main.leaderboards.remove(l);

                                            c.getSource().sendAdmin("Removed leaderboard");
                                            return 0;
                                        })
                                )

                                .then(accessor(LeaderboardAccessor.BORDER))
                                .then(accessor(LeaderboardAccessor.ORDER))
                                .then(accessor(LeaderboardAccessor.SIZE))
                                .then(accessor(LeaderboardAccessor.TITLE))
                                .then(accessor(LeaderboardAccessor.LOCATION))
                        )
                );
    }

    private ObjectiveLeaderboard get(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Objective obj = c.getArgument("obj", Objective.class);
        ObjectiveLeaderboard leaderboard = Main.leaderboards.get(obj);

        if(leaderboard == null) {
            throw NO_LEADERBOARD.create(obj.getName());
        }

        return leaderboard;
    }

    public static <T extends ArgumentBuilder<CommandSource, T>> T compOrStringArg(T arg, @Nullable SuggestionProvider<CommandSource> s, ComponentCommand runnable){
        addCompOrStringArg(arg, s, runnable);
        return arg;
    }
    public static void addCompOrStringArg(ArgumentBuilder<CommandSource, ?> arg, @Nullable SuggestionProvider<CommandSource> s, ComponentCommand command) {
        arg
                .then(arg("string", StringArgumentType.greedyString())
                        .suggests(s == null ? (c, b) -> Suggestions.empty() : s)

                        .executes(c -> command.run(c, EventUtil.stringToNonItalic(c.getArgument("string", String.class))))
                )

                .then(componentArg("cLore", c-> command.run(c, c.getArgument("cLore", Component.class))));
    }
    public static LiteralArgumentBuilder<CommandSource> componentArg(String argName, Command<CommandSource> cmd){
        return arg("-component")
                .then(arg(argName, ComponentArgument.component())
                        .executes(cmd)
                );
    }
    public interface ComponentCommand {
        int run(CommandContext<CommandSource> context, Component lore) throws CommandSyntaxException;
    }
    private static LiteralArgumentBuilder<CommandSource> arg(String name){
        return LiteralArgumentBuilder.literal(name);
    }
    private static <T> RequiredArgumentBuilder<CommandSource, T> arg(String name, ArgumentType<T> type){
        return RequiredArgumentBuilder.argument(name, type);
    }

    private <T> LiteralArgumentBuilder<CommandSource> accessor(LeaderboardAccessor<T> acc) {
        return literal(acc.getName())
                .executes(c -> {
                    ObjectiveLeaderboard leaderboard = get(c);

                    c.getSource().sendMessage(
                            Component.text(leaderboard.getObjective().getName() + "'s " + acc.getName() + ": ")
                                    .append(acc.display(leaderboard))
                    );
                    return 0;
                })

                .then(argument("value", acc.getArgumentType())
                        .executes(c -> {
                            ObjectiveLeaderboard leaderboard = get(c);
                            T parsed = c.getArgument("value", acc.getTypeClass());

                            leaderboard.destroy();
                            acc.set(leaderboard, c, parsed);
                            leaderboard.create();

                            c.getSource().sendAdmin(
                                    Component.text("Set " + leaderboard.getObjective().getName() + "'s " + acc.getName() + " to ")
                                            .append(acc.display(leaderboard))
                            );
                            return 0;
                        })
                );
    }

    public interface LeaderboardAccessor<T> {
        LeaderboardAccessor<Position> LOCATION = new LeaderboardAccessor<Position>() {
            @Override
            public ArgumentType<Position> getArgumentType() {
                return PositionArgument.position();
            }

            @Override
            public Class<Position> getTypeClass() {
                return Position.class;
            }

            @Override
            public String getName() {
                return "location";
            }

            @Override
            public void set(ObjectiveLeaderboard l, CommandContext<CommandSource> c, Position val) {
                l.setLocation(val.getLocation(c.getSource()));
            }

            @Override
            public Component display(ObjectiveLeaderboard l) {
                Location loc = l.getLocation();
                String
                        x = String.format("%.2f", loc.getX()),
                        y = String.format("%.2f", loc.getY()),
                        z = String.format("%.2f", loc.getZ());

                return Component.text("x=" + x + " y=" + y + " z=" + z)
                        .hoverEvent(Component.text("Click to teleport"))
                        .clickEvent(ClickEvent.runCommand("/tp " + x + " " + y + " " + z));
            }
        };

        LeaderboardAccessor<ArmorStandLeaderboard.Order> ORDER = new LeaderboardAccessor<ArmorStandLeaderboard.Order>() {
            @Override
            public ArgumentType<ArmorStandLeaderboard.Order> getArgumentType() {
                return EnumArgument.of(ArmorStandLeaderboard.Order.class);
            }

            @Override
            public Class<ArmorStandLeaderboard.Order> getTypeClass() {
                return ArmorStandLeaderboard.Order.class;
            }

            @Override
            public String getName() {
                return "order";
            }

            @Override
            public void set(ObjectiveLeaderboard l, CommandContext<CommandSource> c, ArmorStandLeaderboard.Order val) {
                l.setOrder(val);
            }

            @Override
            public Component display(ObjectiveLeaderboard l) {
                return Component.text(l.getOrder().name().toLowerCase());
            }
        };

        LeaderboardAccessor<Component> BORDER = new LeaderboardAccessor<Component>() {
            @Override
            public ArgumentType<Component> getArgumentType() {
                return ChatArgument.chat();
            }

            @Override
            public Class<Component> getTypeClass() {
                return Component.class;
            }

            @Override
            public String getName() {
                return "border";
            }

            @Override
            public void set(ObjectiveLeaderboard l, CommandContext<CommandSource> c, Component val) {
                l.setBorder(val);
            }

            @Override
            public Component display(ObjectiveLeaderboard l) {
                return l.getBorder();
            }
        };

        LeaderboardAccessor<Byte> SIZE = new LeaderboardAccessor<Byte>() {
            @Override
            public ArgumentType<Byte> getArgumentType() {
                return ByteArgument.byteArg((byte) 0);
            }

            @Override
            public Class<Byte> getTypeClass() {
                return Byte.class;
            }

            @Override
            public String getName() {
                return "size";
            }

            @Override
            public void set(ObjectiveLeaderboard l, CommandContext<CommandSource> c, Byte val) {
                l.setSize(val);
            }

            @Override
            public Component display(ObjectiveLeaderboard l) {
                return Component.text(l.getSize());
            }
        };

        LeaderboardAccessor<Component> TITLE = new LeaderboardAccessor<Component>() {
            @Override
            public ArgumentType<Component> getArgumentType() {
                return ChatArgument.chat();
            }

            @Override
            public Class<Component> getTypeClass() {
                return Component.class;
            }

            @Override
            public String getName() {
                return "title";
            }

            @Override
            public void set(ObjectiveLeaderboard l, CommandContext<CommandSource> c, Component val) {
                l.setTitle(val);
            }

            @Override
            public Component display(ObjectiveLeaderboard l) {
                return l.getTitle()[0];
            }
        };

        ArgumentType<T> getArgumentType();
        Class<T> getTypeClass();

        String getName();

        void set(ObjectiveLeaderboard l, CommandContext<CommandSource> c,  T val) throws CommandSyntaxException;
        Component display(ObjectiveLeaderboard l);
    }
}
