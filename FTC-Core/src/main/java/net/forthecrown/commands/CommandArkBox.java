package net.forthecrown.commands;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.UUIDArgument;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.RankTier;
import net.forthecrown.user.RankTitle;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandArkBox extends FtcCommand {
    public static final Map<UUID, ArkBoxInfo> ID_2_DATA = new Object2ObjectOpenHashMap<>();

    public CommandArkBox() {
        super("ArkBox");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /ArkBox
     *
     * Permissions used: ftc.commands.arkbox
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    ArkBoxInfo info = ID_2_DATA.get(user.getUniqueId());

                    if(info == null) throw FtcExceptionProvider.create("No ark box");

                    if(!ItemStacks.isEmpty(info.item)) {
                        user.getInventory().addItem(info.item.clone());
                    }

                    if(info.tier != null) {
                        user.setRankTier(info.tier, true);
                        List<RankTitle> titleList = info.tier.getTitlesForAndBelow();

                        for (RankTitle t: titleList) {
                            if(t.name().contains("LEGACY")) user.addTitle(t);
                        }
                    }

                    ID_2_DATA.remove(user.getUniqueId());

                    user.sendMessage(
                            Component.text("Got ark box :D", NamedTextColor.YELLOW)
                    );
                    return 0;
                })

                .then(literal("filter")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .then(literal("block")
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();
                                    Block block = player.getTargetBlock(5);

                                    if(block == null || !(block.getState() instanceof ShulkerBox box)) throw FtcExceptionProvider.create("You must be looking at a shulker");

                                    FILTER.checkInventory(box.getInventory());

                                    c.getSource().sendAdmin("Filtered targetted shulker");
                                    return 0;
                                })
                        )

                        .then(literal("held_shulker")
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    ItemStack item = user.getInventory().getItemInMainHand();

                                    if(ItemStacks.isEmpty(item)) throw FtcExceptionProvider.mustHoldItem();

                                    FILTER.checkItems(item);

                                    c.getSource().sendAdmin("Filtered held item");
                                    return 0;
                                })
                        )
                )

                .then(literal("load")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .executes(c -> {
                            load();

                            c.getSource().sendAdmin("Loaded ark boxes");
                            return 0;
                        })
                )

                .then(literal("save")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .executes(c -> {
                            save();

                            c.getSource().sendAdmin("Saved ark boxes");
                            return 0;
                        })
                )

                .then(literal("remove")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .then(argument("uuid", UUIDArgument.uuid())
                                .suggests((c, b) -> CompletionProvider.suggestMatching(b, ListUtils.convert(ID_2_DATA.keySet(), UUID::toString)))
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> {
                                    UUID id = UUIDArgument.getUUID(c, "uuid");
                                    boolean removed = ID_2_DATA.remove(id) != null;

                                    if(removed) c.getSource().sendAdmin("Removed ark data of " + id.toString());
                                    else c.getSource().sendAdmin("Found no ark data for " + id.toString());

                                    return 0;
                                })
                        )
                )

                .then(literal("create")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .then(argument("uuid", UUIDArgument.uuid())
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .then(argument("tier", EnumArgument.of(RankTier.class))
                                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                                        .executes(c -> {
                                            CrownUser user = getUserSender(c);
                                            UUID target = UUIDArgument.getUUID(c, "uuid");
                                            RankTier tier = c.getArgument("tier", RankTier.class);
                                            ItemStack item = user.getInventory().getItemInMainHand();

                                            ArkBoxInfo info = new ArkBoxInfo(ItemStacks.isEmpty(item) ? null : item.clone(), tier);
                                            ID_2_DATA.put(target, info);

                                            user.sendMessage("Made ark box");
                                            return 0;
                                        })
                                )
                        )
                );
    }

    public static void save() {
        JsonWrapper json = JsonWrapper.empty();

        for (Map.Entry<UUID, ArkBoxInfo> e: ID_2_DATA.entrySet()) {
            JsonElement element = e.getValue().serialize();

            if(element != null) json.add(e.getKey().toString(), element);
        }

        File f = file();

        if(json.isEmpty()) {
            f.delete();
        } else {
            try {
                if(!f.exists()) f.createNewFile();

                JsonUtils.writeFile(json.getSource(), f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void load() {
        ID_2_DATA.clear();
        File f = file();

        if(!f.exists()) return;

        try {
            JsonWrapper json = JsonWrapper.of(JsonUtils.readFileObject(f));

            for (Map.Entry<String, JsonElement> e: json.entrySet()) {
                UUID id = UUID.fromString(e.getKey());
                ArkBoxInfo info = new ArkBoxInfo(e.getValue());

                ID_2_DATA.put(id, info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File file() {
        return new File(Crown.dataFolder(), "legacy_data.json");
    }

    private static final ItemFilter FILTER = new ItemFilter();

    public static class ArkBoxInfo implements Struct, JsonSerializable {
        public final ItemStack item;
        public RankTier tier;

        public ArkBoxInfo(ItemStack item, RankTier tier) {
            this.item = item;
            this.tier = tier;

            if (!ItemStacks.isEmpty(item)) {
                FILTER.checkItems(item);
            }
        }

        public ArkBoxInfo(JsonElement element) {
            JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

            this.item = json.getItem("item");
            this.tier = json.getEnum("tier", RankTier.class);

            if (!ItemStacks.isEmpty(item)) {
                FILTER.checkItems(item);
            }
        }

        @Override
        public JsonElement serialize() {
            JsonWrapper json = JsonWrapper.empty();

            if(!ItemStacks.isEmpty(item)) json.addItem("item", item);
            if(tier != null && tier != RankTier.NONE) json.addEnum("tier", tier);

            return json.nullIfEmpty();
        }
    }

    private static class ItemFilter {
        void checkItems(ItemStack item) {
            ItemMeta meta = item.getItemMeta();
            // legit doe, wtf is this syntax
            if(!(meta instanceof BlockStateMeta blockStateMeta)) return;

            ShulkerBox box = (ShulkerBox) blockStateMeta.getBlockState();
            Inventory inventory = box.getInventory();

            checkInventory(inventory);
        }

        void checkInventory(Inventory inventory) {
            for (ItemStack i: inventory) {
                if(ItemStacks.isEmpty(i)) continue;
                filterItem(i);
            }
        }

        void filterItem(ItemStack item) {
            ItemMeta meta = item.getItemMeta();
            if(!meta.getPersistentDataContainer().has(ItemStacks.GENERIC_ITEM_KEY, PersistentDataType.BYTE)) return;

            for (Enchantment e: meta.getEnchants().keySet()) {
                meta.removeEnchant(e);
            }

            if(meta.hasAttributeModifiers()) {
                for (Map.Entry<Attribute, AttributeModifier> e: meta.getAttributeModifiers().entries()) {
                    meta.removeAttributeModifier(e.getKey(), e.getValue());
                }
            }

            switch (item.getType()) {
                case GOLDEN_HELMET -> filter(RoyalItemFilter.CROWN, meta);
                case NETHERITE_SWORD -> filter(RoyalItemFilter.CUTLASS, meta);
                case GOLDEN_SWORD -> filter(RoyalItemFilter.SWORD, meta);
                default -> {}
            }

            meta.getPersistentDataContainer().remove(ItemStacks.GENERIC_ITEM_KEY);
            item.setItemMeta(meta);
        }

        void filter(RoyalItemFilter filter, ItemMeta meta) {
            meta.displayName(filter.displayName());
            filter.accept(meta);
        }

        private interface RoyalItemFilter {
            RoyalItemFilter
                    SWORD   = new RoyalItemFilter() {
                        @Override
                        public Component displayName() {
                            return Component.text("-")
                                    .color(NamedTextColor.GOLD)
                                    .decoration(TextDecoration.ITALIC, false)

                                    .append(
                                            Component.text("Legacy Royal Sword")
                                                    .style(Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD)
                                                            .decoration(TextDecoration.ITALIC, false)
                                                    )
                                    )
                                    .append(Component.text("-"));
                        }

                        @Override
                        public void accept(ItemMeta meta) {
                        }
                    },
                    CUTLASS = new RoyalItemFilter() {
                        @Override
                        public Component displayName() {
                            return Component.text("-")
                                    .color(TextColor.fromHexString("#917558"))
                                    .decoration(TextDecoration.ITALIC, false)

                                    .append(
                                            Component.text("Legacy Captain's Cutlass")
                                                    .style(Style.style(TextColor.fromHexString("#D1C8BA"), TextDecoration.BOLD)
                                                            .decoration(TextDecoration.ITALIC, false)
                                                    )
                                    )
                                    .append(Component.text("-"));
                        }

                        @Override
                        public void accept(ItemMeta meta) {
                            SWORD.accept(meta);
                        }
                    },
                    CROWN   = new RoyalItemFilter() {
                        @Override
                        public Component displayName() {
                            return Component.text("-[")
                                    .color(NamedTextColor.GOLD)
                                    .decoration(TextDecoration.ITALIC, false)

                                    .append(
                                            Component.text("Legacy Crown")
                                                    .style(Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD)
                                                            .decoration(TextDecoration.ITALIC, false)
                                                    )
                                    )
                                    .append(Component.text("]-"));
                        }

                        @Override
                        public void accept(ItemMeta meta) {
                        }
                    };

            Component displayName();
            void accept(ItemMeta meta);
        }
    }
}