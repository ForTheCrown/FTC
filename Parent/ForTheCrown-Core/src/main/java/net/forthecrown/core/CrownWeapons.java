package net.forthecrown.core;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class CrownWeapons {

    public static boolean isCrownWeapon(ItemStack itemStack){
        if(itemStack == null) return false;
        Material mat = itemStack.getType();
        if(!(mat == Material.GOLDEN_SWORD || mat == Material.NETHERITE_SWORD || mat == Material.IRON_AXE)) return false;
        if(!itemStack.hasItemMeta()) return false;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.has(CrownItems.ITEM_KEY, PersistentDataType.BYTE);
    }

    public static boolean isLegacyWeapon(ItemStack item){
        if(item == null) return false;
        Material mat = item.getType();
        if(!(mat == Material.GOLDEN_SWORD || mat == Material.NETHERITE_SWORD || mat == Material.IRON_AXE)) return false;
        if(!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        if(item.getItemMeta().getPersistentDataContainer().has(CrownItems.ITEM_KEY, PersistentDataType.BYTE)) return false;

        String name = ChatColor.stripColor(ComponentUtils.getString(item.getItemMeta().displayName()));
        boolean either = name.contains("Royal Sword") || name.contains("Captain's Cutlass");
        if(!either) return false;
        boolean result = item.lore().size() > 1;
        if(result) updateFromLegacy(item);
        return result;
    }

    public static void updateFromLegacy(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(CrownItems.ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
    }

    public static CrownWeapon fromItem(ItemStack item){
        if(!isCrownWeapon(item) && isLegacyWeapon(item)) throw new IllegalArgumentException(item.toString() + " Not a crown weapon!");

        return new CrownWeapon(item);
    }

    public static void upgradeLevel(ItemStack item, Player player){
        upgradeLevel(fromItem(item), player);
    }

    public static void upgradeLevel(CrownWeapon weapon, Player player){
        byte rank = (byte) (weapon.rank() + 1);
        if((rank == 5 && !player.hasPermission("ftc.donator")) || rank > 10) return;

        //This is dumb and repetitive, but I couldn't think of a better way of doing this :(
        //Rank II -> case 3. aka gets you the Rank III requirements not the Rank II requirements
        //Found that out too late, and couldn't be arsed to change the cases
        switch (weapon.rank()+1){
            case 1:
                weapon.setNewGoal(EntityType.ZOMBIE, (short) 1000);
                break;
            case 2:
                weapon.setNewGoal(EntityType.SKELETON, (short) 1000);
                break;
            case 3:
                weapon.setNewGoal(EntityType.SNOWMAN, (short) 100);
                break;
            case 4:
                weapon.setNewGoal(EntityType.CREEPER, (short) 1000);
                break;
            case 5:
                weapon.setNewGoal(EntityType.BLAZE, (short) 1000);
                player.sendMessage(Component.text("Looting IV was added to your Sword.").color(NamedTextColor.GRAY));
                weapon.item().addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 4);
                break;
            case 6:
                weapon.setNewGoal(EntityType.ENDERMAN, (short) 1000);
                break;
            case 7:
                weapon.setNewGoal(EntityType.GHAST, (short) 50);
                break;
            case 8:
                weapon.setNewGoal(EntityType.AREA_EFFECT_CLOUD, (short) 10);
                break;
            case 9:
                weapon.setNewGoal(EntityType.WITHER, (short) 3);
                break;
            case 10:
                weapon.item().addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 5);
                player.sendMessage(Component.text("Looting V was added to your Sword.").color(NamedTextColor.GRAY));
                weapon.setNewGoal(null, (short) -1);
                return;
        }
        //Effects
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
        for (int i = 0; i <= 5; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), () -> player.getWorld().spawnParticle(Particle.TOTEM, (
                    player).getLocation().getX(),
                    player.getLocation().getY()+2,
                    player.getLocation().getZ(),
                    30, 0.2d, 0.1d, 0.2d, 0.275d),
                    i*5L);
        }
        Component message = Component.text()
                .append(FtcCore.prefix())
                .append(Component.text("Your sword was upgraded to Rank " + CrownUtils.arabicToRoman(rank) + "!")
                        .color(NamedTextColor.GRAY))
                .build();
        player.sendMessage(message);
    }

    public static class CrownWeapon{

        private final ItemStack item;
        private short goal;
        private short progress;
        private byte rank;
        private EntityType type;

        private CrownWeapon(ItemStack item){
            this.item = item;
            List<Component> lore = Objects.requireNonNull(item.lore());
            Byte[] loreLines = rankAndGoalLine(lore);
            rank = (byte) CrownUtils.romanToArabic(PlainComponentSerializer.plain().serialize(lore.get(loreLines[0])).replaceAll("Rank ", ""));

            Component lore5 = item.lore().get(loreLines[1]);
            String parseFrom = PlainComponentSerializer.plain().serialize(lore5).toLowerCase();

            if(parseFrom.contains("max rank")){
                goal = -1;
                progress = 0;
                type = null;
                rank = 10;
                return;
            }

            int spaceIndex = parseFrom.indexOf(" ")+1;
            progress = Short.parseShort(parseFrom.substring(0, parseFrom.indexOf("/")).trim());
            goal = Short.parseShort(parseFrom.substring(parseFrom.indexOf("/")+1, spaceIndex).trim());

            String mob = parseFrom.substring(spaceIndex, parseFrom.indexOf(" ", spaceIndex+1)-1).toUpperCase().trim();

            if(mob.contains("CHARGE")) type = EntityType.AREA_EFFECT_CLOUD;
            else type = EntityType.valueOf(mob);
        }

        //first is rank, second is killed line
        private static Byte[] rankAndGoalLine(List<Component> lore){
            Byte[] result = new Byte[2];

            for (int i = 0; i < lore.size(); i++){
                String s = PlainComponentSerializer.plain().serialize(lore.get(i)).toLowerCase();

                if(s.contains("rank ") && !s.contains("donators") && !s.contains("up")) result[0] = (byte) i;
                if(s.contains("max rank.") || s.contains("to rank up")) result[1] = (byte) i;
            }

            return result;
        }

        public void update(){
            String lore5Text = type == null ? "Max rank." : CrownUtils.normalEnum(type) + "s to rank up!";
            String lore5GoalText = goal == -1 ? "" : (progress + "/" + goal + " ");
            Component lore5 = Component.text(lore5GoalText + (type == EntityType.AREA_EFFECT_CLOUD ? "Charged Creeper's to rank up!" : lore5Text))
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            Component lore0 = Component.text("Rank " + CrownUtils.arabicToRoman(rank))
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

            List<Component> lores = item.lore();
            lores.set(5, lore5);
            lores.set(0, lore0);
            item.lore(lores);
        }

        public ItemStack item() {
            return item;
        }

        public EntityType getTarget(){
            return type;
        }

        public void setTarget(EntityType type) {
            this.type = type;
        }

        public short getGoal(){
            return goal;
        }

        public void setGoal(short goal){
            this.goal = goal;
            update();
        }

        public short getProgress(){
            return progress;
        }

        public void setProgress(short progress){
            this.progress = progress;
            update();
        }

        public void setNewGoal(EntityType mob, short goal, byte rank, boolean resetProgress){
            this.goal = goal;
            this.type = mob;
            this.rank = rank;
            progress = resetProgress ? 0 : progress;
            update();
        }

        public void setNewGoal(EntityType mob, short goal){
            setNewGoal(mob, goal, (byte) (rank() +1), true);
        }

        public void setRank(byte rank){
            this.rank = rank;
            update();
        }

        public byte rank() {
            return rank;
        }
    }
}
