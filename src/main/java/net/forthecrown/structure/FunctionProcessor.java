package net.forthecrown.structure;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface for processing function info instances
 * during structure placement.
 * <p>
 * Because functions are meant to be ignored, there's no required
 * return type in function processing, this means you can do anything
 * during function placement. Be aware! This does also mean that
 * any block transformations like rotating, offsetting or pivoting
 * must be done manually by calling {@link StructurePlaceConfig#getTransform()}
 */
@FunctionalInterface
public interface FunctionProcessor {
    /**
     * Processes the given function info
     * @param info The info to process
     * @param config The config
     */
    void process(@NotNull FunctionInfo info, @NotNull StructurePlaceConfig config);
}