package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.SuggestionFunction;
import net.forthecrown.commands.arguments.chat.MessageSuggestions;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.holidays.*;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandHolidays extends FtcCommand {
    public static final EnumArgument<Month> MONTH_ARG = EnumArgument.of(Month.class);
    public static final IntegerArgumentType DATE_ARG = IntegerArgumentType.integer(1, 31);

    static final SuggestionFunction TOKEN_SUGGESTIONS = (builder, source) -> HolidayTags.addSuggestions(builder);

    public CommandHolidays() {
        super("Holidays");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Holidays
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        if (Crown.inDebugMode()) {
            command
                    .then(literal("debug")
                            .then(literal("run_day_check")
                                    .executes(c -> {
                                        ServerHolidays.get().onDayChange(ZonedDateTime.now());

                                        c.getSource().sendAdmin("Ran day update check");
                                        return 0;
                                    })
                            )
                    );
        }


        command
                .executes(c -> {
                    TextWriter writer = TextWriters.newWriter();
                    writer.write(Component.text("Current holidays: "));

                    for (var v: ServerHolidays.get().getAll()) {
                        writer.newLine();

                        TextWriter hoverWriter = TextWriters.newWriter();
                        display(v, hoverWriter);

                        writer.write(
                                Component.text(v.getFilteredName() + " [" + v.getPeriod() + "]")
                                        .hoverEvent(hoverWriter.asComponent())
                                        .clickEvent(ClickEvent.suggestCommand("/" + getName() + " edit " + v.getName() + " "))
                                        .color(v.isActive() ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                        );
                    }

                    c.getSource().sendMessage(writer.asComponent());
                    return 0;
                })
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("month", MONTH_ARG)
                                        .then(argument("day", IntegerArgumentType.integer(1, 31))
                                                .executes(c -> {
                                                    String name = c.getArgument("name", String.class);

                                                    ServerHolidays holidays = ServerHolidays.get();

                                                    if(holidays.getHoliday(name) != null) {
                                                        throw Exceptions.alreadyExists("holiday", name);
                                                    }

                                                    Month month = c.getArgument("month", Month.class);
                                                    int day = c.getArgument("day", Integer.class);

                                                    Holiday created = new Holiday(name);
                                                    created.setPeriod(MonthDayPeriod.exact(month, day));

                                                    holidays.addHoliday(created);

                                                    c.getSource().sendAdmin("Created holiday named " + name + ", which will run on " + created.getPeriod() + " of every year");
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                .then(literal("edit")
                        .then(argument("holiday", Arguments.HOLIDAY)
                                .executes(c -> {
                                    Holiday holiday = get(c);

                                    TextWriter writer = TextWriters.newWriter();
                                    display(holiday, writer);

                                    c.getSource().sendMessage(writer.asComponent());
                                    return 0;
                                })

                                .then(literal("give")
                                        .then(literal("-all")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);

                                                    if (holiday.hasNoRewards()) {
                                                        throw Exceptions.HOLIDAY_NO_REWARDS;
                                                    }

                                                    ServerHolidays.get().runHoliday(holiday);

                                                    c.getSource().sendAdmin("Giving all players " + holiday.getName() + " rewards");
                                                    return 0;
                                                })
                                        )

                                        .then(argument("user", Arguments.ONLINE_USER)
                                                .executes(c -> {
                                                    User user = Arguments.getUser(c, "user");
                                                    Holiday holiday = get(c);

                                                    if (holiday.hasNoRewards()) {
                                                        throw Exceptions.HOLIDAY_NO_REWARDS;
                                                    }

                                                    ServerHolidays.get().giveRewards(user, holiday);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Gave ")
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" " + holiday.getName() + " rewards"))
                                                    );

                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("give_item")
                                        .then(argument("user", Arguments.ONLINE_USER)
                                                .executes(c -> {
                                                    User user = Arguments.getUser(c, "user");
                                                    Holiday holiday = get(c);

                                                    ItemStack item = ServerHolidays.get().getRewardItem(holiday, user, ZonedDateTime.now());

                                                    if (ItemStacks.isEmpty(item)) {
                                                        throw Exceptions.holidayNoItem(holiday);
                                                    }

                                                    PlayerInventory inv = user.getInventory();

                                                    if (inv.firstEmpty() == -1) {
                                                        user.getWorld().dropItem(user.getLocation(), item);
                                                    } else {
                                                        inv.addItem(item);
                                                    }

                                                    c.getSource().sendAdmin("Gave " + user.getName() + " " + holiday.getName() + " item rewards");
                                                    return  0;
                                                })
                                        )
                                )

                                .then(literal("inventory")
                                        .executes(c -> {
                                            Holiday holiday = get(c);
                                            Player player = c.getSource().asPlayer();

                                            player.openInventory(holiday.getInventory());
                                            return 0;
                                        })

                                        .then(literal("type")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);
                                                    RewardContainer container = holiday.getContainer();

                                                    c.getSource().sendMessage(holiday.getName() + " inventory type: " + (container.isChest() ? "Chest" : "Shulker"));
                                                    return 0;
                                                })

                                                .then(literal("chest")
                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            RewardContainer container = holiday.getContainer();

                                                            container.setChest(true);

                                                            c.getSource().sendAdmin("Set " + holiday.getName() + " to give items in a chest");
                                                            return 0;
                                                        })
                                                )

                                                .then(literal("shulker")
                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            RewardContainer container = holiday.getContainer();

                                                            container.setChest(false);

                                                            c.getSource().sendAdmin("Set " + holiday.getName() + " to give items in a shulker");
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("name")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);
                                                    RewardContainer container = holiday.getContainer();

                                                    c.getSource().sendMessage(holiday.getName() + " inventory name: " + container.getName());
                                                    return 0;
                                                })

                                                .then(argument("str", Arguments.CHAT)
                                                        .suggests((context, builder) -> {
                                                            return MessageSuggestions.get(
                                                                    context, builder,
                                                                    true,
                                                                    TOKEN_SUGGESTIONS
                                                            );
                                                        })

                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            RewardContainer container = holiday.getContainer();

                                                            Component name = c.getArgument("str", Component.class);
                                                            container.setName(name);

                                                            c.getSource().sendAdmin(holiday.getName() + "'s inventory name is now: " + name);
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("lore")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);
                                                    RewardContainer container = holiday.getContainer();

                                                    if (container.getLore().isEmpty()) {
                                                        throw Exceptions.NOTHING_TO_LIST;
                                                    }

                                                    TextWriter writer = TextWriters.newWriter();
                                                    writer.write(holiday.getName() + "'s lore:");
                                                    writeLore(container, writer);

                                                    writer.newLine();
                                                    writer.write(Component.text("                ").decorate(TextDecoration.STRIKETHROUGH));

                                                    c.getSource().sendMessage(writer.asComponent());
                                                    return 0;
                                                })

                                                .then(literal("remove")
                                                        .then(argument("index", IntegerArgumentType.integer(1))
                                                                .suggests((context, builder) -> {
                                                                    Holiday holiday = get(context);
                                                                    List<Component> lores = holiday.getContainer().getLore();
                                                                    int size = lores.size();

                                                                    if (size < 1) return Suggestions.empty();

                                                                    for (int i = 1; i <= size ; i++) {
                                                                        builder.suggest(i, toTooltip(lores.get(i - 1)));
                                                                    }

                                                                    return builder.buildFuture();
                                                                })

                                                                .executes(c -> {
                                                                    Holiday holiday = get(c);
                                                                    RewardContainer container = holiday.getContainer();

                                                                    int index = c.getArgument("index", Integer.class);

                                                                    if (index > container.getLore().size()) {
                                                                        throw Exceptions.invalidIndex(index, container.getLore().size());
                                                                    }

                                                                    var removed = container.getLore().remove(index - 1);

                                                                    c.getSource().sendAdmin(
                                                                            Text.format("Removed lore line {0, number}: '{1}' from {2}",
                                                                                    index, removed,
                                                                                    holiday.getName()
                                                                            )
                                                                    );
                                                                    return 0;
                                                                })
                                                        )
                                                )

                                                .then(literal("clear")
                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            RewardContainer container = holiday.getContainer();

                                                            container.getLore().clear();

                                                            c.getSource().sendAdmin("Cleared " + holiday.getName() + " container lore");
                                                            return 0;
                                                        })
                                                )

                                                .then(literal("add")
                                                        .then(argument("str", Arguments.CHAT)
                                                                .suggests((context, builder) -> {
                                                                    return MessageSuggestions.get(
                                                                            context, builder,
                                                                            true,
                                                                            TOKEN_SUGGESTIONS
                                                                    );
                                                                })

                                                                .executes(c -> {
                                                                    Holiday holiday = get(c);
                                                                    RewardContainer container = holiday.getContainer();

                                                                    Component lore = c.getArgument("str", Component.class);
                                                                    container.getLore().add(lore);

                                                                    c.getSource().sendAdmin("Added '" + lore + "' to " + holiday.getName() + "'s lore");
                                                                    return 0;
                                                                })
                                                        )
                                                )
                                        )
                                )

                                .then(currencies("Rhine", Holiday::getRhines, Holiday::setRhines))
                                .then(currencies("Gem", Holiday::getGems, Holiday::setGems))

                                .then(literal("delete")
                                        .executes(c -> {
                                            Holiday holiday = get(c);
                                            ServerHolidays.get().remove(holiday);

                                            c.getSource().sendAdmin("Removed holiday: " + holiday.getName());
                                            return 0;
                                        })
                                )

                                .then(literal("auto_give_items")
                                        .executes(c -> {
                                            Holiday holiday = get(c);

                                            c.getSource().sendMessage(
                                                    holiday.getName() + "will automatically give the reward items in its inventory: " +
                                                            holiday.isAutoGiveItems()
                                            );
                                            return 0;
                                        })

                                        .then(argument("bool", BoolArgumentType.bool())
                                                .executes(c -> {
                                                    Holiday holiday = get(c);
                                                    boolean autoGive = c.getArgument("bool", Boolean.class);

                                                    holiday.setAutoGiveItems(autoGive);

                                                    c.getSource().sendAdmin(holiday.getName() + " will auto give reward items: " + autoGive);
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("period")
                                        .executes(c -> {
                                            Holiday holiday = get(c);

                                            c.getSource().sendMessage(holiday.getName() + "'s date/timeframe: " + holiday.getPeriod());
                                            return 0;
                                        })

                                        .then(literal("exact")
                                                .then(argument("month", MONTH_ARG)
                                                        .then(argument("day", DATE_ARG)
                                                                .executes(c -> {
                                                                    Holiday holiday = get(c);

                                                                    Month month = c.getArgument("month", Month.class);
                                                                    int day = c.getArgument("day", Integer.class);

                                                                    holiday.setPeriod(MonthDayPeriod.exact(month, day));

                                                                    c.getSource().sendAdmin(holiday.getName() + "'s date is now " + holiday.getPeriod());
                                                                    return 0;
                                                                })
                                                        )
                                                )
                                        )

                                        .then(literal("between")
                                                .then(argument("m_start", MONTH_ARG)
                                                        .then(argument("d_start", DATE_ARG)
                                                                .then(argument("m_end", MONTH_ARG)
                                                                        .then(argument("d_end", DATE_ARG)
                                                                                .executes(c -> {
                                                                                    Holiday holiday = get(c);

                                                                                    Month mStart = c.getArgument("m_start", Month.class);
                                                                                    Month mEnd = c.getArgument("m_end", Month.class);

                                                                                    int dStart = c.getArgument("d_start", Integer.class);
                                                                                    int dEnd = c.getArgument("d_end", Integer.class);

                                                                                    holiday.setPeriod(
                                                                                            MonthDayPeriod.between(
                                                                                                    mStart, dStart,
                                                                                                    mEnd, dEnd
                                                                                            )
                                                                                    );

                                                                                    c.getSource().sendAdmin(holiday.getName() + "'s time frame is now " + holiday.getPeriod());
                                                                                    return 0;
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )

                                .then(literal("messages")
                                        .executes(c -> {
                                            Holiday holiday = get(c);
                                            List<Component> messages = holiday.getMails();

                                            if(messages.isEmpty()) {
                                                throw Exceptions.NOTHING_TO_LIST;
                                            }

                                            TextWriter writer = TextWriters.newWriter();
                                            writer.write(Component.text(holiday.getName() + "'s mail messages: "));

                                            writeMessages(holiday, writer);

                                            writer.newLine();
                                            writer.write(Component.text("                      ").decorate(TextDecoration.STRIKETHROUGH));

                                            c.getSource().sendMessage(writer.asComponent());
                                            return 0;
                                        })

                                        .then(literal("clear")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);
                                                    List<Component> messages = holiday.getMails();

                                                    messages.clear();

                                                    c.getSource().sendAdmin("Cleared " + holiday.getName() + "'s messages");
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .then(argument("index", IntegerArgumentType.integer(1))
                                                        .suggests((context, builder) -> {
                                                            Holiday holiday = get(context);
                                                            List<Component> messages = holiday.getMails();
                                                            int size = messages.size();

                                                            if (size < 1) return Suggestions.empty();

                                                            for (int i = 1; i <= size ; i++) {
                                                                builder.suggest(i, toTooltip(messages.get(i - 1)));
                                                            }

                                                            return builder.buildFuture();
                                                        })

                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            List<Component> messages = holiday.getMails();
                                                            int index = c.getArgument("index", Integer.class);

                                                            if (index > messages.size()) {
                                                                throw Exceptions.invalidIndex(index, messages.size());
                                                            }

                                                            Component removed = messages.remove(index - 1);

                                                            c.getSource().sendAdmin(
                                                                    Component.text("Removed a message from " + holiday.getName() + "'s messages: ")
                                                                            .append(removed)
                                                            );
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("add")
                                                .then(argument("chat", Arguments.CHAT)
                                                        .suggests((context, builder) -> {
                                                            return MessageSuggestions.get(
                                                                    context, builder,
                                                                    true,
                                                                    TOKEN_SUGGESTIONS
                                                            );
                                                        })

                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            List<Component> messages = holiday.getMails();

                                                            Component msg = c.getArgument("chat", Component.class);

                                                            messages.add(msg);

                                                            c.getSource().sendAdmin(
                                                                    Component.text("Added message to " + holiday.getName() + ": ")
                                                                            .append(msg)
                                                            );
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                );
    }

    void display(Holiday holiday, TextWriter writer) {
        writer.setFieldStyle(Style.style(NamedTextColor.GRAY));


        writer.write(holiday.getFilteredName() + ": ");

        writer.field("Name", holiday.getName());

        if (!holiday.getRhines().isNone()) {
            writer.field("Rhine reward", holiday.getRhines());
        }

        if (!holiday.getGems().isNone()) {
            writer.field("Gem reward", holiday.getGems());
        }

        writer.field("Period", holiday.getPeriod());

        if (!holiday.getMails().isEmpty()) {
            writer.newLine();
            writer.write("Messages: [");

            writeMessages(holiday, writer.withIndent());

            writer.newLine();
            writer.write("]");
        }

        writer.newLine();
        writer.write("Container: {");

        RewardContainer container = holiday.getContainer();
        var cWriter = writer.withIndent();
        cWriter.field("Type", container.isChest() ? "Chest" : "Shulker");
        cWriter.field("name", container.getName());

        if (!container.getLore().isEmpty()) {
            cWriter.newLine();
            cWriter.write("lore: [");

            TextWriter entryWriter = cWriter.withIndent();
            writeLore(container, entryWriter);

            cWriter.newLine();
            cWriter.write("]");
        }

        writer.newLine();
        writer.write("}");
    }

    private void writeLore(RewardContainer container, TextWriter writer) {
        int index = 1;

        for (var s: container.getLore()) {
            writer.line(index++  + ") ", NamedTextColor.GRAY);
            writer.write("'");
            writer.write(s);
            writer.write("'");
        }
    }

    private void writeMessages(Holiday holiday, TextWriter writer) {
        int index = 1;

        for (Component msg: holiday.getMails()) {
            writer.line(index++ + ") ", NamedTextColor.GRAY);
            writer.write("'");
            writer.write(msg);
            writer.write("'");
        }
    }

    private Holiday get(CommandContext<CommandSource> c) {
        return c.getArgument("holiday", Holiday.class);
    }

    private LiteralArgumentBuilder<CommandSource> currencies(String name, Function<Holiday, RewardRange> getter, BiConsumer<Holiday, RewardRange> setter) {
        return literal(name.toLowerCase() + "s")
                .executes(c -> {
                    Holiday holiday = get(c);
                    RewardRange range = getter.apply(holiday);

                    c.getSource().sendMessage(holiday.getName() + " " + name + " reward : " + range.toString());
                    return 0;
                })

                .then(argument("range", Arguments.REWARD_RANGE)
                        .executes(c -> {
                            Holiday holiday = get(c);
                            RewardRange range = c.getArgument("range", RewardRange.class);

                            setter.accept(holiday, range);

                            c.getSource().sendAdmin(holiday.getName() + "'s " + name + " reward is now " + range.toString());
                            return 0;
                        })
                );
    }
}