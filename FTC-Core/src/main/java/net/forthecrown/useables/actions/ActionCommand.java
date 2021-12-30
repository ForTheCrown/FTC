package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionCommand implements UsageAction<ActionCommand.ActionInstance> {
    public static final Key CONSOLE_KEY = Keys.forthecrown("command_console");
    public static final Key USER_KEY = Keys.forthecrown("command_user");

    private final boolean console;

    public ActionCommand(boolean console) {
        this.console = console;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(reader.getString(), console);
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(element.getAsString(), console);
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return new JsonPrimitive(value.getCommand());
    }

    @Override
    public @NotNull Key key() {
        return console ? CONSOLE_KEY : USER_KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestMatching(builder, Bukkit.getCommandMap().getKnownCommands().keySet());
    }

    public static class ActionInstance implements UsageActionInstance {
        private final String command;
        private final boolean console;

        public ActionInstance(String command, boolean console) {
            this.command = command;
            this.console = console;
        }

        @Override
        public void onInteract(Player player) {
            if (FtcUtils.isNullOrBlank(command)) return;

            String cmd = command.replaceAll("%p", player.getName());

            if (console) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else {
                player.performCommand(cmd);
            }
        }

        @Override
        public Key typeKey() {
            return console ? CONSOLE_KEY : USER_KEY;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "cmd=" + '\'' + command + '\'' + '}';
        }

        public boolean isConsole() {
            return console;
        }

        public String getCommand() {
            return command;
        }
    }
}
