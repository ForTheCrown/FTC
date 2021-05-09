package net.forthecrown.mayevent.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.mayevent.guns.HitScanWeapon;
import net.forthecrown.mayevent.guns.RocketLauncher;
import net.forthecrown.mayevent.guns.StandardRifle;
import net.forthecrown.mayevent.guns.TwelveGaugeShotgun;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class WeaponArgType implements ArgumentType<HitScanWeapon> {
    public static DynamicCommandExceptionType exception = new DynamicCommandExceptionType(o -> () -> "Unknown gun: " + o);
    public static WeaponArgType WEAPON = new WeaponArgType();
    private WeaponArgType() {}

    @Override
    public HitScanWeapon parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        switch (name.toLowerCase()){
            case "rocketlauncher":
            case "rocket": return new RocketLauncher();

            case "shotgun":
            case "twelvegauge": return new TwelveGaugeShotgun();

            case "rifle":
            case "standardrifle": return new StandardRifle();

            default:
                reader.setCursor(cursor);
                throw exception.createWithContext(reader, name);
        }
    }

    public static WeaponArgType gun(){
        return WEAPON;
    }

    public static <S> HitScanWeapon getGun(CommandContext<S> c, String argument) throws CommandSyntaxException {
        return c.getArgument(argument, HitScanWeapon.class);
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> c, SuggestionsBuilder builder){
        return CommandSource.suggestMatching(builder, Arrays.asList("rifle", "shotgun", "gauss", "rocket"));
    }
}
