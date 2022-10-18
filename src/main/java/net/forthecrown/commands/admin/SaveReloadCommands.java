package net.forthecrown.commands.admin;

import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.holidays.ServerHolidays;
import net.forthecrown.core.resource.ResourceWorldTracker;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.structure.Structures;
import net.forthecrown.text.Text;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import static net.forthecrown.core.Crown.*;

public class SaveReloadCommands extends FtcCommand {
    private static final EnumArgument<Section> SECTION_ARGUMENT = EnumArgument.of(Section.class);

    private final boolean save;

    public SaveReloadCommands(@NotNull String name, boolean save) {
        super(name);

        this.save = save;

        setPermission(Permissions.ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    for (var e: Section.values()) {
                        e.run(save);
                    }

                    c.getSource().sendAdmin(format(save, "FTC-Plugin"));
                    return 0;
                })

                .then(argument("section", SECTION_ARGUMENT)
                        .executes(c -> {
                            var section = c.getArgument("section", Section.class);
                            section.run(save);

                            c.getSource().sendAdmin(format(save, section.getViewerName()));
                            return 0;
                        })
                );
    }

    public static void createCommands() {
        new SaveReloadCommands("ftcsave", true);
        new SaveReloadCommands("ftcreload", false);
    }

    private static Component format(boolean save, String section) {
        return Text.format("{0} {1}",
                save ? "Saved" : "Reloaded",
                section
        );
    }

    @RequiredArgsConstructor
    public enum Section {
        USER_CACHE (
                UserManager.get().getUserLookup()::save,
                UserManager.get().getUserLookup()::reload
        ),

        USER_ALTS (
                UserManager.get().getAlts()::save,
                UserManager.get().getAlts()::reload
        ),

        USER_BALANCES (
                UserManager.get().getBalances()::save,
                UserManager.get().getBalances()::reload
        ),

        USER_VOTES (
                UserManager.get().getVotes()::save,
                UserManager.get().getVotes()::reload
        ),

        USER_GEMS (
                UserManager.get().getGems()::save,
                UserManager.get().getGems()::reload
        ),

        USER_PLAYTIME (
                UserManager.get().getPlayTime()::save,
                UserManager.get().getPlayTime()::reload
        ),

        USERS (
                UserManager.get()::save,
                UserManager.get()::reload
        ),

        KITS (
                Usables.get().getKits()::save,
                Usables.get().getKits()::reload
        ),

        WARPS (
                Usables.get().getWarps()::save,
                Usables.get().getWarps()::reload
        ),

        USABLES (
                Usables.get()::save,
                Usables.get()::reload
        ),

        REGIONS (
                RegionManager.get()::save,
                RegionManager.get()::reload
        ),

        SHOPS (
                getEconomy().getShops()::save,
                getEconomy().getShops()::reload
        ),

        PUNISHMENTS (
                Punishments.get()::save,
                Punishments.get()::reload
        ),

        MARKETS (
                getEconomy().getMarkets()::save,
                getEconomy().getMarkets()::load
        ),

        SELL_SHOP (
                () -> {},
                getEconomy().getSellShop()::load
        ),

        ANNOUNCER (
                getAnnouncer()::save,
                getAnnouncer()::reload
        ),

        HOLIDAYS (
                ServerHolidays.get()::save,
                ServerHolidays.get()::reload
        ),

        STRUCTURES (
                Structures.get()::save,
                Structures.get()::load
        ),

        RW_TRACKER (
                ResourceWorldTracker.get()::save,
                ResourceWorldTracker.get()::reload
        ),

        CONFIG (
                config()::save,
                config()::reload
        );

        private final Runnable onSave, onLoad;

        public String getViewerName() {
            return Text.prettyEnumName(this);
        }

        public void run(boolean save) {
            if (save) {
                onSave.run();
            } else {
                onLoad.run();
            }
        }
    }
}