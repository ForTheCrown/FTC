package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Main;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.events.custom.SwordRankUpEvent;
import net.forthecrown.inventory.RankedItem;
import net.forthecrown.inventory.weapon.abilities.WeaponAbility;
import net.forthecrown.inventory.weapon.click.ClickHistory;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.inventory.weapon.upgrades.MonetaryUpgrade;
import net.forthecrown.inventory.weapon.upgrades.WeaponUpgrade;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.LoreBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

/**
 * A RoyalSword is a special in-game sword, this is the class which
 * represents it... shocking, I know.
 */
public class RoyalSword extends RankedItem {

    //I know it's flavor, not fluff, as in flavor text, but fuck you UwU
    private int lastFluffChange = 1;

    private WeaponAbility ability;
    private CachedUpgrades nextUpgrades;
    CachedUpgrades waitingUpdate;

    private Object2IntMap<WeaponGoal> goalsAndProgress = new Object2IntOpenHashMap<>();

    private CompoundTag extraData;

    private boolean upgradesFixed, moneyRewardsFixed;

    /**
     * Load constructor, loads all needed data from item's NBT
     * @param item The item to load from
     */
    public RoyalSword(ItemStack item) {
        super(item, RoyalWeapons.TAG_KEY);

        load();
    }

    /**
     * Creation constructor, adds the needed data to the item's NBT.
     * @param owner The owner of the item
     * @param item The item itself.
     */
    public RoyalSword(UUID owner, ItemStack item) {
        super(owner, item, RoyalWeapons.TAG_KEY);
        this.extraData = new CompoundTag();
        this.upgradesFixed = true;
        this.moneyRewardsFixed = true;
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        super.readNBT(tag);

        this.extraData = tag.getCompound("extra_data");
        this.nextUpgrades = RoyalWeapons.getUpgrades(rank+1);
        this.lastFluffChange = tag.getInt("lastFluffChange");
        this.upgradesFixed = tag.getBoolean("upgrades_fixed");
        this.moneyRewardsFixed = tag.getBoolean("money_rewards_fixed");

        if(tag.contains("ability")) {
            this.ability = Registries.WEAPON_ABILITIES.get(Keys.parse(tag.getString("ability")));
        }

        if(!tag.contains("goals")) return;

        //No need to null this, if it's null,
        //the tag returns an empty compound
        CompoundTag goalsTag = tag.getCompound("goals");

        for (String s: goalsTag.getAllKeys()) {
            Key k = Keys.parse(s);
            if(k.namespace().equals(Main.OLD_NAMESPACE)) k = Keys.forthecrown(k.value());
            int progress = goalsTag.getInt(s);

            WeaponGoal goal = Registries.WEAPON_GOALS.get(k);

            //If the goal wasn't found, warn console and ignore it
            if(goal == null) {
                Crown.logger().warn("Found unknown goal in RoyalSword. Owned by:" + getOwner() + " Goal: " + s);
                continue;
            }

            goalsAndProgress.put(goal, progress);
        }
    }

    @Override
    protected void onUpdate(ItemStack item, ItemMeta meta, CompoundTag tag) {
        super.onUpdate(item, meta, tag);

        if(!upgradesFixed) {
            upgradesFixed = true;

            RoyalWeapons.getUpgrades(rank).apply(this, item, meta, tag);
            Announcer.debug("Fixed sword upgrades");
        }

        if(!moneyRewardsFixed) {
            CachedUpgrades[] below = RoyalWeapons.getBelow(getRank());

            for (CachedUpgrades cached: below) {
                for (WeaponUpgrade u: cached) {
                    if (!(u instanceof MonetaryUpgrade)) continue;

                    u.apply(this, item, meta, getExtraData());
                }
            }

            moneyRewardsFixed = true;
        }

        tag.putBoolean("upgrades_fixed", true);
        tag.putBoolean("money_rewards_fixed", true);

        tag.putInt("lastFluffChange", lastFluffChange);
        if(ability != null) tag.putString("ability", ability.key().asString());

        //Serialize goals, same as map, goal 2 progress
        if(!goalsAndProgress.isEmpty()) {
            CompoundTag goalsTag = new CompoundTag();
            for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
                goalsTag.putInt(e.getKey().key().asString(), e.getIntValue());
            }

            tag.put("goals", goalsTag);
        }

