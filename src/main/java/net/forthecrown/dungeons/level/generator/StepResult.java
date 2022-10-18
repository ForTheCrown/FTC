package net.forthecrown.dungeons.level.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.level.DungeonRoom;

import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class StepResult<T extends SectionGenerator<T>> {
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

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** The created child sections */
    final List<T> sections;

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

    /* ----------------------------- STATIC CONSTRUCTORS ------------------------------ */

    public static <T extends SectionGenerator<T>> StepResult<T> success(List<T> sections,
                                                                        DungeonRoom room
    ) {
        return new StepResult<>(sections, room, SUCCESS);
    }

    public static <T extends SectionGenerator<T>> StepResult<T> failure(byte code) {
        return new StepResult<T>(Collections.emptyList(), null, code);
    }

    /* ----------------------------- METHODS ------------------------------ */

    public boolean failed() {
        return resultCode != SUCCESS;
    }
}