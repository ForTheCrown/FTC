package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.StaffChat;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandStaffChat extends CrownCommandBuilder {

    public CommandStaffChat(){
        super("staffchat", FtcCore.getInstance());

        setPermission("ftc.staffchat");
        setAliases("sc");
        setDescription("Sends a message to the staff chat");
        setUsage("&8Usage: &7/sc <message>");

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
     * Referenced other classes:
     * - FtcCore: FtcCore.replaceEmojis
     * - ChatEvents: ChatEvents.sendStaffChatMessage and other variables
     * Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument("message", StringArgumentType.greedyString())
                .suggests(CommandStaffChat::staffChatCompletions)

                .executes(c -> {
                    StaffChat.send(c.getSource().getBukkitSender(), c.getArgument("message", String.class), true);
                    return 0;
                })
        );
    }



    private static <S> CompletableFuture<Suggestions> staffChatCompletions(CommandContext<S> context, SuggestionsBuilder builder){
        String input = builder.getInput();
        builder = builder.createOffset(input.lastIndexOf(' ')+1);

        List<String> argList = new ArrayList<>(emojiList);
        for (Player p: Bukkit.getOnlinePlayers()){
            argList.add(p.getName());
        }

        return suggestMatching(builder, argList);
    }
}