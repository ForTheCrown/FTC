package ftc.chat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class EmoteTabCompleter implements TabCompleter {
	
	/*
	 * Tabcompleter for emojis in /sc
	 * Author: Botul
	 */
	
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    	// Needed later, remember the -1
    	int argN = args.length-1; 
        
    	// Make a list of all the arguments, you can get more complex with this, only adding
    	// certain things to the list for specific perms or for passing some if statement.
    	List<String> emojiList = new ArrayList<>(); 
        emojiList.add(":shrug:");                   
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
        emojiList.add(":pleased:");
        
        //argN is used here to refer to the argument being typed out, partial matches narrows it down
        //so it doesn't give you all the arguments if you typed in some random stuff lol
        return StringUtil.copyPartialMatches(args[argN], emojiList, new ArrayList<>()); 
    }
}
