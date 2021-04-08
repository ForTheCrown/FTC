package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.Iterator;

/**
 * A literal argument, aka enter the exact provided string to use it
 * <p>Another NMS wrapper lol</p>
 */
public class LiteralArgument extends ArgumentBuilder<CommandListenerWrapper, LiteralArgument> {
    private final String literal;

    public LiteralArgument(String literal) {
        this.literal = literal;
    }

    public static LiteralArgument literal(String name) {
        return new LiteralArgument(name);
    }

    protected LiteralArgument getThis() {
        return this;
    }

    public String getLiteral() {
        return this.literal;
    }

    public LiteralCommandNode<CommandListenerWrapper> build() {
        LiteralCommandNode<CommandListenerWrapper> result = new LiteralCommandNode(this.getLiteral(), this.getCommand(), this.getRequirement(), this.getRedirect(), this.getRedirectModifier(), this.isFork());
        Iterator var2 = this.getArguments().iterator();

        while(var2.hasNext()) {
            CommandNode<CommandListenerWrapper> argument = (CommandNode)var2.next();
            result.addChild(argument);
        }

        return result;
    }
}
