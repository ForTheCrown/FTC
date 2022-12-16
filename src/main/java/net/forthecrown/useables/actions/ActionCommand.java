package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.useables.*;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter
public class ActionCommand extends UsageAction {
    private static final Suggester<CommandSource> COMMAND_SUGGESTIONS = (context, builder) -> {
        String filteredInput = builder.getInput()
                .replaceAll("%p", "ap")
                .replaceAll("%plr", "aplr")
                .replaceAll("%player", "aplayer");

        builder = new SuggestionsBuilder(
                filteredInput,
                builder.getStart()
        );

        return FtcSuggestions.COMMAND_SUGGESTIONS.getSuggestions(context, builder);
    };

    // --- TYPE ---
    public static final UsageType<ActionCommand> TYPE_PLAYER = UsageType.of(ActionCommand.class)
            .setSuggests(COMMAND_SUGGESTIONS);

    public static final UsageType<ActionCommand> TYPE_SERVER = UsageType.of(ActionCommand.class)
            .setSuggests(COMMAND_SUGGESTIONS);

    private final boolean server;
    private final String command;

    public ActionCommand(UsageType<ActionCommand> type, String command) {
        super(type);
        this.command = command;

        this.server = type == TYPE_SERVER;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        String command = replaceSelectors(player.getName(), this.command);

        Bukkit.dispatchCommand(
                server ? Bukkit.getConsoleSender() : player,
                command
        );
    }

    public static String replaceSelectors(String name, String command) {
        return command
                .replaceAll("%plr", name)
                .replaceAll("%player", name)
                .replaceAll("%p", name);
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.format("cmd='{0}'", command);
    }

    @Override
    public @Nullable Tag save() {
        return StringTag.valueOf(command);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionCommand parse(UsageType<ActionCommand> type, StringReader reader, CommandSource source) throws CommandSyntaxException {
        String result = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        return new ActionCommand(type, result);
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionCommand load(UsageType<ActionCommand> type, Tag tag) {
        return new ActionCommand(type, tag.getAsString());
    }
}