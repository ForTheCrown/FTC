package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.TargetSelectorType;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.utils.Cooldown;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class _template extends CrownCommandBuilder {

    //name is the command lol /name
    public _template(@NotNull String name, @NotNull Plugin plugin) {
        super(name, plugin);

        //The only 2 useable ones lol
        setPermission("permission.permission");
        setPermissionMessage("No permission >:(");

        register();//Required to make the command useable
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Describe the command
     *
     *
     * Valid usages of command:
     * - /<command> <args>
     *
     * Author:
     */

    /*
     * Quick preword:
     * Brigadier is an NMS (Net.Minecraft.Server) thing, so it doesn't use the CommandSender type, instead it has
     * a CommandListenerWrapper, as Bukkit calls it, Mojang calls it a CommandSourceStack lol
     *
     * You can get the CommandSender from the ListenerWrapper with the getBukkitSender method
     */

    //Method creating command logic
    @Override
    protected void registerCommand(BrigadierCommand command) {
        command //argument() is a method from the CrownCommandBuilder superclass
                .then(argument("Literal") //Literal argument, the exact string must be entered to use
                        .executes(c -> { //c is CommandContext, provided by Mojang
                            Player player = getPlayerSender(c); //Method from super class, if executor is a non player, tells them to sod off
                            CrownUser user = getUserSender(c); //Same as the above, but gets the player's user class

                            return 0; //You have to return a number, it doesn't matter what the number is
                        })
        )
                .then(argument("required", FloatArgumentType.floatArg(0, 2)) // Required Argument, Checking and parsing is done autmatically done by Brigadier
                        //First parameter is min, second is max, you can have only the first parameter, or no parameters at all
                        .suggests((c, b) -> UserType.listSuggestions(b)) //Tab completions for the argument, c is CommandContext
                        //b is SuggestionBuilder, another Mojang thing
                        //If you have a specific set of strings you want to return, use:
                        //suggestMatching(SuggestionBuilder b, String... args) or
                        //suggestMatching(SuggestionBuilder b, Collection<String> args)
                        //Most NMS argument types will provide the suggestions automatically, without you having to write extra code

                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            float argument = c.getArgument("required", Float.class); //This is an easy way of getting primitive types
                            // such as strings, ints, bytes, booleans, floats

                            float argument1 = FloatArgumentType.getFloat(c, "required"); //If you're using any Type in
                            //net.forthecrown.core.commands.brigadier.types, then use this type of to get the variable. Otherwise
                            //It'll try to return an NMS type. Example:
                            Collection<? extends Entity> entities = TargetSelectorType.getEntities(c, "Argument");

                            //Brigadier can use CommandSyntaxException to end a command's execution
                            //CrownCommandException is a child class of that, it only requires a message that gets sent to the player
                            if(!player.isFlying()) throw new CrownCommandException("Error message to be sent to player");

                            //The Cooldown is a utility class lol
                            if(Cooldown.contains(player, "cooldown_category")) return 0;
                            Cooldown.add(player, "category_something", 20);
                            //Player to add into cooldown, can be any Entity, as the paramater requires a CommandSender
                            //second parameter is the category, required to stop cooldowns from overlapping
                            //Thirt parameter is the cooldown time in ticks

                            //Broadcasts an admin message, this is the "[BotulToxin: screwed up again]" thing
                            //Only OP's - and the sender - see this, as far as I know
                            broadcastAdmin(c.getSource(), "Message");

                            return 0;
                        })
                );
    }
}
