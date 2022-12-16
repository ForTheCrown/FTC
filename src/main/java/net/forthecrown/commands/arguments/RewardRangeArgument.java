package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.holidays.RewardRange;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.arguments.RangeArgument;

public class RewardRangeArgument implements ArgumentType<RewardRange>, VanillaMappedArgument {
    @Override
    public RewardRange parse(StringReader reader) throws CommandSyntaxException {
        MinMaxBounds.Ints ints = RangeArgument.intRange().parse(reader);

        int min = ints.getMin() == null ? 0 : ints.getMin();
        int max = ints.getMax() == null ? Integer.MAX_VALUE : ints.getMax();

        if (min < 100 || max < 100) {
            throw Exceptions.INVALID_REWARD_RANGE;
        }

        return RewardRange.between(min, max);
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return RangeArgument.intRange();
    }
}