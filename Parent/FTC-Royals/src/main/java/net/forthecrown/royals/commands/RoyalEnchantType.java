package net.forthecrown.royals.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.custom.CrownArgType;
import net.forthecrown.royals.enchantments.CrownEnchant;
import net.forthecrown.royals.enchantments.RoyalEnchants;

import java.util.concurrent.CompletableFuture;

public class RoyalEnchantType extends CrownArgType<CrownEnchant> {

    private static final RoyalEnchantType ENCHANT_TYPE = new RoyalEnchantType();

    private RoyalEnchantType(){
        super(obj -> new LiteralMessage("Unknown enchant: " + obj.toString()));
    }

    @Override
    protected CrownEnchant parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();

        switch (name.toLowerCase()){
            case "swim":
            case "dolphin_swimmer":
                return RoyalEnchants.dolphinSwimmer();

            case "aim":
            case "strong_aim":
                return RoyalEnchants.strongAim();

            case "crit":
            case "poison_crit":
                return RoyalEnchants.poisonCrit();

            case "block":
            case "healing_block":
                return RoyalEnchants.healingBlock();

            default:
                throw exception.createWithContext(reader, name);
        }
    }

    public static StringArgumentType enchant(){
        return StringArgumentType.word();
    }

    public static <S> CrownEnchant getEnchant(CommandContext<S> context, String argument) throws CommandSyntaxException {
        return ENCHANT_TYPE.parse(inputToReader(context, argument));
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> c, SuggestionsBuilder builder){
        return CrownCommandBuilder.suggestMatching(builder, "swim", "crit", "aim", "block");
    }
}
