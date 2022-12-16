package net.forthecrown.useables;

import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.io.SerializationHelper;

import java.nio.file.Path;

/**
 * A trigger manager which is used to manage all triggers
 * created by commands.
 * <p>
 * It writes all triggers to a global_triggers.dat file in
 * the plugin directory
 */
@RequiredArgsConstructor
public class GlobalTriggerManager extends TriggerManager {
    private final Path path;

    /**
     * Saves all global triggers to a file
     */
    public void saveFile() {
        SerializationHelper.writeTagFile(path, this::save);
    }

    /**
     * Reads all global triggers from the file
     */
    public void readFile() {
        SerializationHelper.readTagFile(path, this::load);
    }
}