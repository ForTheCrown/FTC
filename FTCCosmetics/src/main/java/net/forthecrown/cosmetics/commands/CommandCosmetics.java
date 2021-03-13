package net.forthecrown.cosmetics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.cosmetics.Cosmetics;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandCosmetics extends CrownCommandBuilder {

    public CommandCosmetics(){
        super("cosmetics", Cosmetics.plugin);

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
           CrownUser u = getUserSender(c);
           u.getPlayer().openInventory(Cosmetics.plugin.getMainCosmeticInventory(u));
           return 0;
        });
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the Cosmetics menu
     *
     *
     * Valid usages of command:
     * - /cosmetics
     *
     * Referenced other classes:
     * - Main: Main.plugin
     *
     * Author: Wout
     */
}
