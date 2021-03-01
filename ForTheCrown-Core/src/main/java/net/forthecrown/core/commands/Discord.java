package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class Discord extends CrownCommandBuilder {
    public Discord(){
        super("Discord", FtcCore.getInstance());

        setPermission(null);
        setDescription("Gives you the servers discord link.");
        register();
    }

    /*
    * Just sends the player the discord link
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command){
        command.executes(c ->{
            c.getSource().getBukkitSender().sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
            return 0;
        });
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        sender.sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
        return true;
    }*/
}
