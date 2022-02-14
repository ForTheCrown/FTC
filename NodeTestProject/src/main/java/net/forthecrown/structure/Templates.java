package net.forthecrown.structure;

import java.awt.*;

public interface Templates {
        int[][]
                ONE_BLOCK_DATA = {{0, 0}},
                CORRIDOR_DATA = {{0, 0}, {1, 0}, {2, 0}},
                L_LEFT_DATA = {{0, 0}, {0, 1}, {1, 1}},
                FOUR_WAY_DATA = {{1, 0}, {0, 1}, {1, 1}, {2, 1}, {0, 2}},
                THREE_WAY_DATA = {{0, 0}, {1, 0}, {2, 0}, {1, 1}};

    Template
            ONE_BLOCK = Template.of(Color.CYAN, ONE_BLOCK_DATA),
            CORRIDOR = Template.of(Color.BLUE, CORRIDOR_DATA),
            L_LEFT = Template.of(Color.RED, L_LEFT_DATA),
            FOUR_WAY = Template.of(Color.GREEN, FOUR_WAY_DATA),
            THREE_WAY = Template.of(Color.MAGENTA, THREE_WAY_DATA);
}
