package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.Pet;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PetArgument implements ArgumentType<Pet> {
    public static final PetArgument PET = new PetArgument();

    public static final DynamicCommandExceptionType UNKOWN_PET = new DynamicCommandExceptionType(o -> () -> "Unknown pet: " + o.toString().toLowerCase());
    public static final DynamicCommandExceptionType PET_NOT_OWNED = new DynamicCommandExceptionType(o -> () -> "You don't own this pet: " + o.toString().toLowerCase());

    public static Pet getPet(CommandContext<CommandSource> c, String argument){
        return c.getArgument(argument, Pet.class);
    }

    public static Pet getPetIfOwned(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        Pet p = getPet(c, argument);
        if(c.getSource().is(Player.class)){
            CrownUser user = UserManager.getUser(c.getSource().asPlayer());

            if(!user.hasPet(p)) throw PET_NOT_OWNED.create(p);
        }

        return p;
    }

    @Override
    public Pet parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        Pet pet;
        try {
            pet = Pet.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e){
            throw UNKOWN_PET.createWithContext(GrenadierUtils.correctReader(reader, cursor), name);
        }

        return pet;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignorePerms){
        if(ignorePerms) return CompletionProvider.suggestMatching(builder, ListUtils.arrayToList(Pet.values(), p -> p.name().toLowerCase()));

        if(context.getSource() instanceof CommandSource){
            CrownUser user;

            try {
                user = UserManager.getUser(((CommandSource) context.getSource()).asPlayer());
            } catch (CommandSyntaxException e) {
                return Suggestions.empty();
            }

            List<String> result = new ArrayList<>();
            for (Pet p: Pet.values()){
                if(user.hasPet(p)) result.add(p.name().toLowerCase());
            }

            return CompletionProvider.suggestMatching(builder, result);
        } else return Suggestions.empty();
    }
}
