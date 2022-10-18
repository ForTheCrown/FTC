package net.forthecrown.dungeons.level.post;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostParameter {
    ROT (0F),
    GROWTH (0F);

    private final double defaultValue;
}