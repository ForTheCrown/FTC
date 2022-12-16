package net.forthecrown.useables;

import lombok.Getter;
import lombok.Setter;

/**
 * Provides a simple base for implementing the {@link CheckHolder} interface
 */
@Getter @Setter
public abstract class AbstractCheckable implements CheckHolder {
    protected final UsageTypeList<UsageTest> checks = UsageTypeList.newTestList();

    protected boolean silent = false;
}