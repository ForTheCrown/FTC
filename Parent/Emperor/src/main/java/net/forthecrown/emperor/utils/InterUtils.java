package net.forthecrown.emperor.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.commands.arguments.ActionArgType;
import net.forthecrown.emperor.commands.arguments.CheckArgType;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.useables.Actionable;
import net.forthecrown.emperor.useables.Preconditionable;
import net.forthecrown.emperor.useables.UsageAction;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.item.ItemArgument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class InterUtils {
    public static boolean isUsingFlag(StringReader reader){
        return reader.peek() == '-';
    }

    public static LiteralArgumentBuilder<CommandSource> literal(String name){
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type){
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static LiteralArgumentBuilder<CommandSource> actionsArguments(BrigadierFunction<Actionable> p){
        return literal("actions")
                        .then(literal("list")
                                .executes(c -> {
                                    Actionable sign = p.apply(c);

                                    int index = 0;
                                    TextComponent.Builder builder = Component.text().append(Component.text("Interaction actions:"));

                                    for (UsageAction action: sign.getActions()){
                                        builder.append(Component.newline());
                                        builder.append(Component.text(index + ") " + action.asString()));
                                        index++;
                                    }

                                    c.getSource().sendMessage(builder.build());
                                    return 0;
                                })
                        )

                        .then(literal("remove")
                                .then(argument("index", IntegerArgumentType.integer(0))
                                        .executes(c -> {
                                            Actionable sign = p.apply(c);
                                            int index = c.getArgument("index", Integer.class);

                                            sign.removeAction(index);

                                            c.getSource().sendAdmin("Removed action with index " + index);
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("add")
                                .then(argument("type", ActionArgType.action())
                                        .then(argument("toParse", StringArgumentType.greedyString())
                                                .suggests((c, b) -> {
                                                    try {
                                                        return ActionArgType.getAction(c, "type").getSuggestions(c, b);
                                                    } catch (Exception ignored) {}
                                                    return Suggestions.empty();
                                                })

                                                .executes(c -> {
                                                    Actionable sign = p.apply(c);
                                                    UsageAction action = ActionArgType.getAction(c, "type");
                                                    String toParse = c.getArgument("toParse", String.class);

                                                    action.parse(c, new StringReader(toParse));
                                                    sign.addAction(action);

                                                    c.getSource().sendAdmin("Successfully added action");
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("clear")
                                .executes(c -> {
                                    Actionable sign = p.apply(c);
                                    sign.clearActions();

                                    c.getSource().sendAdmin("Cleared actions");
                                    return 0;
                                })
                        );
    }

    public static LiteralArgumentBuilder<CommandSource> checksArguments(BrigadierFunction<Preconditionable> p){
        return literal("checks")
                .then(literal("list")
                        .executes(c -> {
                            Preconditionable sign = p.apply(c);

                            int index = 0;
                            TextComponent.Builder builder = Component.text().append(Component.text("Interaction checks:"));

                            for (UsageCheck pa: sign.getChecks()){
                                builder.append(Component.newline());
                                builder.append(Component.text(index + ") " + pa.asString()));
                                index++;
                            }

                            c.getSource().sendMessage(builder.build());
                            return 0;
                        })
                )

                .then(literal("remove")
                        .then(argument("key", CheckArgType.precondition())
                                .suggests((c, b) -> CompletionProvider.suggestMatching(b, p.apply(c).getStringCheckTypes()))

                                .executes(c -> {
                                    Preconditionable sign = p.apply(c);
                                    Key key = c.getArgument("key", Key.class);

                                    sign.removeCheck(key);

                                    c.getSource().sendAdmin("Removed check " + key.asString());
                                    return 0;
                                })
                        )
                )

                .then(literal("put")
                        .then(argument("type", CheckArgType.precondition())
                                .then(argument("toParse", StringArgumentType.greedyString())
                                        .suggests((c, b) -> {
                                            try {
                                                return CheckArgType.getCheck(c, "type").getSuggestions(c, b);
                                            } catch (Exception ignored) {}
                                            return Suggestions.empty();
                                        })

                                        .executes(c -> {
                                            Preconditionable sign = p.apply(c);
                                            UsageCheck pa = CheckArgType.getCheck(c, "type");

                                            String toParse = c.getArgument("toParse", String.class);
                                            pa.parse(c, new StringReader(toParse));

                                            sign.addCheck(pa);

                                            c.getSource().sendAdmin("Successfully added check");
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("clear")
                        .executes(c -> {
                            Preconditionable sign = p.apply(c);
                            sign.clearChecks();

                            c.getSource().sendAdmin("Cleared checks");
                            return 0;
                        })
                );
    }

    public static ItemStack parseItem(StringReader reader) throws CommandSyntaxException{
        int amount = reader.readInt();
        reader.skipWhitespace();

        return ItemArgument.itemStack().parse(reader).create(amount, true);
    }

    public static ItemStack parseGivenItem(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        if(isUsingFlag(reader)) return getReferencedItem(c, reader);

        return parseItem(reader);
    }

    public static ItemStack getReferencedItem(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        if(!isUsingFlag(reader)) return null;

        String reed = reader.readUnquotedString();
        if(!reed.contains("heldItem")) throw FtcExceptionProvider.createWithContext("Invalid flag: " + reed, reader);

        Player player = c.getSource().asPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        if(main == null || main.getType() == Material.AIR) throw FtcExceptionProvider.create("You must be holding an item");

        return main.clone();
    }

    public static CompletableFuture<Suggestions> listItems(CommandContext<CommandSource> c, SuggestionsBuilder builder){
        int index = builder.getRemaining().indexOf(' ');
        if(index == -1) return CompletionProvider.suggestMatching(builder, Arrays.asList("1", "8", "16", "32", "64", "-heldItem"));

        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);
        return ItemArgument.itemStack().listSuggestions(c, builder);
    }

    public interface BrigadierFunction<F> {
        F apply(CommandContext<CommandSource> from) throws CommandSyntaxException;
    }
}
