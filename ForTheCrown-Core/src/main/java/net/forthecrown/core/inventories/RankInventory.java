package net.forthecrown.core.inventories;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RankInventory {

    private final CrownUser user;
    public RankInventory(CrownUser user) {
        this.user = user;
    }

    public Inventory getUsersRankGUI(){
        switch (user.getBranch()){
            case PIRATES:
                return getPiratesGUI();
            case VIKINGS:
                return getVikingsGUI();
            case ROYALS:
            case DEFAULT:
                return getRoyalsGUI();
            default:
                throw new IllegalStateException("Unexpected value: " + user.getRank().getRankBranch());
        }
    }

    public Inventory getRoyalsGUI(){
        Inventory inv = getBaseInventory(Branch.ROYALS, "Royals");

        inv.setItem(12, getRankItem(Material.MAP, Rank.KNIGHT, "&7Acquired by completing the first three", "&7levels in the Dungeons and giving the", "&7apples to Diego in the shop."));
        inv.setItem(13, getRankItem(Material.PAPER, Rank.BARON, "&7Acquired by paying 500,000 Rhines."));
        inv.setItem(14, getRankItem(Material.PAPER, Rank.BARONESS, "&7Gotten together with the Baron rank."));

        inv.setItem(29, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.LORD, "&7Included in Tier-1 Donator ranks package,", "&7which costs €10.00 in the webstore."));
        inv.setItem(38, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.LADY, "&7Included in Tier-1 Donator ranks package,", "&7which costs €10.00 in the webstore."));

        inv.setItem(31, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.DUKE, "&7Included in Tier-2 Donator ranks package,", "&7which costs €20.00 in the webstore."));
        inv.setItem(40, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.DUCHESS, "&7Included in Tier-2 Donator ranks package,", "&7which costs €20.00 in the webstore."));

        inv.setItem(33, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.PRINCE, "&7Included in Tier-3 Donator ranks package,", "&7which costs €6.00/month in the webstore."));
        inv.setItem(42, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.PRINCESS, "&7Included in Tier-3 Donator ranks package,", "&7which costs €6.00/month in the webstore."));

        inv.setItem(22, getLegendRank());
        return inv;
    }

    public Inventory getPiratesGUI(){
        Inventory inv = getBaseInventory(Branch.PIRATES, "Pirates");

        inv.setItem(13, getLegendRank());

        inv.setItem(21, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.SAILOR, ChatColor.GRAY + "Acquired by gathering 10 pirate points by finding",
                ChatColor.GRAY + "treasures, hunting mob heads or completing",
                ChatColor.GRAY + "some grappling hook challenge levels."));
        inv.setItem(23, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.PIRATE, ChatColor.GRAY + "Acquired by gathering 50 pirate points by finding",
                ChatColor.GRAY + "treasures, hunting mob heads or completing",
                ChatColor.GRAY + "some grappling hook challenge levels."));

        inv.setItem(39, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.CAPTAIN, ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
                ChatColor.GRAY + "which costs €20.00 in the webstore."));
        inv.setItem(41, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.ADMIRAL, ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
                ChatColor.GRAY + "which costs €6.00/month in the webstore."));

        return inv;
    }

    public Inventory getVikingsGUI(){
        Inventory inv = getBaseInventory(Branch.VIKINGS, "Vikings");

        inv.setItem(20, getRankItem(Material.PAPER, Rank.VIKING, ""));
        inv.setItem(21, getRankItem(Material.PAPER, Rank.BERSERKER, ""));

        inv.setItem(23, getRankItem(Material.PAPER, Rank.WARRIOR, ""));
        inv.setItem(24, getRankItem(Material.PAPER, Rank.SHIELD_MAIDEN, ""));

        inv.setItem(39, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.HERSIR, ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
                ChatColor.GRAY + "which costs €20.00 in the webstore."));
        inv.setItem(41, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.JARL, ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
                ChatColor.GRAY + "which costs €6.00/month in the webstore."));

        inv.setItem(13, getLegendRank());

        return inv;
    }

    private Inventory getBaseInventory(Branch branch, String title){
        CustomInventoryHolder holder = new CustomInventoryHolder(CrownUtils.translateHexCodes(title), 54);
        Inventory inventory = holder.getInventory();

        inventory.setItem(8, CrownUtils.makeItem(Material.PAPER, 1, true, "&eNext page >"));
        inventory.setItem(10, getRankItem(Material.MAP, Rank.DEFAULT, "This is the default rank!"));

        //border making
        ItemStack borderItem;
        switch (branch){
            case ROYALS:
                borderItem = CrownUtils.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, true, " ");
                inventory.setItem(4, CrownUtils.makeItem(Material.IRON_SWORD, 1, true, ChatColor.AQUA + "Royals", "&7The Crown's most loyal knights and nobles!"));
                break;

            case PIRATES:
                borderItem = CrownUtils.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, " ");
                inventory.setItem(4, CrownUtils.makeItem(Material.OAK_BOAT, 1, true, ChatColor.AQUA + "Pirates", "&7Sailors who bow to no crown!"));
                break;

            case VIKINGS:
                borderItem = CrownUtils.makeItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, true, " ");
                inventory.setItem(4, CrownUtils.makeItem(Material.IRON_AXE, 1, true, ChatColor.AQUA + "Vikings", "&7Warriors from the north with an", "uncontrollable lust for blood"));
                break;

            default:
                inventory.setItem(4, CrownUtils.makeItem(Material.IRON_SWORD, 1, true, ChatColor.AQUA + "Royals", "&7The Crown's most loyal knights and nobles!"));
                borderItem = CrownUtils.makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, true, " ");
                break;
        }

        for (int i = 0; i < inventory.getSize(); i++){
            if(i == 4) continue;
            if(i == 8) continue;
            if(i == 10 || i == 19 || i == 28 || i == 37) i += 7;
            inventory.setItem(i, borderItem);
        }
        return inventory;
    }

    private ItemStack getLegendRank(){
        if(user.hasRank(Rank.LEGEND) || user.getPlayer().hasPermission("ftc.legend")) return getRankItem(Material.MAP, Rank.LEGEND, "The rarest rank on all of FTC");
        return null;
    }

    private ItemStack getRankItem(Material mat, Rank rank, String... description) {
        String s;
        if (rank == Rank.DEFAULT) s = "Default";
        else s = rank.getPrefix();

        ItemStack item = CrownUtils.makeItem(mat, 1, true, s, description);
        if (Rank.getFreeRanks().contains(rank)) {
            if (user.getAvailableRanks().contains(rank)) item.setType(Material.MAP);
            else item.setType(Material.PAPER);
        }
        if (rank == user.getRank()) item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        return item;
    }
}