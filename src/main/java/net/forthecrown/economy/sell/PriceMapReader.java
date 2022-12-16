package net.forthecrown.economy.sell;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.utils.inventory.menu.Slot;
import org.bukkit.Material;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses a {@link ItemPriceMap} from a .shop
 * file.
 * <p>
 * Parsing is done by reading the file line by line
 * and using the {@link #SEPARATOR} char as a column
 * separator, The file requires each row to have 6 columns
 * which contain the following data:
 * <pre>
 * Column 1: Contains the name of the primary material
 *           that row's data will be for, eg: 'stone'.
 *
 * Column 2: Contains the item's default price.
 *
 * Column 3: Contains the max amount of money that can be earned from
 *           that item, if the column is left blank, it uses
 *           DEF_MAX_EARNINGS
 *
 * Column 4: Contains the name of the compact material
 *           the row has, eg: 'diamond_block'. If left empty the item
 *           will not have a compact item. If this column is given a
 *           value but the next column is not, this parser will
 *           throw an exception
 *
 * Column 5: Contains the compact scalar, the amount of items from
 *           column 1 it takes make 1 of the compact material, if
 *           left empty the item will not have a compact item. If
 *           this column is given a value but the previous one is
 *           left blank, this parser will throw an exception
 *
 * Column 6: Contains the inventory slot of that item, this can
 *           be either a single index or an inventory position, eg:
 *           '14' or '3, 2' where the first integer is a column (x)
 *           of the position and the second is a row (y)
 * </pre>
 * Each row must begin and end with a {@link #SEPARATOR} character
 * <p>
 * If a line starts with '#' then it's marked as a comment line and
 * ignored by this parser. Spaces do also not matter in the context
 * of this parser, all whitespaces are simply skipped over
 * <p>
 * I should add a 'blank line' is any line which is left empty or
 * is given '-' as a value. Basically an empty column is implicitly
 * empty, but a column with '-' as the value is explicitly empty
 */
public class PriceMapReader {
    /**
     * Column separator character: '|'
     */
    static final char SEPARATOR = '|';

    /**
     * Explicitly empty column value
     */
    static final char EMPTY_VAL = '-';

    static final int DEF_MAX_EARNINGS = 500_000;

    static ItemPriceMap readFile(Path path) {
        var map = new ItemPriceMap();

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            read(map, reader);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return map;
    }

    private static void read(ItemPriceMap map, BufferedReader reader) throws IOException {
        var lines = reader.lines().toList();
        int line = 0;

        for (var s: lines) {
            line++;

            // Trim it, so we don't have any
            // leading spaces or anything
            var trimmed = s.trim();

            // Test if it's a comment line or not,
            // if it is, skip it
            if (trimmed.startsWith("#")) {
                continue;
            }

            try {
                map.add(parseLine(new StringReader(trimmed)));
            } catch (CommandSyntaxException e) {
                throw new IOException(
                        String.format("Error on line %s: '%s'", line, e.getMessage()),
                        e
                );
            }
        }
    }

    private static ItemSellData parseLine(StringReader reader) throws CommandSyntaxException, IOException {
        var builder = ItemSellData.builder();

        // --- Base data ---

        skipSeparator(reader);
        builder.material(parseMaterial(reader, false));

        skipSeparator(reader);
        builder.price(parseInt(reader, null));

        // --- Max earnings ---

        skipSeparator(reader);
        builder.maxEarnings(parseInt(reader, DEF_MAX_EARNINGS));

        // --- Compact data parsing ---

        skipSeparator(reader);
        var compactMat = parseMaterial(reader, true);
        builder.compactMaterial(compactMat);

        skipSeparator(reader);
        var multiplier = parseInt(reader, 0);
        builder.compactMultiplier(multiplier);

        // Ensure that both the multiplier and compact
        // material were given and not just 1 of them
        if ((multiplier == 0 && compactMat != null)
                || (multiplier != 0 && compactMat == null)
        ) {
            throw new IOException(
                    "Either multiplier or compact material was given," +
                            "but not both. Both most be given!"
            );
        }

        // --- Slot parsing ---

        skipSeparator(reader);
        var slot = parseInt(reader, null);

        reader.skipWhitespace();

        // Check if we were given an inventory position instead
        // of a raw inventory slot
        if (reader.peek() == ',') {
            reader.skip();
            reader.skipWhitespace();

            var column = slot;
            var row = parseInt(reader, null);

            builder.inventoryIndex(Slot.toIndex(column, row));
        } else {
            builder.inventoryIndex(slot);
        }

        skipSeparator(reader);
        return builder.build();
    }

    static void skipSeparator(StringReader reader) throws CommandSyntaxException {
        reader.skipWhitespace();
        reader.expect(SEPARATOR);
        reader.skipWhitespace();
    }

    /**
     * Parses a material from the given reader
     * @param reader The reader to parse from
     * @param allowEmpty Whether to allow null return values
     * @return The parsed material, or null, if allowEmpty is set to true
     * @throws IOException If allowEmpty was false and an empty value was found, or if
     *                     read material was an invalid material
     */
    static Material parseMaterial(StringReader reader, boolean allowEmpty) throws IOException {
        if (testEmpty(allowEmpty, reader)) {
            return null;
        }

        var label = reader.readUnquotedString();
        var material = Material.getMaterial(label.toUpperCase());

        if (material == null) {
            throw new IOException("Unknown Material: '" + label + "'");
        }

        return material;
    }

    /**
     * Parses an integer from the given reader
     * @param reader The reader
     * @param def The value to return if the next value
     *            is empty, if this is null, empty values
     *            will not be allowed
     * @return The read integer
     * @throws IOException If there was empty value when it was
     *                     not allowed
     * @throws CommandSyntaxException If the input couldn't be
     *                                parsed as an integer
     */
    static int parseInt(StringReader reader, Integer def) throws IOException, CommandSyntaxException {
        if (testEmpty(def != null, reader)) {
            return def;
        }

        return reader.readInt();
    }

    /**
     * Tests that the given reader does not have
     * an implicitly or explicitly empty value
     * @param allowed Whether to allow empty values or not
     * @param reader The reader to test
     * @return True, if the value was empty, false otherwise
     * @throws IOException If the the allowed argument was false and
     *                     next value was empty
     */
    static boolean testEmpty(boolean allowed, StringReader reader) throws IOException {
        if (reader.peek() == EMPTY_VAL || reader.peek() == SEPARATOR) {
            if (!allowed) {
                throw new IOException("Empty value where not allowed");
            } else {
                return true;
            }
        }

        return false;
    }
}