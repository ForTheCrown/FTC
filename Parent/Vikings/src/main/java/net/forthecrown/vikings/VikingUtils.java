package net.forthecrown.vikings;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.utils.CrownBoundingBox;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import org.bukkit.Location;

public final class VikingUtils {

    public static CrownBoundingBox parseRegion(StringReader reader, CommandSource source) throws CommandSyntaxException {
        PositionArgument posArg = PositionArgument.position();

        Location pos1 = posArg.parse(reader).getBlockLocation(source);
        reader.expect(' ');
        Location pos2 = posArg.parse(reader).getBlockLocation(source);

        return CrownBoundingBox.of(pos1, pos2);
    }

}
