package net.forthecrown.randomfeatures.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.randomfeatures.RandomFeatures;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandDeathtop extends CrownCommandBuilder {

    public CommandDeathtop(RandomFeatures plugin){
        super("deathtop", plugin);

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            RandomFeatures.instance.showLeaderboard(getPlayerSender(c), "Death");
            return 0;
        });
    }
}
