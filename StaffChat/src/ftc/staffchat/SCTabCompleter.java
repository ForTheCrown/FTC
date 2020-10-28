package ftc.staffchat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class SCTabCompleter implements TabCompleter { //Since you said you've never used a Tab Completer, let me, a dumb ass, explain this lol.
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) { //it works a lot like a command, you can use perm checks and all that other stuff
        int argN = args.length-1; //Needed later, remember the -1
        List<String> emojiList = new ArrayList<>(); //basically you just make a list of all the arguments, you can get more complex with this, only adding
        emojiList.add(":shrug:");                   //certain things to the list for specific perms or for passing some if statement
        emojiList.add(":ughcry:");
        emojiList.add(":gimme:");
        emojiList.add(":gimmecry:");
        emojiList.add(":bear:");
        emojiList.add(":smooch:");
        emojiList.add(":why:");
        emojiList.add(":tableflip:");
        emojiList.add(":tableput:");
        emojiList.add(":pretty:");
        emojiList.add(":sparkle:"); 
        emojiList.add(":blush:");
        emojiList.add(":sad:");
        return StringUtil.copyPartialMatches(args[argN], emojiList, new ArrayList<>()); //argN is used here to refer to the argument being typed out, partial matches narrows it down
    }                                                                                   //so it doesn't give you all the arguments if you typed in some random stuff lol
}
