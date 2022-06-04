package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.ServerHolidays;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.arguments.RangeArgument;

public class RewardRangeArgument implements ArgumentType<ServerHolidays.RewardRange>, VanillaMappedArgument {
    private static final RewardRangeArgument INSTANCE = new RewardRangeArgument();

    public static RewardRangeArgument range() {
        return INSTANCE;
    }

    @Override
    public ServerHolidays.RewardRange parse(StringReader reader) throws CommandSyntaxException {
        MinMaxBounds.Ints ints = RangeArgument.intRange().parse(reader);

        int min = ints.getMin() == null ? 0 : ints.getMin().intValue();
        int max = ints.getMax() == null ? FtcVars.maxMoneyAmount.get() : ints.getMax().intValue();

        if (min < 100 || max < 100) {
            throw FtcExceptionProvider.create("Max or Min bounds cannot be below 100");
        }

        return ServerHolidays.RewardRange.between(min, max);
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return RangeArgument.intRange();
    }
}