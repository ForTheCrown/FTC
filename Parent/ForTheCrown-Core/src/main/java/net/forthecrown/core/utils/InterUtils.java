package net.forthecrown.core.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.item.ItemArgument;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class InterUtils {
    public static boolean isUsingFlag(StringReader reader){
        return reader.peek() == '-';
    }

    public static ItemStack parseItem(StringReader reader) throws CommandSyntaxException{
        int amount = reader.readInt();
        reader.skipWhitespace();

        return ItemArgument.itemStack().parse(reader).create(amount, true);
    }

    public static ItemStack parseGivenItem(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        if(isUsingFlag(reader)) return getReferencedItem(c, reader);

        return parseItem(reader);
    }

    public static ItemStack getReferencedItem(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        if(!isUsingFlag(reader)) return null;

        String reed = reader.readUnquotedString();
        if(!reed.contains("heldItem")) throw FtcExceptionProvider.createWithContext("Invalid flag: " + reed, reader);

        Player player = c.getSource().asPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        if(main == null || main.getType() == Material.AIR) throw FtcExceptionProvider.create("You must be holding an item");

        return main;
    }

    public static CompletableFuture<Suggestions> listItems(CommandContext<CommandSource> c, SuggestionsBuilder builder){
        if(builder.getRemaining().startsWith("-")) return CommandSource.suggestMatching(builder, Arrays.asList("-heldItem"));

        int index = builder.getRemaining().indexOf(' ');
        if(index == -1) return CommandSource.suggestMatching(builder, Arrays.asList("1", "8", "16", "32", "64"));

        builder = builder.createOffset(builder.getInput().lastIndexOf(' '));
        return ItemArgument.itemStack().listSuggestions(c, builder);
    }
}
