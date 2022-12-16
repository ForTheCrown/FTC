package net.forthecrown.commands.manager;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.experimental.UtilityClass;
import net.forthecrown.grenadier.CompletionProvider;

/**
 * Utility class for methods relating to {@link StringReader}s
 */
public @UtilityClass class Readers {

    /**
     * Tests if the given reader's remaining input starts
     * with the given string.
     * <p>
     * Note: This method ignores cases
     *
     * @param reader The reader to test
     * @param s The string to test if the reader's starts with
     * @return
     */
    public boolean startsWith(StringReader reader, String s) {
        if (!reader.canRead(s.length())) {
            return false;
        }

        return CompletionProvider.startsWith(s, reader.getRemaining());
    }

    /**
     * Skips the given string in the given reader, if
     * the given reader's remaining input starts with
     * the given string.
     *
     * @see #startsWith(StringReader, String)
     * @param reader The reader to move the cursor of
     * @param s The string to skip
     */
    public void skip(StringReader reader, String s) {
        if (!startsWith(reader, s)) {
            return;
        }

        reader.setCursor(reader.getCursor() + s.length());
    }

    /**
     * Ensures that the given string reader is
     * at the end of its input
     * @param reader The reader to test
     * @throws CommandSyntaxException If the reader is not at the end of it's input
     */
    public void ensureCannotRead(StringReader reader) throws CommandSyntaxException {
        if (reader.canRead()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherUnknownArgument()
                    .createWithContext(reader);
        }
    }
}