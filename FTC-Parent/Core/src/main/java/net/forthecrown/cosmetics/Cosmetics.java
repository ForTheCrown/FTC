package net.forthecrown.cosmetics;

public final class Cosmetics {

    private static PlayerRidingManager rideManager;

    private Cosmetics() {}

    public static void init(){
        rideManager = new PlayerRidingManager();
    }

    public static void shutDown(){
        rideManager.stopAllRiding();
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }

    /*public void onEnable() {
        // Config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        rideManager = new PlayerRidingManager();

        // Events
        //getServer().getPluginManager().registerEvents(new CosmeticEvents(), this);
        getServer().getPluginManager().registerEvents(CosmeticDeathEffect.listener, this);
        getServer().getPluginManager().registerEvents(CosmeticArrowEffect.listener, this);
        getServer().getPluginManager().registerEvents(CustomInv.listener, this); // TODO: move to core


        // Command
        new CommandCosmetics();
    }

    @Override
    public void onDisable() {
        getRideManager().stopAllRiding();
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }


    public static final Set<Particle> ACCEPTED_ARROW_PARTICLES = new HashSet<>(Arrays.asList(
            Particle.FLAME, Particle.SNOWBALL, Particle.SNEEZE,
            Particle.HEART, Particle.DAMAGE_INDICATOR, Particle.DRIPPING_HONEY,
            Particle.CAMPFIRE_COSY_SMOKE, Particle.SOUL, Particle.FIREWORKS_SPARK));

    public static final Set<String> ACCEPTED_DEATH_PARTICLES = new HashSet<>(
            Arrays.asList("SOUL", "TOTEM", "EXPLOSION", "ENDER_RING"));*/
}
