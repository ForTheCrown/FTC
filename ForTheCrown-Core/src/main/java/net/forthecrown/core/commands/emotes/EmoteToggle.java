package net.forthecrown.core.commands.emotes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class EmoteToggle extends CrownCommandBuilder {

    public EmoteToggle(){
        super("toggleemotes", FtcCore.getInstance());

        setPermission("ftc.emotes");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds player to config list "NoEmotes", which
     * disables receiving and sending these emotes:
     * 	- mwah
     * 	- poke
     * 	- bonk
     *  - jingle
     *  - scare
     *  - hug
     *
     *
     * Valid usages of command:
     * - /toggleemotes
     *
     * Main Author: Botul
     */

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        CrownUser user = FtcCore.getUser(player);
        String message = "&7You can longer send or receive emotes.";

        user.setAllowsEmotes(!user.allowsEmotes());
        if(user.allowsEmotes()) message = "&eYou can now send and receive emotes :D";

        user.sendMessage(message);
        return true;
    }*/

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c ->{
            CrownUser user = getUserSender(c);

            String message = "&7You can longer send or receive emotes.";

            user.setAllowsEmotes(!user.allowsEmotes());
            if(user.allowsEmotes()) message = "&eYou can now send and receive emotes :D";

            user.sendMessage(message);
            return 0;
        });
    }
}
