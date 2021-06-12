package net.forthecrown.emperor;

import net.forthecrown.emperor.admin.CrownPunishmentManager;
import net.forthecrown.emperor.admin.ServerRules;
import net.forthecrown.emperor.admin.jails.CrownJailManager;
import net.forthecrown.emperor.commands.manager.CoreCommands;
import net.forthecrown.emperor.comvars.ComVar;
import net.forthecrown.emperor.comvars.ComVars;
import net.forthecrown.emperor.comvars.types.ComVarType;
import net.forthecrown.emperor.comvars.types.KeyComVarType;
import net.forthecrown.emperor.crownevents.ArmorStandLeaderboard;
import net.forthecrown.emperor.economy.CrownBalances;
import net.forthecrown.emperor.economy.CrownBlackMarket;
import net.forthecrown.emperor.economy.shops.CrownShopManager;
import net.forthecrown.emperor.events.*;
import net.forthecrown.emperor.registry.CrownActionRegistry;
import net.forthecrown.emperor.registry.CrownCheckRegistry;
import net.forthecrown.emperor.registry.CrownKitRegistry;
import net.forthecrown.emperor.registry.CrownWarpRegistry;
import net.forthecrown.emperor.useables.CrownUsablesManager;
import net.forthecrown.emperor.user.CrownUserManager;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.CrownUtils;
import net.kyori.adventure.key.Key;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Main class that does all the dirty internal stuff
 */
public final class Main extends JavaPlugin implements CrownCore {

    //Hacky way of determining if we're on the test server or not
    public static final ComVar<Boolean> inDebugMode = ComVars.set("core_debug", ComVarType.BOOLEAN, !new File("plugins/CoreProtect/config.yml").exists());

    static Main                     inst;

    static String                   prefix = "&6[FTC]&r  ";
    static String                   discord;
    static PeriodicalSaver          saver;
    static ComVar<Integer>          maxMoneyAmount;

    static ComVar<Key>              onFirstJoinKit;
    static ComVar<Byte>             maxNickLength;
    static ComVar<Short>            nearRadius;
    static ComVar<Short>            hoppersInOneChunk;
    static ComVar<Long>             marriageCooldown;
    static ComVar<Long>             userDataResetInterval;// 5356800000L aka 2 months, by default
    static ComVar<Long>             branchSwapCooldown;// 172800000 aka 2 days, by default
    static ComVar<Boolean>          allowOtherPlayerNicks;
    static ComVar<Boolean>          taxesEnabled;
    static ComVar<Boolean>          logAdminShop;
    static ComVar<Boolean>          logNormalShop;
    static ComVar<Integer>          tpTickDelay;
    static ComVar<Integer>          tpCooldown;
    static ComVar<Integer>          tpaExpiryTime;
    static ComVar<Integer>          startRhines;
    static ComVar<Integer>          baronPrice;

    static CrownBalances            balances;
    static CrownBroadcaster         announcer;
    static CrownBlackMarket         blackMarket;
    static CrownKingship            kingship;

    static CrownUsablesManager      usablesManager;
    static CrownShopManager         shopManager;
    static CrownPunishmentManager   punishmentManager;
    static CrownUserManager         userManager;
    static CrownJailManager         jailManager;

    static ServerRules              rules;

    static CrownWarpRegistry        warpRegistry;
    static CrownKitRegistry         kitRegistry;
    static CrownActionRegistry      actionRegistry;
    static CrownCheckRegistry       checkRegistry;

    static Location                 serverSpawn;
    static CrownMessages            messages;
    static Logger                   logger;

    static final Map<Material, ComVar<Short>> defaultItemPrices = new HashMap<>();
    public static final Set<ArmorStandLeaderboard> LEADERBOARDS = new HashSet<>();
    static LuckPerms luckPerms;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();

        luckPerms = LuckPermsProvider.get();

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        announcer = new CrownBroadcaster();

        messages = new CrownMessages();
        messages.load();

