package net.forthecrown.mayevent.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.custom.CrownArgType;
import net.forthecrown.mayevent.guns.*;

import java.util.concurrent.CompletableFuture;

public class WeaponArgType extends CrownArgType<HitScanWeapon> {

    public static WeaponArgType WEAPON = new WeaponArgType();

    private WeaponArgType() {
        super(obj -> new LiteralMessage("Unkown gun: " + obj.toString()));
    }

    @Override
    protected HitScanWeapon parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        switch (name.toLowerCase()){
            case "rocketlauncher":
            case "rocket": return new RocketLauncher();

            case "gauss":
            case "GaussCannon": return new GaussCannon();

            case "shotgun":
            case "twelvegauge": return new TwelveGaugeShotgun();

            case "rifle":
            case "standardrifle": return new StandardRifle();

            default:
                reader.setCursor(cursor);
                throw exception.createWithContext(reader, name);
        }
    }

    public static StringArgumentType gun(){
        return StringArgumentType.word();
    }

    public static <S> HitScanWeapon getGun(CommandContext<S> c, String argument) throws CommandSyntaxException {
        return WEAPON.parse(inputToReader(c, argument));
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> c, SuggestionsBuilder builder){
        return CrownCommandBuilder.suggestMatching(builder, "rifle", "shotgun", "gauss", "rocket");
    }
}
