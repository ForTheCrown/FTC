package net.forthecrown.useables;

import com.google.gson.JsonElement;
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
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.item.ItemArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

//DO NOT EDIT THIS CLASS, IF YOU DO, THE CODE WONT COMPILE
//?????????????????????????
public final class InteractionUtils {
    private InteractionUtils() {}

    private static boolean isUsingFlag(StringReader reader){
        return reader.peek() == '-';
    }

    public static LiteralArgumentBuilder<CommandSource> literal(String name){
        LiteralArgumentBuilder<CommandSource> result = LiteralArgumentBuilder.literal(name);
        return result;
    }

    public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type){
        RequiredArgumentBuilder<CommandSource, T> result = RequiredArgumentBuilder.argument(name, type);
        return result;
    }

    public static LiteralArgumentBuilder<CommandSource> actionsArguments(BrigadierFunction<Actionable> p){
        return literal("actions")
                .then(literal("list")
                        .executes(c -> {
                            Actionable sign = p.apply(c);

                            int index = 0;
                            TextComponent.Builder builder = Component.text().append(Component.text("Interaction actions:"));

                            for (UsageActionInstance action: sign.getActions()){
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
                                .then(argument("type", RegistryArguments.usageAction())
                                        .then(argument("toParse", StringArgumentType.greedyString())
                                                .suggests((c, b) -> {
                                                    try {
                                                        return RegistryArguments.getAction(c, "type").getSuggestions(c, b);
                                                    } catch (Exception ignored) {}
                                                    return Suggestions.empty();
                                                })

                                        .executes(c -> {
                                            Actionable sign = p.apply(c);
                                            UsageAction<?> action = RegistryArguments.getAction(c, "type");
                                            String toParse = StringArgumentType.getString(c, "toParse");

                                            StringReader reader = new StringReader(toParse);
                                            UsageActionInstance instance = action.parse(reader, (CommandSource) c.getSource());

                                            if(reader.canRead()) {
                                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().create();
                                            }

                                            sign.addAction(instance);

                                            ((CommandSource) c.getSource()).sendAdmin("Successfully added action");
                                            return 0;
                                        })
                                )
                        )
                )

                /*.then(literal("clear")
                        .executes(c -> {
                            Actionable sign = p.apply(c);
                            sign.clearActions();

                            c.getSource().sendAdmin("Cleared actions");
                            return 0;
                        })
                )*/;
    }

    public static LiteralArgumentBuilder<CommandSource> checksArguments(BrigadierFunction<Preconditionable> p){
        return literal("checks")
                .then(literal("list")
                        .executes(c -> {
                            Preconditionable sign = p.apply(c);

                            int index = 0;
                            TextComponent.Builder builder = Component.text().append(Component.text("Interaction checks:"));

                            for (UsageCheckInstance pa: sign.getChecks()){
                                builder.append(Component.newline());
                                builder.append(Component.text(index + ") " + pa.asString()));
                                index++;
                            }

                            c.getSource().sendMessage(builder.build());
                            return 0;
                        })
                )

                .then(literal("remove")
                        .then(argument("key", RegistryArguments.usageCheck())
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
                        .then(argument("type", RegistryArguments.usageCheck())
                                .then(argument("toParse", StringArgumentType.greedyString())
                                        .suggests((c, b) -> {
                                            try {
                                                return RegistryArguments.getCheck(c, "type").getSuggestions(c, b);
                                            } catch (Exception ignored) {}
                                            return Suggestions.empty();
                                        })

                                        .executes(c -> {
                                            Preconditionable sign = p.apply(c);
                                            UsageCheck<?> pa = RegistryArguments.getCheck(c, "type");
                                            String toParse = StringArgumentType.getString(c, "toParse");

                                            StringReader reader = new StringReader(toParse);
                                            UsageCheckInstance instance = pa.parse(reader, (CommandSource) c.getSource());

                                            if(reader.canRead()) {
                                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().create();
                                            }

                                            sign.addCheck(instance);

                                            ((CommandSource) c.getSource()).sendAdmin("Successfully added check");
                                            return 0;
                                        })
                                )
                        )
                )

                /*.then(literal("clear")
                        .executes(c -> {
                            Preconditionable sign = p.apply(c);
                            sign.clearChecks();

                            c.getSource().sendAdmin("Cleared checks");
                            return 0;
                        })
                )*/;
    }

    public static void ensureReaderEnd(StringReader reader) throws CommandSyntaxException {
        if(reader.canRead()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().create();
        }
    }

    public static ItemStack parseItem(StringReader reader) throws CommandSyntaxException {
        int amount = reader.readInt();
        reader.skipWhitespace();

        return ItemArgument.itemStack().parse(reader).create(amount, true);
    }

    public static ItemStack parseGivenItem(CommandSource c, StringReader reader) throws CommandSyntaxException {
        if(isUsingFlag(reader)) return getReferencedItem(c, reader);

        return parseItem(reader);
    }

    public static ItemStack getReferencedItem(CommandSource c, StringReader reader) throws CommandSyntaxException {
        if(!isUsingFlag(reader)) return null;

        String reed = reader.readUnquotedString();
        if(!reed.contains("heldItem")) throw FtcExceptionProvider.createWithContext("Invalid flag: " + reed, reader);

        Player player = c.asPlayer();
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

    public static JsonElement writeAction(UsageActionInstance instance){
        UsageAction type = Registries.USAGE_ACTIONS.get(instance.typeKey());

        return type.serialize(instance);
    }

    public static UsageActionInstance readAction(String strKey, JsonElement element) throws CommandSyntaxException {
        Key key = FtcUtils.parseKey(strKey);

        return (UsageActionInstance) Registries.USAGE_ACTIONS.get(key).deserialize(element);
    }

    public static JsonElement writeCheck(UsageCheckInstance instance){
        UsageCheck type = Registries.USAGE_CHECKS.get(instance.typeKey());

        return type.serialize(instance);
    }

    public static UsageCheckInstance readCheck(String strKey, JsonElement element) throws CommandSyntaxException {
        Key key = FtcUtils.parseKey(strKey);

        return (UsageCheckInstance) Registries.USAGE_CHECKS.get(key).deserialize(element);
    }
}