        balances = new CrownBalances(this);
        blackMarket = new CrownBlackMarket(this);
        shopManager = new CrownShopManager();
        userManager = new CrownUserManager(this);
        punishmentManager = new CrownPunishmentManager();
        jailManager = new CrownJailManager();
        kingship = new CrownKingship(this);
        rules = new ServerRules();

        actionRegistry = new CrownActionRegistry();
        checkRegistry = new CrownCheckRegistry();

        usablesManager = new CrownUsablesManager();
        usablesManager.registerDefaults(actionRegistry, checkRegistry);

        warpRegistry = new CrownWarpRegistry();
        kitRegistry = new CrownKitRegistry();

        CoreCommands.init();

        registerEvents();
        if(getConfig().getBoolean("System.run-deleter-on-startup")) userManager.checkAllUserDatas();
    }

    @Override
    public void onDisable() {
        if(getConfig().getBoolean("System.save-on-disable")) CrownCore.saveFTC();

        Bukkit.getScheduler().cancelTasks(this);

        for (Player p: Bukkit.getOnlinePlayers()) p.closeInventory();
        for (ArmorStandLeaderboard a: LEADERBOARDS) a.destroy();
        for (LivingEntity e: MobHealthBar.NAMES.keySet()) e.customName(MobHealthBar.NAMES.getOrDefault(e, null));

        UserManager.LOADED_USERS.forEach((i, u) -> {
            u.setAfk(false);
            u.updateAfk();
        });
        UserManager.LOADED_USERS.clear();
        UserManager.LOADED_ALTS.clear();
    }

    private void registerEvents(){
        Server server = getServer();
        PluginManager pm = server.getPluginManager();

        pm.registerEvents(new JeromeEvent(), this);

        pm.registerEvents(new CoreListener(), this);
        pm.registerEvents(new GraveListener(), this);
        pm.registerEvents(new ChatEvents(), this);

        pm.registerEvents(new ShopCreateEvent(), this);
        pm.registerEvents(new SignInteractEvent(), this);
        pm.registerEvents(new ShopDestroyEvent(), this);
        pm.registerEvents(new ShopTransactionEvent(), this);

        pm.registerEvents(new BlackMarketEvents(), this);

        pm.registerEvents(new MobHealthBar(), this);
        pm.registerEvents(new SmokeBomb(), this);
        pm.registerEvents(new InteractableEvents(), this);

        pm.registerEvents(new MarriageListener(), this);
    }

    @Override
    public void onLoad() {
        CrownWorldGuard.init();
    }

    @Override
    public void saveConfig() {
        getConfig().set("Taxes", taxesEnabled.getValue());
        getConfig().set("BranchSwapCooldown", branchSwapCooldown.getValue());
        getConfig().set("UserDataResetInterval", userDataResetInterval.getValue());
        getConfig().set("Shops.log-normal-purchases", logNormalShop.getValue());
        getConfig().set("Shops.log-admin-purchases", logAdminShop.getValue());
        getConfig().set("HoppersInOneChunk", hoppersInOneChunk.getValue());
        getConfig().set("MaxMoneyAmount", maxMoneyAmount.getValue());
        getConfig().set("TeleportTickDelay", tpTickDelay.getValue());
        getConfig().set("TeleportCooldown", tpCooldown.getValue());
        getConfig().set("TpaExpiryTime", tpaExpiryTime.getValue());
        getConfig().set("OnJoinKit", onFirstJoinKit.getValue().value());
        getConfig().set("StartRhines", startRhines.getValue(100));
        getConfig().set("MaxNickLength", maxNickLength.getValue((byte) 16));
        getConfig().set("AllowOtherPlayerNicks", allowOtherPlayerNicks.getValue());
        getConfig().set("BaronPrice", baronPrice.getValue());
        getConfig().set("NearRadius", nearRadius.getValue());
        getConfig().set("ServerSpawn", serverSpawn);
        getConfig().set("MarriageStatusCooldown", marriageCooldown.getValue());

        for (Material m: defaultItemPrices.keySet()){
            getConfig().set("DefaultPrices." + m.toString(), defaultItemPrices.get(m).getValue((short) 2));
        }

        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        prefix = getConfig().getString("Prefix");
        discord = getConfig().getString("Discord");

        maxMoneyAmount = ComVars.set(       "core_maxMoneyAmount",            ComVarType.INTEGER,     getConfig().getInt("MaxMoneyAmount"));
        branchSwapCooldown = ComVars.set(   "core_branchSwapInterval",        ComVarType.LONG,        getConfig().getLong("BranchSwapCooldown"));
        taxesEnabled = ComVars.set(         "core_taxesEnabled",              ComVarType.BOOLEAN,     getConfig().getBoolean("Taxes"));
        userDataResetInterval = ComVars.set("core_userEarningsResetInterval", ComVarType.LONG,        getConfig().getLong("UserDataResetInterval"));
        logAdminShop = ComVars.set(         "core_log_admin",                 ComVarType.BOOLEAN,     getConfig().getBoolean("Shops.log-admin-purchases"));
        logNormalShop = ComVars.set(        "core_log_normal",                ComVarType.BOOLEAN,     getConfig().getBoolean("Shops.log-normal-purchases"));
        hoppersInOneChunk = ComVars.set(    "core_maxHoppersPerChunk",        ComVarType.SHORT,       (short) getConfig().getInt("HoppersInOneChunk"));
        tpTickDelay = ComVars.set(          "core_tpTickDelay",               ComVarType.INTEGER,     getConfig().getInt("TeleportTickDelay"));
        tpCooldown = ComVars.set(           "core_tpCooldown",                ComVarType.INTEGER,     getConfig().getInt("TeleportCooldown"));
        tpaExpiryTime = ComVars.set(        "core_tpaExpiryTime",             ComVarType.INTEGER,     getConfig().getInt("TpaExpiryTime"));
        onFirstJoinKit = ComVars.set(       "core_onJoinKit",                 KeyComVarType.KEY,      Key.key(this, getConfig().getString("OnJoinKit")));
        startRhines = ComVars.set(          "core_startRhines",               ComVarType.INTEGER,     getConfig().getInt("StartRhines"));
        maxNickLength = ComVars.set(        "core_maxNickLength",             ComVarType.BYTE,        (byte) getConfig().getInt("MaxNickLength"));
        allowOtherPlayerNicks = ComVars.set("core_allowOtherPlayerNicks",     ComVarType.BOOLEAN,     getConfig().getBoolean("AllowOtherPlayerNicks"));
        baronPrice = ComVars.set(           "core_baronPrice",                ComVarType.INTEGER,     getConfig().getInt("BaronPrice"));
        nearRadius = ComVars.set(           "core_nearRadius",                ComVarType.SHORT,       (short) getConfig().getInt("NearRadius"));
        marriageCooldown = ComVars.set(     "core_marriageCooldown",          ComVarType.LONG,        getConfig().getLong("MarriageStatusCooldown"));

        serverSpawn = getConfig().getLocation("ServerSpawn", new Location(CrownUtils.WORLD_VOID, 153.5, 5, 353.5, 90, 0));

        loadDefaultItemPrices();

        if(saver != null){
            try {
                saver.cancel();
            } catch (IllegalStateException ignored){}
        }

        if(getConfig().getBoolean("System.save-periodically")){
            saver = new PeriodicalSaver(this);
            saver.start();
        } else if (saver != null && !saver.isCancelled()){
            saver.cancel();
            saver = null;
        }
    }

    private void loadDefaultItemPrices(){
        ConfigurationSection itemPrices = getConfig().getConfigurationSection("DefaultPrices");

        for(String s : itemPrices.getKeys(true)){
            Material mat = Material.valueOf(s);

            //defaultItemPrices.put(mat, (short) itemPrices.getInt(s));
            defaultItemPrices.put(mat,
                    ComVars.set(
                            "sellshop_price_" + s.toLowerCase(),
                            ComVarType.SHORT,
                            (short) itemPrices.getInt(s)
                    )
            );
        }
    }

    @Override
    public @NonNull String namespace() {
        return getName().toLowerCase();
    }
}
