package net.forthecrown.commands.admin;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.arguments.HolidayArgument;
import net.forthecrown.commands.arguments.RewardRangeArgument;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.ServerHolidays;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FieldedWriter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.Month;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.forthecrown.core.ServerHolidays.*;

public class CommandHolidays extends FtcCommand {
    public static final EnumArgument<Month> MONTH_ARG = EnumArgument.of(Month.class);
    public static final IntegerArgumentType DATE_ARG = IntegerArgumentType.integer(1, 31);

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
                                        Crown.getHolidays().onDayChange();

                                        c.getSource().sendAdmin("Ran day update check");
                                        return 0;
                                    })
                            )
                    );
        }


        command
                .executes(c -> {
                    ComponentWriter writer = ComponentWriter.normal();
                    writer.write(Component.text("Current holidays: "));

                    for (var v: Crown.getHolidays().getAll()) {
                        writer.newLine();

                        ComponentWriter hoverWriter = ComponentWriter.normal();
                        display(v, hoverWriter);

                        writer.write(
                                Component.text(v.getFilteredName() + " [" + v.getPeriod() + "]")
                                        .hoverEvent(hoverWriter.get())
                                        .clickEvent(ClickEvent.suggestCommand("/" + getName() + " edit " + v.getName() + " "))
                                        .color(v.getPeriod().isActive() ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                        );
                    }

                    c.getSource().sendMessage(writer.get());
                    return 0;
                })

                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("month", MONTH_ARG)
                                        .then(argument("day", IntegerArgumentType.integer(1, 31))
                                                .executes(c -> {
                                                    String name = c.getArgument("name", String.class);

                                                    ServerHolidays holidays = Crown.getHolidays();

                                                    if(holidays.getHoliday(name) != null) {
                                                        throw FtcExceptionProvider.create("A holiday named " + name + " already exists");
                                                    }

                                                    Month month = c.getArgument("month", Month.class);
                                                    int day = c.getArgument("day", Integer.class);

                                                    ServerHolidays.Holiday created = new ServerHolidays.Holiday(name);
                                                    created.setPeriod(HolidayPeriod.exact(month, day));

                                                    holidays.addHoliday(created);

                                                    c.getSource().sendAdmin("Created holiday named " + name + ", which will run on " + created.getPeriod() + " of every year");
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                .then(literal("edit")
                        .then(argument("holiday", HolidayArgument.holiday())
                                .executes(c -> {
                                    Holiday holiday = get(c);

                                    ComponentWriter writer = ComponentWriter.normal();
                                    display(holiday, writer);

                                    c.getSource().sendMessage(writer.get());
                                    return 0;
                                })

                                .then(literal("give")
                                        .then(literal("-all")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);

                                                    if (holiday.hasNoRewards()) {
                                                        throw FtcExceptionProvider.create("Holiday has no rewards to give, no gems, rhines or items");
                                                    }

                                                    Crown.getHolidays().runHoliday(holiday);

                                                    c.getSource().sendAdmin("Giving all players " + holiday.getName() + " rewards");
                                                    return 0;
                                                })
                                        )

                                        .then(argument("user", UserArgument.user())
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    Holiday holiday = get(c);

                                                    if (holiday.hasNoRewards()) {
                                                        throw FtcExceptionProvider.create("Holiday has no rewards to give, no gems, rhines or items");
                                                    }

                                                    Crown.getHolidays().giveRewards(user, holiday);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Gave ")
                                                                    .append(user.nickDisplayName())
                                                                    .append(Component.text(" " + holiday.getName() + " rewards"))
                                                    );

                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("give_item")
                                        .then(argument("user", UserArgument.onlineUser())
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    Holiday holiday = get(c);

                                                    ItemStack item = Crown.getHolidays().getRewardItem(holiday, user);

                                                    if (ItemStacks.isEmpty(item)) {
                                                        throw FtcExceptionProvider.create(holiday.getName() + " has no reward items to give");
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

                                                .then(argument("str", StringArgumentType.greedyString())
                                                        .suggests((context, builder) -> {
                                                            builder = builder.createOffset(builder.getInput().lastIndexOf(' ')+1);

                                                            FtcSuggestionProvider.__suggestPlayerNamesAndEmotes(context, builder, true);
                                                            return CompletionProvider.suggestMatching(builder, RewardContainer.REPLACE_TAGS);
                                                        })

                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            RewardContainer container = holiday.getContainer();

                                                            String name = c.getArgument("str", String.class);
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
                                                        throw FtcExceptionProvider.create(holiday.getName() + " has no lore");
                                                    }

                                                    ComponentWriter writer = ComponentWriter.normal();
                                                    writer.write(holiday.getName() + "'s lore:");
                                                    writeLore(container, writer);

                                                    writer.newLine();
                                                    writer.write(Component.text("                ").decorate(TextDecoration.STRIKETHROUGH));

                                                    c.getSource().sendMessage(writer.get());
                                                    return 0;
                                                })

                                                .then(literal("remove")
                                                        .then(argument("index", IntegerArgumentType.integer(1))
                                                                .suggests((context, builder) -> {
                                                                    Holiday holiday = get(context);
                                                                    List<String> lores = holiday.getContainer().getLore();
                                                                    int size = lores.size();

                                                                    if (size < 1) return Suggestions.empty();

                                                                    for (int i = 1; i <= size ; i++) {
                                                                        builder.suggest(i, new LiteralMessage(lores.get(i - 1)));
                                                                    }

                                                                    return builder.buildFuture();
                                                                })

                                                                .executes(c -> {
                                                                    Holiday holiday = get(c);
                                                                    RewardContainer container = holiday.getContainer();

                                                                    int index = c.getArgument("index", Integer.class);

                                                                    if (index > container.getLore().size()) {
                                                                        throw FtcExceptionProvider.create("Invalid index: " + index + ", max index: " + container.getLore().size());
                                                                    }

                                                                    String removed = container.getLore().remove(index - 1);

                                                                    c.getSource().sendAdmin("Removed lore line " +
                                                                            index + ": '" + removed + "' from " +
                                                                            holiday.getName()
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
                                                        .then(argument("str", StringArgumentType.greedyString())
                                                                .suggests((context, builder) -> {
                                                                    builder = builder.createOffset(builder.getInput().lastIndexOf(' ')+1);

                                                                    FtcSuggestionProvider.__suggestPlayerNamesAndEmotes(context, builder, true);
                                                                    return CompletionProvider.suggestMatching(builder, RewardContainer.REPLACE_TAGS);
                                                                })

                                                                .executes(c -> {
                                                                    Holiday holiday = get(c);
                                                                    RewardContainer container = holiday.getContainer();

                                                                    String lore = c.getArgument("str", String.class);
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
                                            Crown.getHolidays().remove(holiday);

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

                                                                    holiday.setPeriod(HolidayPeriod.exact(month, day));

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
                                                                                            HolidayPeriod.between(
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
                                                throw FtcExceptionProvider.create(holiday.getName() + " has no messages");
                                            }

                                            ComponentWriter writer = ComponentWriter.normal();
                                            writer.write(Component.text(holiday.getName() + "'s mail messages: "));

                                            writeMessages(holiday, writer);

                                            writer.newLine();
                                            writer.write(Component.text("                      ").decorate(TextDecoration.STRIKETHROUGH));

                                            c.getSource().sendMessage(writer.get());
                                            return 0;
                                        })

                                        .then(literal("clear")
                                                .executes(c -> {
                                                    Holiday holiday = get(c);
                                                    List<Component> messages = holiday.getMails();

                                                    if(messages.isEmpty()) {
                                                        throw FtcExceptionProvider.create(holiday.getName() + " has no messages to clear");
                                                    }

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
                                                                builder.suggest(i, GrenadierUtils.componentToMessage(messages.get(i - 1)));
                                                            }

                                                            return builder.buildFuture();
                                                        })

                                                        .executes(c -> {
                                                            Holiday holiday = get(c);
                                                            List<Component> messages = holiday.getMails();

                                                            if(messages.isEmpty()) {
                                                                throw FtcExceptionProvider.create(holiday.getName() + " has no messages to remove");
                                                            }

                                                            int index = c.getArgument("index", Integer.class) - 1;

                                                            if(index >= messages.size()) {
                                                                throw FtcExceptionProvider.create("Index too big for message list, max index: " + messages.size());
                                                            }

                                                            Component removed = messages.remove(index);

                                                            c.getSource().sendAdmin(
                                                                    Component.text("Removed a message from " + holiday.getName() + "'s messages: ")
                                                                            .append(removed)
                                                            );
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("add")
                                                .then(argument("chat", ChatArgument.chat())
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

    void display(Holiday holiday, ComponentWriter w) {
        FieldedWriter writer = FieldedWriter.wrap(w);
        writer.write(holiday.getFilteredName() + ": ");

        writer.writeField("Name", holiday.getName());

        if (!holiday.getRhines().isNone()) {
            writer.writeField("Rhine reward", holiday.getRhines());
        }

        if (!holiday.getGems().isNone()) {
            writer.writeField("Gem reward", holiday.getGems());
        }

        writer.writeField("Period", holiday.getPeriod());

        if (!holiday.getMails().isEmpty()) {
            writer.newLine();
            writer.write("Messages: [");

            writeMessages(holiday, writer.prefixedWriter(Component.text("  ")));

            writer.newLine();
            writer.write("]");
        }

        writer.newLine();
        writer.write("Container: {");

        RewardContainer container = holiday.getContainer();
        FieldedWriter cWriter = FieldedWriter.wrap(writer.prefixedWriter(Component.text("  ")));
        cWriter.writeField("Type", container.isChest() ? "Chest" : "Shulker");
        cWriter.writeField("name", container.getName());

        if (!container.getLore().isEmpty()) {
            cWriter.newLine();
            cWriter.write("lore: [");

            ComponentWriter entryWriter = cWriter.prefixedWriter(Component.text("  "));
            writeLore(container, entryWriter);

            cWriter.newLine();
            cWriter.write("]");
        }

        writer.newLine();
        writer.write("}");
    }

    private void writeLore(RewardContainer container, ComponentWriter writer) {
        int index = 1;

        for (var s: container.getLore()) {
            writer.newLine();
            writer.write(index  + ") " + s);
        }
    }

    private void writeMessages(Holiday holiday, ComponentWriter writer) {
        int index = 1;

        for (Component msg: holiday.getMails()) {
            writer.newLine();
            writer.write(Component.text(index + ") ").color(NamedTextColor.GRAY));
            writer.write(msg);

            index++;
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

                .then(argument("range", RewardRangeArgument.range())
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