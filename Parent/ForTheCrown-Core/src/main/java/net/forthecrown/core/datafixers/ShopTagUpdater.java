package net.forthecrown.core.datafixers;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.ShopManager;
import net.forthecrown.core.types.CrownSignShop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShopTagUpdater implements DataFixer<CrownSignShop> {

    private final OutputStreamWriter writer;

    private final File dir;
    private final Logger logger;
    private final NamespacedKey key;
    int updated = 0;

    public ShopTagUpdater(FtcCore core) throws IOException {
        this.dir = new File(core.getDataFolder() + File.separator + "shopdata" + File.separator);
        this.logger = core.getLogger();
        key = FtcCore.SHOP_KEY;

        File outputLog = new File(core.getDataFolder() + File.separator + "shopUpdater.txt");
        if(!outputLog.exists()) outputLog.createNewFile();
        writer = new OutputStreamWriter(new FileOutputStream(outputLog));
    }

    @Override
    public DataFixer<CrownSignShop> begin() {
        if(!dir.exists()) throw new IllegalStateException(dir.getPath() + " doesn't exist");
        if(!dir.isDirectory()) throw new IllegalStateException(dir.getPath() + " is not directory");

        for (File f: dir.listFiles()){
            try {
                if(f.length() == 0){
                    log(Level.WARNING, "Found empty file: " + f.getName());
                    continue;
                }
                CrownSignShop s = get(f.getName());
                if(s == null) continue;
                if(needsFix(s)) fix(s);
                s.unload();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        log("Updated " + updated + " shops in total");
        return this;
    }

    @Override
    public void complete() {
        ShopManager.LOADED_SHOPS.clear();
        log("Unloading all loaded shops");

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CrownSignShop get(String fileName){
        fileName = fileName.replaceAll("_void", " void")
                .replaceAll("_maps", " maps")
                .replaceAll("_senate", " senate");
        String[] divided = fileName.replaceAll(".yml", "").split("_");

        World world = Objects.requireNonNull(Bukkit.getWorld(divided[0].replaceAll(" ", "_")));
        int x = Integer.parseInt(divided[1]);
        int y = Integer.parseInt(divided[2]);
        int z = Integer.parseInt(divided[3]);

        Location loc = new Location(world, x, y, z);
        if(!isSign(loc)){
            log(Level.WARNING, fileName + " is not a sign");
            return null;
        }

        return new CrownSignShop(loc);
    }

    @Override
    public boolean needsFix(CrownSignShop shop){
        return !(shop.getSign().getPersistentDataContainer().has(key, PersistentDataType.BYTE));
    }

    public boolean isSign(Location shop){
        return shop.getBlock().getState() instanceof Sign;
    }

    @Override
    public void fix(CrownSignShop shop){
        Sign sign = shop.getSign();
        sign.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> sign.update(), 1);
        log("Updated tags of shop " + shop.getName());
        updated++;
    }

    private void log(String info) { log(Level.INFO, info); }

    @Override
    public void log(Level level, String info){
        logger.log(level, info);
        try {
            writer.write(level.getName() + " " + info + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
