package net.forthecrown.useables;

import lombok.Getter;
import lombok.Setter;

/**
 * Provides a simple base for implementing the {@link CheckHolder} interface
 */
public abstract class AbstractCheckable implements CheckHolder {
    @Getter
    protected final UsageTypeList<UsageTest> checks = UsageTypeList.newTestList();

    @Getter @Setter
    protected boolean silent;
}