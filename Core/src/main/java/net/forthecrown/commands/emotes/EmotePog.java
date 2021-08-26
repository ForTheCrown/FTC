package net.forthecrown.commands.emotes;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class EmotePog extends FtcCommand {

    public EmotePog(){
        super("pog", Crown.inst());

        setAliases("pgo", "poggers", "pogchamp", "pogo");
        setPermission(Permissions.EMOTE_POG);

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
        Component cMessage = Component.text()
                .color(NamedTextColor.GRAY)
                .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                .append(Component.text(" PogChamped"))
                .append(FtcUtils.isNullOrBlank(message) ? Component.empty() : Component.text(": ").append(Component.text(message).color(NamedTextColor.WHITE)))
                .build();

        Crown.getAnnouncer().announceToAllRaw(cMessage);
        Cooldown.add(user, "Core_Emote_Pog", 3*20);
    }
}
