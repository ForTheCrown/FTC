package net.forthecrown.core.goalbook;

import com.google.gson.JsonElement;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public abstract class Reward implements Keyed, JsonSerializable, Predicate<CrownUser> {
    private final String displayText;
    private final Key key;
    private final int level;
    private final boolean donatorExclusive;
    private final ClickableTextNode claimNode;

    public Reward(String keyVal, String display, int level, boolean donatorExclusive) {
        this.donatorExclusive = donatorExclusive;
        this.displayText = display;
        this.level = level;

        this.key = Keys.forthecrown(level + "_" + (donatorExclusive ? "don_" : "") + keyVal);

        claimNode = new ClickableTextNode(key.asString())
                .setPrompt(user -> {
                    Component initial = Component.text("[")
                            .append(display())
                            .append(Component.text("]"));

                    if(test(user)) {
                        return initial.color(NamedTextColor.YELLOW);
                    } else {
                        return initial.color(NamedTextColor.GRAY);
                    }
                })

                .setExecutor(user -> {
                    if(!test(user)) {
                        throw FtcExceptionProvider.translatable("goalBook.reward.cannotClaim");
                    }

                    award(user);
                });
    }

    @Override
    public boolean test(CrownUser user) {
        if(isDonatorExclusive() && !user.isGoalBookDonator()) return false;

        GoalBook.Progress progress = Crown.getGoalBook().getProgress(user.getUniqueId());
        return progress.availableRewards().contains(this);
    }

    public boolean isDonatorExclusive() {
        return donatorExclusive;
    }

    public String getDisplayText() {
        return displayText;
    }

    public Component display() {
        return Component.text(getDisplayText());
    }

    public ClickableTextNode getClaimingNode() {
        return claimNode;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeKey(key);
    }

    public final void award(CrownUser user) {
        GoalBook.Progress progress = Crown.getGoalBook().getProgress(user.getUniqueId());
        progress.claimedRewards().add(this);

        user.sendMessage(
                Component.translatable("goalBook.reward.got", NamedTextColor.YELLOW,
                        display().color(NamedTextColor.GOLD)
                )
        );

        onClaim(user);
    }

    protected abstract void onClaim(CrownUser user);
}
