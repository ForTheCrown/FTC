package net.forthecrown.utils;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import net.forthecrown.user.enums.Faction;
import org.jetbrains.annotations.Nullable;

public class FactionFlag extends Flag<Faction> {

    public FactionFlag(String name) {
        super(name);
    }

    @Override
    public Faction parseInput(FlagContext context) throws InvalidFlagFormat {
        if(context.getUserInput().equalsIgnoreCase("none")) return null;
        try {
            return Faction.valueOf(context.getUserInput().toUpperCase());
        } catch (Exception e){
            throw new InvalidFlagFormat(context.getUserInput() + " is not a valid branch value");
        }
    }

    @Override
    public Faction unmarshal(@Nullable Object o) {
        try {
            return Faction.valueOf(o.toString().toUpperCase());
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public Object marshal(Faction o) {
        try {
            return o.name();
        } catch (Exception e){
            return "none";
        }
    }
}