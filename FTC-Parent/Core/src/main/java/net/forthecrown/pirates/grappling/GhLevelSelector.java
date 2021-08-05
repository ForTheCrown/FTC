package net.forthecrown.pirates.grappling;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InvCords;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.inventory.builder.options.InventoryRunnable;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class GhLevelSelector {

    public static final BuiltInventory CONFIRM_RESET = new InventoryBuilder(27, Component.text("Reset progress?"))
            .add(new InventoryBorder())
            .add(3, 1,
                    new ItemStackBuilder(Material.GREEN_STAINED_GLASS_PANE, 1)
                            .setName(Component.text("[Confirm]").style(FtcFormatter.nonItalic(NamedTextColor.GREEN)))
                            .build()
                    , resetConfirm()
            )
            .add(5, 1,
                    new ItemStackBuilder(Material.RED_STAINED_GLASS_PANE, 1)
                            .setName(Component.text("[Deny]").style(FtcFormatter.nonItalic(NamedTextColor.RED)))
                            .build()
                    , resetDeny()
            )
            .build();

    public static BuiltInventory SELECTOR_MENU = createInv();

    public static void recreateSelector() {
        SELECTOR_MENU = createInv();
    }

    private static BuiltInventory createInv() {
        return new InventoryBuilder(54, Component.text("Grappling Hook levels"))
                .addAll(listLevels())
                .add(4, 5,
                        new ItemStackBuilder(Material.BARRIER)
                                .setName(Component.text("Reset progress").style(FtcFormatter.nonItalic(NamedTextColor.RED)))
                                .addLore(Component.text("Reset all your progress to get the rewards again").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                                .build()
                        , (user, context) -> CONFIRM_RESET.open(user)
                )
                .build();
    }

    private static List<LevelOption> listLevels() {
        List<LevelOption> options = new ArrayList<>();

        List<GhLevelData> datas = Pirates.getParkour().getOrderedList();

        for (int i = 0; i < datas.size(); i++) {
            options.add(new LevelOption(datas.get(i), i));
        }

        return options;
    }

    private static InventoryRunnable resetConfirm() {
        return  (user, context) -> {
            Pirates.getParkour().resetProgress(user.getUniqueId());

            user.sendMessage(Component.translatable("gh.progressReset", NamedTextColor.YELLOW));
            SELECTOR_MENU.open(user);
        };
    }

    private static InventoryRunnable resetDeny() {
        return (user, context) -> SELECTOR_MENU.open(user);
    }

    private static class LevelOption implements CordedInventoryOption {

        private final String levelName;
        private final InvCords cords;

        private LevelOption(GhLevelData data, int slot) {
            this.levelName = data.getName();
            this.cords = InvCords.fromSlot(slot);
        }

        public String getLevelName() {
            return levelName;
        }

        public GhLevelData getLevel() {
            GhLevelData data = Pirates.getParkour().byName(levelName);

            if(data == null) {
                ForTheCrown.logger().warning("Null data for InvOption with name " + levelName);
                return null;
            }

            return data;
        }

        @Override
        public void place(Inventory inventory, CrownUser user) {
            GhLevelData data = getLevel();
            if(data == null) return;

            inventory.setItem(getSlot(), data.makeItem(user.getUniqueId()));
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
            if(!user.getPlayer().getInventory().isEmpty()) throw FtcExceptionProvider.translatable("commands.invMustBeEmpty", NamedTextColor.GRAY);

            GhLevelData data = getLevel();
            if(data == null) return;

            data.enter(user.getPlayer(), user.getWorld());
            user.sendMessage(Component.translatable("gh.leave",
                    NamedTextColor.GRAY,
                    Component.text("/leave").color(NamedTextColor.YELLOW))
            );
        }

        @Override
        public InvCords getCoordinates() {
            return cords;
        }
    }
}
