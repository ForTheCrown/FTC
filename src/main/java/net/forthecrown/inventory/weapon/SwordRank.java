package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.inventory.weapon.upgrades.WeaponUpgrade;
import net.forthecrown.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class SwordRank {
    // --- INSTANCE FIELDS ---

    /** The Rank number shown to viewers, 1 indexed */
    private final int viewerRank;

    /** The rank used behind the scenes, 0 indexed */
    private final int index;

    /** List of upgrades triggered when this rank is reached */
    private final ObjectList<WeaponUpgrade> upgrades;

    /** Map of name 2 goals that are required to pass in order to move on to the next rank */
    private final Object2ObjectMap<String, WeaponGoal> goals;

    /** The next rank, null, if this is the final rank */
    SwordRank next;

    /** The previous rank, null, if this is the first rank */
    SwordRank previous;

    // --- CONSTRUCTOR ---

    SwordRank(Builder builder) {
        this.viewerRank = builder.getRank();
        this.index = viewerRank - 1;

        this.upgrades = ObjectLists.unmodifiable(builder.getUpgrades());
        this.goals = Object2ObjectMaps.unmodifiable(builder.getGoals());
    }

    // --- BUILDER STATIC CONSTRUCTOR ---

    public static Builder builder(int rank) {
        return new Builder(rank);
    }

    // --- METHODS ---

    /**
     * Tests if this rank has any flavor text
     * to show in the sword's lore
     * @return True, if this rank has flavor text, false otherwise
     */
    public boolean hasFlavorText() {
        for (var u: upgrades) {
            var fluff = u.getFlavorText();

            if (fluff != null && fluff.length > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Applies all this rank's upgrades to the given
     * sword, item and meta
     * @param sword The sword the upgrades are being applied to
     * @param item The sword's item
     * @param meta The sword's meta
     */
    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta) {
        for (var u: upgrades) {
            u.apply(sword, item, meta);
        }
    }

    public void writePreview(TextWriter writer) {
        if (upgrades.isEmpty()) {
            return;
        }

        writer.newLine();
        writer.newLine();

        if (upgrades.size() == 1) {
            var upgrade = upgrades.get(0);

            writer.write("Next upgrade: ", NamedTextColor.GRAY);
            writer.write(upgrade.loreDisplay().color(NamedTextColor.GRAY));

            return;
        }

        writer.write("Next rank: ", NamedTextColor.GRAY);
        TextWriter uWriter = writer.withPrefix(Component.text("• ", NamedTextColor.GRAY));

        for (var u: upgrades) {
            uWriter.line(u.loreDisplay().color(NamedTextColor.GRAY));
        }
    }

    public void writeFlavor(TextWriter writers) {
        for (var u: upgrades) {
            var flavor = u.getFlavorText();

            if (flavor == null || flavor.length == 0) {
                continue;
            }

            for (var t: flavor) {
                writers.line(t);
            }
        }
    }

    public boolean hasStatusDisplay() {
        for (var u: upgrades) {
            if (u.statusDisplay() != null) {
                return true;
            }
        }

        return false;
    }

    public void writeStatus(TextWriter writer) {
        writer.newLine();
        writer.newLine();

        writer.write(Component.translatable("item.modifiers.mainhand",
                NamedTextColor.GRAY
        ));

        for (var u: upgrades) {
            Component[] status = u.statusDisplay();

            if (status == null || status.length == 0) {
                continue;
            }

            for (var t: status) {
                writer.line(t.color(NamedTextColor.GREEN));
            }
        }
    }

    // --- SUB CLASS ---

    @Getter
    @RequiredArgsConstructor
    public static class Builder {
        private final int rank;

        private final ObjectList<WeaponUpgrade> upgrades = new ObjectArrayList<>();
        private final Object2ObjectMap<String, WeaponGoal> goals = new Object2ObjectOpenHashMap<>();

        public Builder addGoal(WeaponGoal goal) {
            var difGoal = goals.put(goal.getName(), goal);

            if (difGoal != null) {
                throw new IllegalStateException(
                        String.format("Goal named '%s' was already added!", goal.getName())
                );
            }

            return this;
        }

        public Builder addUpgrade(WeaponUpgrade upgrade) {
            upgrades.add(upgrade);
            return this;
        }

        public void register(Builder[] ranks) {
            ranks[rank - 1] = this;
        }
    }
}