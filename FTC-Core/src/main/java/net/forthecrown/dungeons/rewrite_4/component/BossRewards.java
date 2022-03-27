package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.Collection;

public abstract class BossRewards extends BossComponent implements BossStatusListener {
    @Override
    public void onBossSummon(BossContext context) {}

    @Override
    public void onBossDeath(BossContext context, boolean forced) {
        if(forced) return;

        giveRewards(context.players());
    }

    public void giveRewards(Collection<Player> players) {
        UserManager manager = Crown.getUserManager();

        for (Player p: players) {
            if(!getBoss().getRoom().contains(p)) continue;
            if(manager.isAltForAny(p.getUniqueId(), players)) continue;

            CrownUser user = UserManager.getUser(p);

            /*Challenges.BEAT_4_DUNGEON_BOSSES.trigger(user, getBoss());
            Bosses.ACCESSOR.setStatus(user.getDataContainer(), getBoss(), true);*/

            customRewards(p);
            awardAdvancement(p);
        }
    }

    private Key bossKey() {
        return getBoss().getType().key();
    }

    public NamespacedKey advancementKey() {
        return Keys.key(bossKey().namespace(), "dungeons/" + bossKey().value());
    }

    public void awardAdvancement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(advancementKey());

        if(advancement == null) {
            Crown.logger().warn("{} boss has no advancement", bossKey().asString());
            return;
        }

        FtcUtils.grantAdvancement(advancement, player);
    }

    protected abstract void customRewards(Player player);

    public static class BossItemsReward extends BossRewards {
        private final BossItems bossItems;

        public BossItemsReward(BossItems bossItems) {
            this.bossItems = bossItems;
        }

        @Override
        protected void customRewards(Player player) {
            DungeonUtils.giveOrDropItem(player.getInventory(), player.getLocation(), bossItems.item());
        }
    }
}