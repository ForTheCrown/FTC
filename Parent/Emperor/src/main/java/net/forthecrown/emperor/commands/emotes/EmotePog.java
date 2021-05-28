package net.forthecrown.emperor.commands.emotes;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.emperor.Announcer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.Cooldown;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;

public class EmotePog extends CrownCommandBuilder {

    public EmotePog(){
        super("pog", CrownCore.inst());

        setAliases("pgo", "poggers", "pogchamp", "pogo");
        setPermission("ftc.emotes.pog");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);

            if(Cooldown.contains(u, "Core_Emote_Pog")) throw FtcExceptionProvider.create("You pog too often lol");
            poggers(u, null);
            return 0;
        }).then(argument("text", StringArgumentType.greedyString())
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    String text = c.getArgument("text", String.class);
                    if(Cooldown.contains(user, "Core_Emote_Pog")) throw FtcExceptionProvider.create("You pog too often lol");

                    poggers(user, text);
                    return 0;
                })
        );
    }

    private static void poggers(CrownUser user, @Nullable String message){
        String text = ChatColor.YELLOW + user.getName() + ChatColor.GRAY + " PogChamped" + ChatColor.RESET;
        if(message != null && !message.isBlank()) text += ": " + message;
        Announcer.ac(text);
        Cooldown.add(user, "Core_Emote_Pog", 3*20);
    }
}
