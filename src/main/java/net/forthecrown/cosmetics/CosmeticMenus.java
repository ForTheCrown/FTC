package net.forthecrown.cosmetics;

import dev.geco.gsit.api.GSitAPI;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import static net.forthecrown.utils.text.Text.nonItalic;

public final class CosmeticMenus {
    private CosmeticMenus() {}

    public static final MenuNode HEADER = MenuNode.builder()
            .setItem(user -> {
                return ItemStacks.builder(Material.NETHER_STAR, 1)
                        .setName("&eMenu")
                        .addLoreRaw(Component.empty())

                        .addLoreRaw(
                                Text.format("You have &6{0, gems}&r.",
                                        nonItalic(NamedTextColor.GRAY),
                                        user.getGems()
                                )
                        )

                        .build();
            })
            .build();

    public static final Menu MAIN = baseInventory(54, Component.text("Cosmetics"), false)
            .add(49, ridingToggleOption())
            .add(4, HEADER)

            .add(4, 2,
                    pageButton(Cosmetics.LOGIN,
                            Material.GLOWSTONE_DUST,
                            "Login/Leave Decorations",
                            "Spice up your join/leave messages with",
                            "a little showing off"
                    )
            )

            .add(2, 2, pageButton(
                    Cosmetics.ARROWS,
                    Material.BOW,
                    "Arrow Particle Trails",
                    "Upgrade your arrows with fancy particle",
                    "trails as they fly through the air!"
            ))

            .add(3, 3, pageButton(
                    Cosmetics.EMOTE,
                    Material.TOTEM_OF_UNDYING,
                    "Emotes",
                    "Poking, smooching, bonking and more",
                    "to interact with friends!"
            ))

            .add(5, 3, pageButton(
                    Cosmetics.TRAVEL,
                    Material.FEATHER,
                    "Region Pole Effects",
                    "Spice up your travels with some effects :D"
            ))

            .add(6, 2, pageButton(
                    Cosmetics.DEATH,
                    Material.SKELETON_SKULL,
                    "Death Particles",
                    "Make your death more spectacular by",
                    "exploding into pretty particles!"
            ))

            .build();

    public static final MenuNode GO_BACK = MenuNode.builder()
            .setItem(
                    ItemStacks.builder(Material.PAPER, 1)
                            .setName("&e< Go back")
                            .addLore("&7Back to the main menu")
                            .build()
            )
            .setRunnable((user, click) -> MAIN.open(user))
            .build();

    public static MenuBuilder baseInventory(int size, Component title, boolean goBack) {
        MenuBuilder builder = Menus.builder(size, title)
                .addBorder();

        if (goBack) {
            builder.add(4, GO_BACK);
        }

        return builder;
    }

    private static MenuNode pageButton(CosmeticType type, Material material, String name, String... desc) {
        return MenuNode.builder()
                .setItem(user -> {
                    var builder = ItemStacks.builder(material)
                            .setNameRaw(
                                    Component.text(name, nonItalic(NamedTextColor.GOLD))
                            )
                            .addLoreRaw(Component.empty());

                    for (var s: desc) {
                        builder.addLoreRaw(
                                Component.text(s, nonItalic(NamedTextColor.GRAY))
                        );
                    }

                    return builder.build();
                })

                .setRunnable((user, context) -> type.getMenu().open(user))
                .build();
    }

    public static MenuNode createUnSelectNode(CosmeticType<?> type) {
        return MenuNode.builder()
                .setRunnable((user, context, click) -> {
                    click.shouldReloadMenu(true);
                    user.getCosmeticData().set(type, null);
                })

                .setItem(user -> {
                    var builder = ItemStacks.builder(Material.BARRIER)
                            .setName("&6No effect")
                            .addLore("&7Click to go back to default");

                    if (user.getCosmeticData().isUnset(type)) {
                        builder
                                .addEnchant(Enchantment.CHANNELING, 1)
                                .setFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    return builder.build();
                })

                .build();
    }

    public static MenuNode ridingToggleOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    boolean allows = GSitAPI.canPlayerSit(user.getPlayer());

                    var builder = ItemStacks.builder(allows ? Material.SADDLE : Material.BARRIER)
                            .addLoreRaw(Component.empty())
                            .addLore("&7Right-click someone to jump on top of them.")
                            .addLore("&7Shift-right-click someone to kick them off.")
                            .addLoreRaw(Component.empty());

                    if (allows) {
                        builder
                                .setName("&eYou can ride other players!")
                                .addLore("&7Click to disabled this feature.");
                    } else {
                        builder
                                .setName("&eYou've disabled riding other players!")
                                .addLore("&7Click to enable this feature.");
                    }

                    return builder.build();
                })

                .setRunnable((user, context) -> {
                    context.shouldReloadMenu(true);
                    boolean allows = GSitAPI.canPlayerSit(user.getPlayer());
                    GSitAPI.setCanPlayerSit(user.getPlayer(), !allows);
                })

                .build();
    }
}