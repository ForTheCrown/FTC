package net.forthecrown.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.item.ItemArgument;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

//DO NOT EDIT THIS CLASS, IF YOU DO, THE CODE WONT COMPILE
//?????????????????????????
public final class InteractionUtils {
    private InteractionUtils() {}

    private static boolean isUsingFlag(StringReader reader){
        return reader.peek() == '-';
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
        if(FtcItems.isEmpty(main)) throw FtcExceptionProvider.create("You must be holding an item");

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

    // ----------------------------------------------
    // The following serialization functions exist to
    // make any future changes to checks and actions
    // serialization easier to implement
    // ----------------------------------------------

    public static void loadChecks(Checkable checkable, JsonObject json) throws CommandSyntaxException {
        checkable.clearChecks();

        JsonElement checks = json.get("preconditions");
        if(checks == null || !checks.isJsonObject()) return;

        for (Map.Entry<String, JsonElement> e: checks.getAsJsonObject().entrySet()){
            checkable.addCheck(readCheck(e.getKey(), e.getValue()));
        }
    }

    public static void saveChecks(Checkable checkable, JsonObject jsonObject) {
        if(checkable.getChecks().isEmpty()) return;
        JsonWrapper json = JsonWrapper.empty();

        for (UsageCheckInstance c: checkable.getChecks()) {
            json.add(c.typeKey().asString(), writeCheck(c));
        }

        jsonObject.add("preconditions", json.getSource());
    }

    public static void loadActions(Actionable actionable, JsonObject json) {
        actionable.clearActions();

        JsonElement actionsElement = json.get("actions");
        if(actionsElement == null || !actionsElement.isJsonArray()) return;

        for (JsonElement e: actionsElement.getAsJsonArray()) {
            JsonWrapper j = JsonWrapper.of(e.getAsJsonObject());

            try {
                actionable.addAction(readAction(j.getString("type"), j.get("value")));
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void saveActions(Actionable actionable, JsonObject jsonObject) {
        if(actionable.getActions().isEmpty()) return;

        JsonArray array = new JsonArray();

        for (UsageActionInstance a: actionable.getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.typeKey().asString()));
            object.add("value", writeAction(a));

            array.add(object);
        }

        jsonObject.add("actions", array);
    }
}
