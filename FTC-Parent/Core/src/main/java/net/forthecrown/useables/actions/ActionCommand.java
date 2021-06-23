package net.forthecrown.core.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.useables.UsageAction;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ActionCommand implements UsageAction {
    public static final Key CONSOLE_KEY = Key.key(CrownCore.inst(), "command_console");
    public static final Key USER_KEY = Key.key(CrownCore.inst(), "command_user");

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
    public Key key() {
        return console ? CONSOLE_KEY : USER_KEY;
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
        int index = builder.getRemaining().indexOf(' ');
        if(index == -1) return CompletionProvider.suggestMatching(builder, Bukkit.getCommandMap().getKnownCommands().keySet());

        String cmd = builder.getRemaining().substring(0, index).trim();
        Command command = Bukkit.getCommandMap().getCommand(cmd);
        if(command == null) return Suggestions.empty();

        String[] args = builder.getRemaining().trim().split(" ");
        return CommandSource.suggestMatching(builder, command.tabComplete(context.getSource().asBukkit(), cmd, Arrays.copyOfRange(args, 1, args.length)));
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

    public boolean isConsole() {
        return console;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
