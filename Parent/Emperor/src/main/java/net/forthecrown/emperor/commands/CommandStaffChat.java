package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.StaffChat;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandStaffChat extends FtcCommand {

    public CommandStaffChat(){
        super("staffchat", CrownCore.inst());

        setPermission(Permissions.STAFF_CHAT);
        setAliases("sc");
        setDescription("Sends a message to the staff chat");

        register();

        emojiList.add(":shrug:");
        emojiList.add(":ughcry:");
        emojiList.add(":hug:");
        emojiList.add(":hugcry:");
        emojiList.add(":bear:");
        emojiList.add(":smooch:");
        emojiList.add(":why:");
        emojiList.add(":tableflip:");
        emojiList.add(":tableput:");
        emojiList.add(":pretty:");
        emojiList.add(":sparkle:");
        emojiList.add(":blush:");
        emojiList.add(":sad:");
        emojiList.add(":pleased:");
        emojiList.add(":fedup:");
        emojiList.add(":reallysad:");
    }

    private final static List<String> emojiList = new ArrayList<>();

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Sends a message to the staff chat
     *
     *
     * Valid usages of command:
     * - /staffchat
     * - /sc
     *
     * Permissions used:
     * - ftc.staffchat
     *
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("message", StringArgumentType.greedyString())
                .suggests(this::completions)

                .executes(c -> {
                    if(c.getSource().isPlayer() && StaffChat.ignoring.contains(c.getSource().asPlayer())){
                        throw FtcExceptionProvider.create("You are ingoring staff chat, do '/sct visible' to use it again");
                    }

                    StaffChat.send(c.getSource(), ChatFormatter.formatStringIfAllowed(c.getArgument("message", String.class), Bukkit.getConsoleSender()), true);
                    return 0;
                })
        );
    }

    private <S> CompletableFuture<Suggestions> completions(CommandContext<S> context, SuggestionsBuilder builder){
        String input = builder.getInput();
        builder = builder.createOffset(input.lastIndexOf(' ')+1);

        List<String> argList = new ArrayList<>(emojiList);
        for (Player p: Bukkit.getOnlinePlayers()){
            argList.add(p.getName());
        }

        return CompletionProvider.suggestMatching(builder, argList);
    }
}