package net.forthecrown.core.converters;

import net.forthecrown.core.CrownCore;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Balances_YamlToJson {
    public static void checkAndRun() {
        if(!new File(CrownCore.dataFolder().getPath() + File.separator + "balance.yml").exists()) return;

        try {
            new Balances_YamlToJson()
                    .convert()
                    .finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final File yamlFile;
    private final File jsonFile;

    private final YamlConfiguration configuration;
    private final JsonBuf json;

    private Balances_YamlToJson() throws IOException {
        this.yamlFile = new File(CrownCore.dataFolder().getPath() + File.separator + "balance.yml");
        this.jsonFile = new File(CrownCore.dataFolder().getPath() + File.separator + "balances.json");

        if(!jsonFile.exists()) jsonFile.createNewFile();

        this.configuration = YamlConfiguration.loadConfiguration(yamlFile);
        this.json = JsonBuf.empty();
    }

    public Balances_YamlToJson convert() {
        for (String s: configuration.getKeys(false)) {
            /*UUID id = UUID.fromString(s);
            OfflinePlayer player = Bukkit.getOfflinePlayer(id);

            if(player == null || player.getName() == null) continue;

            if (System.currentTimeMillis() - player.getLastSeen() > CrownCore.getUserResetInterval()) {
                CrownCore.getUserSerializer().delete(id);
                continue;
            }*/

            int bal = configuration.getInt(s);
            if(bal <= CrownCore.getStartRhines()) continue;

            json.add(s, bal);
        }

        return this;
    }

    public void finish() throws IOException {
        yamlFile.delete();

        JsonUtils.writeFile(json.getSource(), jsonFile);
    }
}
