package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.events.ChatEvents;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.concurrent.CompletableFuture;

public class StaffChatCommand extends CrownCommandBuilder {

    public StaffChatCommand(){
        super("staffchat", FtcCore.getInstance());

        setPermission("ftc.staffchat");
        setAliases("sc");
        setDescription("Sends a message to the staff chat");
        setUsage("&8Usage: &7/sc <message>");

        register();
    }

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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.then(argument("message", StringArgumentType.greedyString())
                .suggests()

                .executes(c -> {
                    ChatEvents.sendStaffChatMessage(c.getSource().getBukkitSender(), c.getArgument("message", String.class));
                    return 0;
                })
        );
    }

    public static <S> CompletableFuture<Suggestions> listCompletionsBranch(CommandContext<S> context, SuggestionsBuilder builder){
        String input = builder.getInput();
        String token = input.substring(input.lastIndexOf(' ')).trim();

        for (Branch r: Branch.values()){
            if(token.isBlank() || r.toString().regionMatches(true, 0, token, 0, token.length())) builder.suggest(r.toString());
        }
        return builder.buildFuture();
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length < 1) return false;

        if(args[0].contains("cmd:")){
            String cmd = args[0].toLowerCase().replaceAll("cmd:", "");

            switch (cmd){
                case "visible":
                    if(ChatEvents.ignoringStaffChat.contains(sender)){
                        ChatEvents.ignoringStaffChat.remove(sender);
                        sender.sendMessage(ChatColor.GRAY + "You will now see staff chat messages again");
                    } else {
                        ChatEvents.ignoringStaffChat.add(sender);
                        sender.sendMessage(ChatColor.GRAY + "You will no longer see staff chat messages");
                    }
                    return true;

                case "mute":
                    if(!sender.hasPermission("ftc.staffchat.admin")) break;
                    if(ChatEvents.scMuted){
                        sender.sendMessage(ChatColor.GRAY + "Staff Chat is no longer muted");
                        ChatEvents.sendStaffChatMessage(null, sender.getName() + " has unmuted staff chat");
                    }
                    else{
                        sender.sendMessage(ChatColor.GRAY + "Staff chat is now muted");
                        ChatEvents.sendStaffChatMessage(null, sender.getName() + " has muted staff chat");
                    }

                    ChatEvents.scMuted = !ChatEvents.scMuted;
                    return true;

                default:
            }
        }
        ChatEvents.sendStaffChatMessage(sender, String.join(" ", args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int argN = args.length-1;
        List<String> emojiList = new ArrayList<>();

        if(argN == 0 && args[0].contains("cmd:")){
            emojiList.add("cmd:visible");
            if(sender.hasPermission("ftc.staffchat.admin"))emojiList.add("cmd:mute");
        }

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

        emojiList.addAll(getPlayerNameList());

        return emojiList;
    }*/
}