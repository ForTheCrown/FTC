package net.forthecrown.core.commands.emotes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.jetbrains.annotations.Nullable;

public class EmotePog extends CrownCommandBuilder {

    public EmotePog(){
        super("pog", FtcCore.getInstance());

        setAliases("pgo", "poggers", "pogchamp", "pogo");
        setPermission("ftc.emotes.pog");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);

            if(Cooldown.contains(u, "Core_Emote_Pog")) throw new CrownCommandException("You pog too often lol");
            poggers(u, null);
            return 0;
        }).then(argument("text", StringArgumentType.greedyString())
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    String text = c.getArgument("text", String.class);
                    if(Cooldown.contains(user, "Core_Emote_Pog")) throw new CrownCommandException("You pog too often lol");

                    poggers(user, text);
                    return 0;
                })
        );
    }

    private static void poggers(CrownUser user, @Nullable String message){
        String text = user.getName() + " PogChamped";
        if(message != null && !message.isBlank()) text += ": " + message;
        Announcer.ac(text);
        Cooldown.add(user, "Core_Emote_Pog", 3*20);
    }
}
