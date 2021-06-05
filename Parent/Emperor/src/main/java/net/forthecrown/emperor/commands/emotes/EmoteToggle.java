package net.forthecrown.emperor.commands.emotes;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class EmoteToggle extends FtcCommand {

    public EmoteToggle(){
        super("toggleemotes", CrownCore.inst());

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
     * Valid usages of command:
     * - /toggleemotes
     *
     * Main Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
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
