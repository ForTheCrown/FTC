package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.enums.Pet;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PetType implements ArgumentType<Pet> {
    public static final PetType PET = new PetType();

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
            reader.setCursor(cursor);
            throw UNKOWN_PET.createWithContext(reader, name);
        }

        return pet;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignorePerms){
        if(ignorePerms) return CommandSource.suggestMatching(builder, ListUtils.arrayToCollection(Pet.values(), p -> p.name().toLowerCase()));

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

            return CommandSource.suggestMatching(builder, result);
        } else return Suggestions.empty();
    }
}
