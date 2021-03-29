package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class BrigadierCommand extends LiteralArgumentBuilder<CommandListenerWrapper> {

    public BrigadierCommand(String literal) {
        super(literal);
    }
}
