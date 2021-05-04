package net.forthecrown.core.types.signs;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.item.ItemArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface SignAction extends JsonSerializable, SuggestionProvider<CommandSource> {

    void parse(JsonElement json) throws CommandSyntaxException;
    void parse(String input) throws CommandSyntaxException;

    void onInteract(Player player);

    Action getAction();

    String toString();

    boolean equals(Object o);
    int hashCode();

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    class SignCommand implements SignAction {
        private String command;
        private final boolean console;

        public SignCommand(boolean console){
            this.console = console;
        }

        @Override
        public void parse(JsonElement json) {
            command = json.getAsString();
        }

        @Override
        public void parse(String input) throws CommandSyntaxException {
            command = input;
        }

        @Override
        public void onInteract(Player player) {
            if(CrownUtils.isNullOrBlank(command)) return;

            String cmd = command.replaceAll("%p", player.getName());

            if(console) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            else player.performCommand(cmd);
        }

        @Override
        public Action getAction() {
            return console ? Action.COMMAND_CONSOLE : Action.COMMAND_USER;
        }

        @Override
        public JsonPrimitive serialize() {
            return new JsonPrimitive(command);
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return CommandSource.suggestMatching(builder, Bukkit.getCommandMap().getKnownCommands().keySet());
        }

        @Override
        public String toString() {
            return "SignCommand{" + "command='" + command + '\'' + ", executor=" + (console ? "console" : "user") + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SignCommand command1 = (SignCommand) o;

            return new EqualsBuilder()
                    .append(console, command1.console)
                    .append(command, command1.command)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(command)
                    .append(console)
                    .toHashCode();
        }
    }

    class SignGiveItem implements SignAction {
        private ItemStack item;

        @Override
        public void parse(JsonElement json) throws CommandSyntaxException {
            try {
                item = NbtGetter.itemFromNBT(NBT.of(MojangsonParser.parse(json.getAsString())));
            } catch (RuntimeException e){
                item = null;
                e.printStackTrace();
            }
        }

        @Override
        public void parse(String input) throws CommandSyntaxException {
            item = ItemArgument.itemStack().parse(new StringReader(input)).create(1, true);
        }

        @Override
        public void onInteract(Player player) {
            if(item == null) return;

            if(player.getInventory().firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), item.clone());
            else player.getInventory().addItem(item.clone());
        }

        @Override
        public Action getAction() {
            return Action.GIVE_ITEM;
        }

        @Override
        public JsonElement serialize() {
            if(item == null) return JsonNull.INSTANCE;

            return new JsonPrimitive(NbtGetter.ofItem(item).serialize());
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return ItemArgument.itemStack().listSuggestions(context, builder);
        }

        @Override
        public String toString() {
            return "SignGiveItem{" + "item=" + item + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SignGiveItem item1 = (SignGiveItem) o;

            return new EqualsBuilder()
                    .append(item, item1.item)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(item)
                    .toHashCode();
        }
    }

    class SignShowText implements SignAction {
        private static final GsonComponentSerializer serializer = GsonComponentSerializer.gson();
        private Component component;

        @Override
        public void parse(JsonElement json) throws CommandSyntaxException {
            try {
                component = serializer.deserializeFromTree(json);
            }catch (Exception e){
                component = null;
            }
        }

        @Override
        public void parse(String input) throws CommandSyntaxException {
            try {
                component = serializer.deserialize(input);
            } catch (Exception e){
                throw FtcExceptionProvider.create(e.getMessage());
            }
        }

        @Override
        public void onInteract(Player player) {
            if(component == null) return;
            player.sendMessage(component);
        }

        @Override
        public Action getAction() {
            return Action.SHOW_TEXT;
        }

        @Override
        public JsonElement serialize() {
            if(component == null) return new JsonObject();

            return serializer.serializeToTree(component);
        }

        @Override
        public String toString() {
            return "SignShowText{" + "component=" + serializer.serialize(component) + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            SignShowText text = (SignShowText) o;

            return new EqualsBuilder()
                    .append(component, text.component)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(component)
                    .toHashCode();
        }
    }

    class SignRemoveThing implements SignAction {

        private final boolean fromBal;
        private int amount;
        public SignRemoveThing(boolean fromBal){
            this.fromBal = fromBal;
        }

        @Override
        public void parse(JsonElement json) throws CommandSyntaxException {
            amount = json.getAsInt();
        }

        @Override
        public void parse(String input) throws CommandSyntaxException {
            try {
                amount = Integer.parseInt(input);
            } catch (NumberFormatException e){
                throw FtcExceptionProvider.create("Couldn't parse integer");
            }
        }

        @Override
        public void onInteract(Player player) {
            if(fromBal) FtcCore.getBalances().add(player.getUniqueId(), -amount);
            else {
                CrownUser user = UserManager.getUser(player);
                user.setGems(user.getGems() - amount);
            }
        }

        @Override
        public Action getAction() {
            return fromBal ? Action.REMOVE_BALANCE : Action.REMOVE_GEMS;
        }

        @Override
        public JsonElement serialize() {
            return new JsonPrimitive(amount);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SignRemoveThing thing = (SignRemoveThing) o;

            return new EqualsBuilder()
                    .append(fromBal, thing.fromBal)
                    .append(amount, thing.amount)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(fromBal)
                    .append(amount)
                    .toHashCode();
        }
    }

    enum Action {
        COMMAND_USER (() -> new SignCommand(false)),
        COMMAND_CONSOLE (() -> new SignCommand(true)),

        REMOVE_BALANCE (() -> new SignRemoveThing(true)),
        REMOVE_GEMS (() -> new SignRemoveThing(false)),

        GIVE_ITEM (SignGiveItem::new),
        SHOW_TEXT (SignShowText::new);

        private final Supplier<SignAction> supplier;
        Action(Supplier<SignAction> supplier){
            this.supplier = supplier;
        }

        public SignAction get(){
            return supplier.get();
        }
    }
}
