package net.forthecrown.core;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BranchFlag extends Flag<Branch> {

    protected BranchFlag(String name) {
        super(name);
    }

    @Override
    public Branch parseInput(FlagContext context) throws InvalidFlagFormat {
        if(context.getUserInput().equalsIgnoreCase("none")) return null;
        try {
            return Branch.valueOf(context.getUserInput().toUpperCase());
        } catch (Exception e){
            throw new InvalidFlagFormat(context.getUserInput() + " is not a valid branch value");
        }
    }

    @Override
    public Branch unmarshal(@Nullable Object o) {
        try {
            return Branch.valueOf(o.toString().toUpperCase());
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public Object marshal(Branch o) {
        try {
            return o.toString().toLowerCase();
        } catch (Exception e){
            return "none";
        }
    }

    public static boolean flagAllows(CrownUser user, BranchFlag flag){
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(user.getPlayer());
        Collection<Branch> collection = WorldGuard.getInstance().getPlatform()
                .getRegionContainer().createQuery()
                .getApplicableRegions(wgPlayer.getLocation()).queryAllValues(wgPlayer, flag);

        for (Branch b: collection){
            if(b == null) continue;
            if(!user.getBranch().equals(b)) return false;
        }
        return true;
    }
}
