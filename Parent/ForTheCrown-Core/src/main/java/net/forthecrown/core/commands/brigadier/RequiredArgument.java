package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.Iterator;

/**
 * A required argument, aka, enter something and then the specified argument type will attempt to parse it
 * <p>Another wrapper for NMS :D</p>
 * @param <T> The object to parse from an argument
 */
public class RequiredArgument<T> extends ArgumentBuilder<CommandListenerWrapper, RequiredArgument<T>> {
    private final String name;
    private final ArgumentType<T> type;
    private SuggestionProvider<CommandListenerWrapper> suggestionsProvider = null;

    public RequiredArgument(String name, ArgumentType<T> type) {
        this.name = name;
        this.type = type;
    }

    public static <T> RequiredArgument<T> argument(String name, ArgumentType<T> type) {
        return new RequiredArgument<>(name, type);
    }

    public RequiredArgument<T> suggests(SuggestionProvider<CommandListenerWrapper> provider) {
        this.suggestionsProvider = provider;
        return this.getThis();
    }

    public SuggestionProvider<CommandListenerWrapper> getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    @Override
    protected RequiredArgument<T> getThis() {
        return this;
    }

    public ArgumentType<T> getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public ArgumentCommandNode<CommandListenerWrapper, T> build() {
        ArgumentCommandNode<CommandListenerWrapper, T> result = new ArgumentCommandNode(this.getName(), this.getType(), this.getCommand(), this.getRequirement(), this.getRedirect(), this.getRedirectModifier(), this.isFork(), this.getSuggestionsProvider());
        Iterator var2 = this.getArguments().iterator();

        while(var2.hasNext()) {
            CommandNode<CommandListenerWrapper> argument = (CommandNode)var2.next();
            result.addChild(argument);
        }

        return result;
    }
}
