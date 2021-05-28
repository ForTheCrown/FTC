package net.forthecrown.emperor.datafixers;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.economy.BalanceMap;
import net.forthecrown.emperor.economy.SortedBalanceMap;
import net.forthecrown.emperor.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAndBalanceUpdater implements DataFixer<UUID> {

    private final File userDir;
    private final BalanceMap balances;
    private final OutputStreamWriter writer;
    private final Logger logger;
    private final Set<UUID> toRemove = new HashSet<>();

    int deleted;

    public UserAndBalanceUpdater(CrownCore core) throws IOException {
        this.userDir = new File(core.getDataFolder().getPath() + File.separator + "playerdata");
        this.logger = core.getLogger();
        balances = CrownCore.getBalances().getMap();

        File outputLog = new File(core.getDataFolder(), "userAndBalanceUpdaterLog.txt");
        if(!outputLog.exists()) outputLog.createNewFile();

        writer = new OutputStreamWriter(new FileOutputStream(outputLog));
    }

    @Override
    public DataFixer<UUID> begin() {
        if(!userDir.exists()) throw new IllegalStateException("User directory doesn't exist");
        if(!userDir.isDirectory()) throw new IllegalStateException("User directory is not directory");

        for (OfflinePlayer player: Bukkit.getOfflinePlayers()){
            boolean inBalances = balances.contains(player.getUniqueId());

            File f = new File(userDir.getPath() + File.separator + player.getUniqueId() + ".yml");
            boolean fileExists = f.exists();

            if(!f.getName().contains("1d0e75b1-1930-373a-9b77-ae89ac735ec1")){
                if(!inBalances && !fileExists) continue;
                if(inBalances && fileExists) continue;
            }

            toRemove.add(player.getUniqueId());
            if(f.exists()) f.delete();
            log(Level.INFO, player.getUniqueId() + " didn't have either a file or wasn't in the balanceMap, deleting and removing from map");
        }
        return this;
    }

    @Override
    public UUID get(String fileName) {
        return UUID.fromString(fileName);
    }

    @Override
    public void log(Level level, String info) {
        logger.log(level, info);
        try {
            writer.write(level.getName() + " " + info + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean needsFix(UUID toCheck) {
        File f = new File(userDir, toCheck.toString() + ".yml");
        return !f.exists();
    }

    @Override
    public void fix(UUID toFix) {
        toRemove.add(toFix);
        log(Level.INFO, "Removing " + toFix.toString() + " from BalanceMap");
        deleted++;
    }

    @Override
    public void complete() {
        for (UUID id: toRemove){
            balances.remove(id);
        }

        CrownCore.getBalances().setMap((SortedBalanceMap) balances);
        CrownCore.getBalances().save();

        UserManager.LOADED_USERS.clear();
        UserManager.LOADED_ALTS.clear();

        log(Level.INFO, "UserAndBalanceData check ran and all unneeded files deleted");

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
