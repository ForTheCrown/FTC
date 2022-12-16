package net.forthecrown.commands.admin;

import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.holidays.ServerHolidays;
import net.forthecrown.core.module.ModuleServices;
import net.forthecrown.core.resource.ResourceWorldTracker;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.economy.Economy;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.log.LogManager;
import net.forthecrown.structure.Structures;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.WaypointManager;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

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
                    if (save) {
                        ModuleServices.SAVE.run();
                    } else {
                        ModuleServices.RELOAD.run();
                    }

                    c.getSource().sendAdmin(format(save, "FTC-Plugin"));
                    return 0;
                })

                .then(argument("section", SECTION_ARGUMENT)
                        .executes(c -> {
                            var section = c.getArgument("section", Section.class);

                            if (save) {
                                if (section.onSave == null) {
                                    throw Exceptions.format(
                                            "{0} cannot be saved!",
                                            section
                                    );
                                }
                            } else if (section.onLoad == null) {
                                throw Exceptions.format(
                                        "{0} cannot be loaded!",
                                        section
                                );
                            }

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
                Usables.getInstance().getKits()::save,
                Usables.getInstance().getKits()::reload
        ),

        WARPS (
                Usables.getInstance().getWarps()::save,
                Usables.getInstance().getWarps()::reload
        ),

        USABLES (
                Usables.getInstance()::save,
                Usables.getInstance()::reload
        ),

        WAYPOINTS (
                WaypointManager.getInstance()::save,
                WaypointManager.getInstance()::reload
        ),

        GUILDS (
                GuildManager.get()::save,
                GuildManager.get()::load
        ),

        SHOPS (
                Economy.get().getShops()::save,
                Economy.get().getShops()::reload
        ),

        PUNISHMENTS (
                Punishments.get()::save,
                Punishments.get()::reload
        ),

        MARKETS (
                Economy.get().getMarkets()::save,
                Economy.get().getMarkets()::load
        ),

        SELL_SHOP (
                () -> {},
                Economy.get().getSellShop()::load
        ),

        ANNOUNCER (
                Announcer.get()::save,
                Announcer.get()::reload
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

        CHALLENGES (
                ChallengeManager.getInstance()::save,
                ChallengeManager.getInstance()::load
        ),

        DATA_LOG (
                LogManager.getInstance()::save,
                LogManager.getInstance()::load
        ),

        SCRIPTS (
                null,
                ScriptManager.getInstance()::load
        ),

        CONFIG (
                ConfigManager.get()::save,
                ConfigManager.get()::load
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