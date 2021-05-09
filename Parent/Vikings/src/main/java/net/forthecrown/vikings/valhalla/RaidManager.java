package net.forthecrown.vikings.valhalla;

import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.creation.RaidWorldCreator;

import java.io.File;

public class RaidManager {
    private final RaidWorldCreator worldCreator;
    private final File raidDir;

    private RaidManager(){
        raidDir = new File(Vikings.inst().getDataFolder() + File.separator + "raids");
        worldCreator = new RaidWorldCreator();
    }

    public static RaidManager init(){
        return new RaidManager();
    }

    public RaidParty createParty(){

    }

    public File getRaidDir() { return raidDir; }
    public RaidWorldCreator getWorldCreator() { return worldCreator; }
}
