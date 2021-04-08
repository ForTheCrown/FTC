package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

/**
 * Wrapper for the super class
 * <p>So plugin's only using the PaperMC API, and not the actual NMS methods, can use Brigadier as well</p>
 */
public class BrigadierCommand extends LiteralArgumentBuilder<CommandListenerWrapper> {

    public BrigadierCommand(String literal) {
        super(literal);
    }
}
