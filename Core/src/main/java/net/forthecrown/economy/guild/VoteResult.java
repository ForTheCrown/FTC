package net.forthecrown.economy.guild;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;

public enum VoteResult implements JsonSerializable, Translatable, ComponentLike {
    NO_VOTES(false, "noVotes"),

    TIE (false, "tie"),
    TIE_WITH_ABSTENTIONS (false, "tie"),

    WIN (true, "win"),
    WIN_WITH_ABSTENTIONS (true, "win"),

    LOSE (false, "lose"),
    LOSE_WITH_ABSTENTIONS (false, "lose");

    private final boolean win;
    private final String translationKey;

    VoteResult(boolean win, String suffix) {
        this.win = win;
        this.translationKey = "guilds.voteResult." + suffix;
    }

    public boolean isWin() {
        return win;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }

    @Override
    public @NotNull String translationKey() {
        return translationKey;
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.translatable(this);
    }
}
