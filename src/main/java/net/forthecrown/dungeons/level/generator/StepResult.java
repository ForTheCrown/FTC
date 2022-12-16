package net.forthecrown.dungeons.level.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.level.DungeonRoom;

import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class StepResult {
    /* ----------------------------- CONSTANTS ------------------------------ */
    /** Result code indicating that the result is a success */
    public static final byte SUCCESS = 0;

    /** Result code indicating the generation step encountered a fatal failure */
    public static final byte FAILED = 1;

    /** Generation step reached or exceeded the maximum dungeon depth */
    public static final byte MAX_DEPTH = 2;

    /**
     * Section reached or exceeded the maximum section depth,
     * aka the maximum depth of a specific kind of section, for
     * example: a hallway depth limit
     */
    public static final byte MAX_SECTION_DEPTH = 3;

    /* -------------------------- INSTANCE FIELDS --------------------------- */

    /** The created child sections */
    final List<PieceGenerator> sections;

    /** The generated room */
    final DungeonRoom room;

    /**
     * numerical result value because I was too lazy
     * to create another enum... and because a single
     * byte takes up way less space than an entire
     * enum class along with UTF-8 encoded constant
     * names, integer ordinals and so on
     *
     * @see #SUCCESS
     * @see #FAILED
     * @see #MAX_DEPTH
     * @see #MAX_SECTION_DEPTH
     */
    final byte resultCode;

    /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

    /**
     * Creates a successful result
     * @param sections Newly created child sections
     *                 leading off into further rooms
     * @param room The generated room
     * @return The created result
     */
    public static StepResult success(List<PieceGenerator> sections,
                                     DungeonRoom room
    ) {
        return new StepResult(sections, room, SUCCESS);
    }

    /**
     * Creates a failed result
     * @param code The failure code
     * @return The created result
     * @see #FAILED
     * @see #MAX_DEPTH
     * @see #MAX_SECTION_DEPTH
     */
    public static StepResult failure(byte code) {
        return new StepResult(Collections.emptyList(), null, code);
    }

    /* ------------------------------ METHODS ------------------------------- */

    public boolean failed() {
        return resultCode != SUCCESS;
    }
}