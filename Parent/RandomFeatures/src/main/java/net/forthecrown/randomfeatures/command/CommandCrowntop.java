package net.forthecrown.randomfeatures.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.randomfeatures.RandomFeatures;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandCrowntop extends CrownCommandBuilder {

    public CommandCrowntop(RandomFeatures plugin){
        super("crowntop", plugin);

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
           RandomFeatures.instance.showLeaderboard(getPlayerSender(c), "crown");
           return 0;
        });
    }
}