        //If there's an upgrade waiting to be applied, apply it and then null it.
        if(waitingUpdate != null) {
            waitingUpdate.apply(this, item, meta, getExtraData());

            waitingUpdate = null;
        }

        tag.put("extra_data", extraData);
    }

    @Override
    protected void createLore(LoreBuilder lore) {
        super.createLore(lore);

        //The bearer of this... bla bla
        addFlavorText(lore);

        if(ability != null) {
            lore.add(
                    Component.text("Current ability: ")
                            .append(ability.loreDisplay())
                            .style(nonItalic(NamedTextColor.GRAY))
            );
        }

        //Add goal text, if there are goals to go for
        if(!goalsAndProgress.isEmpty()) {
            //If we've got multiple entries we need to display the lore differently.
            boolean multiEntry = goalsAndProgress.size() > 1;

            //Instead of 1 goal, we have a bullet list if we have several
            if(multiEntry) {
                lore.add(
                        Component.text("Goals:")
                                .style(nonItalic(NamedTextColor.AQUA))
                );
            }

            //For every goal
            for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
                TextComponent.Builder builder = Component.text()
                        .style(nonItalic(NamedTextColor.AQUA));

                //If multiple goals, add bullet
                if(multiEntry) builder.append(Component.text("• "));

                builder
                        .append(
                                Component.text(e.getIntValue() + "/" + e.getKey().getGoal() + " ")
                        )
                        .append(
                                e.getKey().loreDisplay()
                        );

                if(!multiEntry) builder.append(Component.text(" to rank up!"));

                lore.add(builder.build());
            }
        } else {
            if(rank >= RoyalWeapons.MAX_RANK) {
                lore.add(
                        Component.text("Max rank reached")
                                .style(nonItalic(NamedTextColor.DARK_GRAY))
                );
            }
        }

        //If there's something we'll upgrade to once we beat the goal(s),
        //then show the upgrade
        if(nextUpgrades != null) {
            nextUpgrades.addLore(lore);
        }

        //If the owner is not a donator, tell them they could donate :)
        if(hasPlayerOwner() && !getOwnerUser().hasPermission(Permissions.DONATOR_1)) {
            lore.add(
                    Component.text("Donators can upgrade Royal Tools beyond rank " + FtcUtils.arabicToRoman(RoyalWeapons.DONATOR_RANK))
                            .style(nonItalic(NamedTextColor.DARK_GRAY))
            );
        }

        //Could be NIL, because that's used for a generic display version of the sword.
        if(hasPlayerOwner()) {
            lore.add(
                    Component.text("Owner: ")
                            .style(nonItalic(NamedTextColor.DARK_GRAY))
                            .append(getOwnerUser().nickDisplayName())
            );
        }

        // Add current upgrades to the lore
        // let 'em know what their sword has
        CachedUpgrades[] previous = RoyalWeapons.getBelow(getRank() + 1);
        if(previous == null || previous.length < 1) return;

        for (CachedUpgrades u: previous) {
            if(!u.hasStatusDisplay()) continue;

            u.addCurrentLore(lore);
            break;
        }
    }

    private void addFlavorText(LoreBuilder lore) {
        final Component border = Component.text("------------------------------").style(nonItalic(NamedTextColor.DARK_GRAY));

        lore.add(border);

        CachedUpgrades lastChange = RoyalWeapons.getUpgrades(lastFluffChange);
        lastChange.addFluff(lore);

        lore.add(border);
    }

    public double damage(Player killer, EntityDamageByEntityEvent event, ClickHistory history) {
        WeaponUseContext context = new WeaponUseContext(killer, this, (LivingEntity) event.getEntity(), event.getDamage(), event.getFinalDamage(), history);

        // Run ability before goal check, ability can make damage go higher
        if(ability != null) ability.onWeaponUse(context);

        //Test all goals to see if we damaged any matching entities
        for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
            if(!e.getKey().test(context)) continue;

            //If we did, increment goal count by the amount stated in ComVar
            int newVal = e.getIntValue() + e.getKey().getIncrementAmount(context);
            goalsAndProgress.put(e.getKey(), Math.min(e.getKey().getGoal(), newVal));
        }

        //If we should rank up... rank up
        if(shouldRankUp()) {
            incrementRank();

            doEffects(
                    killer,
                    killer.getWorld(),
                    killer.getLocation()
            );
        }

        //Always update item
        update();
        return context.baseDamage;
    }

    public void incrementRank() {
        new SwordRankUpEvent(rank, rank + 1, this).callEvent();

        rank++;

        this.waitingUpdate = nextUpgrades;
        nextUpgrades = RoyalWeapons.getUpgrades(rank + 1);

        if(waitingUpdate != null && waitingUpdate.hasFluff()) lastFluffChange = rank;

        setGoals(RoyalWeapons.getGoalsAtRank(rank));
    }

    private void doEffects(Player player, World w, Location l) {
        //Play sounds
        w.playSound(l, Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
        w.playSound(l, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

        //Spawn totem particles
        for (int i = 0; i <= 5; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    Crown.inst(), () -> w.spawnParticle(
                            Particle.TOTEM, l.getX(), l.getY()+2, l.getZ(),
                            30, 0.2d, 0.1d, 0.2d, 0.275d),
                    i*5L
            );
        }

        //Tell 'em they're a good boy
        player.sendMessage(
                Component.text()
                        .append(Crown.prefix())
                        .append(
                                Component.text("Your sword was upgraded to Rank " + FtcUtils.arabicToRoman(rank) + "!")
                                        .color(NamedTextColor.GRAY)
                        )
                        .build()
        );
    }

    /**
     * Tests to see if the sword should be ranked up
     * @return Whether to rank up the sword.
     */
    public boolean shouldRankUp() {
        if(rank >= RoyalWeapons.MAX_RANK) return false;
        if(goalsAndProgress.isEmpty()) return false;

        for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
            if(e.getIntValue() < e.getKey().getGoal()) return false;
        }

        return true;
    }

    /**
     * Gets the upgrade the sword will receive when it's rank upgrades
     * @return Next rank's upgrade, null, if none
     */
    public CachedUpgrades getNextUpgrades() {
        return nextUpgrades;
    }

    /**
     * Sets the next upgrade
     * @param nextUpgrade New next rank's upgrade
     */
    public void setNextUpgrades(CachedUpgrades nextUpgrade) {
        this.nextUpgrades = nextUpgrade;
    }

    /**
     * Gets a map of goal 2 progress
     * @return Progress map of sword's goals, may be empty if at max level
     */
    public Object2IntMap<WeaponGoal> getGoalsAndProgress() {
        return goalsAndProgress;
    }

    /**
     * Sets the current goals and their progress
     * @param goalsAndProgress The goals and their progress
     */
    public void setGoalsAndProgress(Object2IntMap<WeaponGoal> goalsAndProgress) {
        this.goalsAndProgress = goalsAndProgress;
    }

    /**
     * Sets the goals
     * @param goals The goals to set for the sword
     */
    public void setGoals(List<WeaponGoal> goals) {
        this.goalsAndProgress.clear();
        for (WeaponGoal g: goals) {
            this.goalsAndProgress.put(g, 0);
        }
    }

    public int getLastFluffChange() {
        return lastFluffChange;
    }

    public void setLastFluffChange(int lastFluffChange) {
        this.lastFluffChange = lastFluffChange;
    }

    public WeaponAbility getAbility() {
        return ability;
    }

    public void setAbility(WeaponAbility ability) {
        this.ability = ability;
    }

    public CompoundTag getExtraData() {
        return extraData;
    }
}