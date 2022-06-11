package net.forthecrown.useables.checks;

import net.forthecrown.useables.UsageType;

/**
 * A usage check type
 * @param <T> The type of check instance
 */
public interface UsageCheck<T extends UsageCheckInstance> extends UsageType<T> {}