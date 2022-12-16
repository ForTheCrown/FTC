package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;

/**
 * Provides a simple way of implementing the {@link Usable} interface
 */
public abstract class AbstractUsable extends AbstractCheckable implements Usable {
    @Getter
    protected final UsageTypeList<UsageAction> actions = UsageTypeList.newActionList();

    @Override
    public void save(CompoundTag tag) {
        saveActions(tag);
        saveChecks(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        try {
            loadActions(tag);
            loadChecks(tag);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }
}