package net.forthecrown.dungeons.level;

import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;

public final class Rooms {
    private Rooms() {}

    public static final int
            FLAG_CONNECTOR  = 0x1,
            FLAG_BOSS_ROOM  = 0x2,
            FLAG_STAIRS     = 0x4,
            FLAG_MOB_ROOM   = 0x8,
            FLAG_DEAD_END   = 0x10,
            FLAG_ROOT       = 0x20;

    public static final Registry<RoomType> REGISTRY = PieceTypes.newRegistry("rooms");

    public static final Holder<RoomType>
            // Root
            ROOT          = create("root",          "dungeons/starting_room",   FLAG_ROOT),

            // Connectors
            SHORT_HALLWAY = create("test_hallway",  "dungeons/short_hallway_0", FLAG_CONNECTOR),
            TETRIS_ROOM   = create("test_t_shape",  "dungeons/tetris_block_0",  FLAG_CONNECTOR),
            FOUR_WAY      = create("four_way",      "dungeons/4_way",           FLAG_CONNECTOR),
            L_TURN        = create("l_turn",        "dungeons/l_turn",          FLAG_CONNECTOR),
            STAIRS        = create("stairs",        "dungeons/stairs",          FLAG_CONNECTOR | FLAG_STAIRS),

            // Mob Rooms
            MOB_ROOM_1    = create("mob_room_1",    "dungeons/mob_room_1",      FLAG_MOB_ROOM),
            MOB_ROOM_2    = create("mob_room_2",    "dungeons/esl_room",        FLAG_MOB_ROOM),
            MOB_ROOM_3    = create("mob_room_3",    "dungeons/mob_room_3",      FLAG_MOB_ROOM | FLAG_STAIRS),

            // Bos Rooms
            BOSS_ROOM_1   = create("boss_room_1",   "dungeons/boss_room_1",     FLAG_BOSS_ROOM);

    private static Holder<RoomType> create(String key, String structName, int flags) {
        return REGISTRY.register(key, new RoomType(structName, flags));
    }
}