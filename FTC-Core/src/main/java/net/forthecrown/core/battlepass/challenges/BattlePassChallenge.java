package net.forthecrown.core.battlepass.challenges;

import com.google.gson.JsonElement;
import net.forthecrown.core.Keys;
import net.forthecrown.core.battlepass.BattlePass;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

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
        this.key = Keys.forthecrown(
                name.toLowerCase()
                        .replaceAll(" ", "_")
                        .replaceAll(",", "_")
        );
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

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

    public Component displayName() {
        TextComponent.Builder builder = Component.text()
                .append(name().color(NamedTextColor.GOLD));

        for (Component c: getDescription()) {
            builder
                    .append(Component.newline())
                    .append(c);
        }

        return name().hoverEvent(builder.build());
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

    public void onReset() {}
}
