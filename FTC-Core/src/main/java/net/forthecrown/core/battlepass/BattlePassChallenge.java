package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class BattlePassChallenge implements Keyed, Nameable, JsonSerializable {
    private final String name;
    private final Key key;
    private final Component[] description;
    private final BattlePass.Category category;
    private final int target;
    private final int exp;
    private boolean enabled;

    public BattlePassChallenge(String name,
                               BattlePass.Category category,
                               int target, int exp,
                               Component... desc
    ) {
        this.name = name;
        this.category = category;
        this.description = desc;
        this.target = target;
        this.exp = exp;
        this.key = Keys.forthecrown(name.toLowerCase().replaceAll(" ", "_"));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final void trigger(UUID uuid, int amount) {
        if(!isEnabled()) return;

        onTrigger(Crown.getBattlePass().getProgress(uuid), amount);
    }

    public final void trigger(UUID uuid) {
        trigger(uuid, 1);
    }

    protected abstract void onTrigger(BattlePass.Progress progress, int amount);

    public int getTarget() {
        return target;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public String getName() {
        return name;
    }

    public BattlePass.Category getCategory() {
        return category;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public Component[] getDescription() {
        return description;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeKey(key);
    }
}
