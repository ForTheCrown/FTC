package net.forthecrown.economy.pirates.merchants;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.forthecrown.core.Crown;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.economy.pirates.BlackMarketUtils;
import net.forthecrown.events.dynamic.BmParrotListener;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Pet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParrotMerchant implements UsablePirateNpc {
    public static final Key KEY = Squire.createPiratesKey("parrots");

    private static final ItemStack GRAY = makeHead(Pet.GRAY_PARROT);
    private static final ItemStack GREEN = makeHead(Pet.GREEN_PARROT);
    private static final ItemStack BLUE = makeHead(Pet.BLUE_PARROT);
    private static final ItemStack RED = makeHead(Pet.RED_PARROT);
    private static final ItemStack LIGHT_BLUE = makeHead(Pet.AQUA_PARROT);

    public Inventory createInventory(CrownUser user){
        Inventory result = BlackMarketUtils.getBaseInventory("Parrot Shop", BlackMarketUtils.borderItem());

        result.setItem(11, ifHasPet(user, Pet.GRAY_PARROT, gray()));
        result.setItem(12, ifHasPet(user, Pet.GREEN_PARROT, green()));
        result.setItem(13, ifHasPet(user, Pet.BLUE_PARROT, blue()));
        result.setItem(14, ifHasPet(user, Pet.RED_PARROT, red()));
        result.setItem(15, ifHasPet(user, Pet.AQUA_PARROT, aqua()));

        return result;
    }

    private ItemStack ifHasPet(CrownUser user, Pet pet, ItemStack item){
        if(user.hasPet(pet)){
            ItemMeta meta = item.getItemMeta();
            List<Component> lores = meta.lore();

            lores.add(Component.text("Already owned").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));;
            meta.lore(lores);

            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack makeHead(Pet pet){
        ItemStack result = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) result.getItemMeta();

        PlayerProfile profile = new CraftPlayerProfile(UUID.randomUUID(), pet.name().toLowerCase() + "_head");
        profile.setProperty(new ProfileProperty("textures", pet.getTexture()));
        meta.setPlayerProfile(profile);

        List<Component> lore = new ArrayList<>();
        lore.add(pet.requirementDisplay().decoration(TextDecoration.ITALIC, false));
        lore.add(
                Component.text("Use /parrot " + pet.name().toLowerCase().replaceAll("_parrot", "") + " to summon it.")
                        .style(Style.style(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))
        );
        meta.lore(lore);
        meta.displayName(pet.getName().decoration(TextDecoration.ITALIC, false));

        result.setItemMeta(meta);

        NBT nbt = NbtHandler.ofItemTags(result);
        nbt.put("pet", pet.name());

        return NbtHandler.applyTags(result, nbt);
    }

    public static ItemStack gray(){
        return GRAY.clone();
    }

    public static ItemStack green(){
        return GREEN.clone();
    }

    public static ItemStack blue(){
        return BLUE.clone();
    }

    public static ItemStack red(){
        return RED.clone();
    }

    public static ItemStack aqua(){
        return LIGHT_BLUE.clone();
    }

    @Override
    public void onUse(CrownUser user, Entity entity) {
        BmParrotListener listener = new BmParrotListener(user.getPlayer());
        Bukkit.getPluginManager().registerEvents(listener, Crown.inst());

        user.getPlayer().openInventory(createInventory(user));
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
