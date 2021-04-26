package net.forthecrown.core.commands.brigadier.types.custom;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Pet;
import net.forthecrown.core.utils.ListUtils;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PetType extends CrownArgType<Pet> {

    private static final PetType PET = new PetType();

    private PetType(){
        super(obj -> new LiteralMessage("Unknown pet: " + obj.toString()));
    }

    @Override
    protected Pet parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String origName = reader.readUnquotedString();
        String name = origName.toUpperCase();
        if(!name.endsWith("_PARROT")) name += "_PARROT";

        Pet result;
        try {
            result = Pet.valueOf(name);
        } catch (IllegalArgumentException e){
            reader.setCursor(cursor);
            throw exception.createWithContext(reader, origName);
        }

        return result;
    }

    public static StringArgumentType pet(){
        return StringArgumentType.word();
    }

    public static <S> Pet getPet(CommandContext<S> context, String argument) throws CommandSyntaxException {
        return PET.parse(inputToReader(context, argument));
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> context, SuggestionsBuilder builder){
        return CrownCommandBuilder.suggestMatching(builder, ListUtils.arrayToCollection(Pet.values(), pet -> pet.toString().toLowerCase().replaceAll("_parrot", "")));
    }

    public static CompletableFuture<Suggestions> suggestUserAware(CommandContext<CommandListenerWrapper> context, SuggestionsBuilder builder){
        if(!(context.getSource().getBukkitEntity() instanceof Player)) return Suggestions.empty();
        CrownUser user = UserManager.getUser(context.getSource().getBukkitEntity().getUniqueId());

        List<String> pets = new ArrayList<>();

        for (Pet p: Pet.values()){
            if (user.hasPet(p)) pets.add(p.toString().toLowerCase().replaceAll("_parrot", ""));
        }

        return CrownCommandBuilder.suggestMatching(builder, pets);
    }
}