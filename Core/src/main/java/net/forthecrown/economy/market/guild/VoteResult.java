package net.forthecrown.economy.market.guild;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;

public enum VoteResult implements JsonSerializable {
    TIE (false),
    TIE_WITH_ABSTENTIONS (false),

    WIN (true),
    WIN_WITH_ABSTENTIONS (true),

    LOSE (false),
    LOSE_WITH_ABSTENTIONS (false);

    private final boolean win;

    VoteResult(boolean win) {
        this.win = win;
    }

    public boolean isWin() {
        return win;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
