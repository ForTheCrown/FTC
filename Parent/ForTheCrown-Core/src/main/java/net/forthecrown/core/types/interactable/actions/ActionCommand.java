package net.forthecrown.core.types.interactable.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.types.interactable.InteractionAction;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ActionCommand implements InteractionAction {
    private String command;
    private final boolean console;

    public ActionCommand(boolean console) {
        this.console = console;
    }

    @Override
    public void parse(JsonElement json) {
        command = json.getAsString();
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        command = reader.getString();
    }

    @Override
    public void onInteract(Player player) {
        if (CrownUtils.isNullOrBlank(command)) return;

        String cmd = command.replaceAll("%p", player.getName());

        if (console) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else {
            player.performCommand(cmd);
        }
    }

    @Override
    public String getRegistrationName() {
        return "command_" + (console ? "console" : "user");
    }

    @Override
    public String asString() {
        return toString();
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
        return getClass().getSimpleName() +  "{" + "command='" + command + '\'' + ", executor=" + (console ? "console" : "user") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionCommand command1 = (ActionCommand) o;

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
