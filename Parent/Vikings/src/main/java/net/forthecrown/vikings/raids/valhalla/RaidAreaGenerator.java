package net.forthecrown.vikings.raids.valhalla;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.utils.ListUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Structure;
import org.bukkit.block.structure.UsageMode;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

//Class existence reason: Make raid area
public class RaidAreaGenerator {

    private final VikingRaid raid;
    private final Random random = new Random();
    private LootTable chestLootTable;
    private Location blockToPower;

    private CrownBoundingBox originalArea;
    private CrownBoundingBox newArea;

    private List<Location> chestLocations;
    private List<Location> hostileLocations;
    private List<Location> passiveLocations;
    private List<Location> specialLocations;

    private List<EntityType> passiveMobs;
    private List<EntityType> hostileMobs;
    private List<EntityType> specialMobs;

    private Consumer<LivingEntity> onPassiveSpawn;
    private Consumer<LivingEntity> onHostileSpawn;
    private Consumer<LivingEntity> onSpecialSpawn;

    public RaidAreaGenerator(VikingRaid raid){
        this.raid = raid;

        //Set default consumers
        onPassiveSpawn = entity -> {};
        onHostileSpawn = entity -> {
          double health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * raid.getCurrentParty().getModifier();
          entity.setHealth(health);
          entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        };
        onSpecialSpawn = entity -> {};
    }

    public void generate(){
        clearMobs();
        placeHostileMobs();
        placePassiveMobs();
        if(raid.getCurrentParty().specialsAllowed()) placeSpecialMobs();

        placeLoot();
        generateChunks();
        powerPowerblock();
        clearDrops();
    }

    public void clearDrops(){
        for (Item i: newArea.getEntitiesByType(Item.class)){
            i.remove();
        }
    }

    public void generateChunks(){
        Validate.notNull(newArea);
        Validate.notNull(originalArea);

        //Bukkit to WorldEdit conversion stuffs
        World world = BukkitAdapter.adapt(originalArea.getWorld());
        BlockVector3 min = BukkitAdapter.asBlockVector(originalArea.getMinLocattion());
        BlockVector3 max = BukkitAdapter.asBlockVector(originalArea.getMaxLocation());
        BlockVector3 to = BukkitAdapter.asBlockVector(newArea.getMinLocattion());

        //I have no idea, copied this from the WE API wiki
        CuboidRegion region = new CuboidRegion(world, min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);
        }

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(to)
                    .build();
            Operations.complete(operation);
        }

        powerStructureBlocks();
    }

    private void powerStructureBlocks(){
        BlockVector3 min = BukkitAdapter.asBlockVector(newArea.getMinLocattion());
        BlockVector3 max = BukkitAdapter.asBlockVector(newArea.getMaxLocation());
        World world = BukkitAdapter.adapt(newArea.getWorld());
        Region region = new CuboidRegion(world, min, max);

        for (BlockVector3 b : region) {
            Block block = new Location(newArea.getWorld(), b.getBlockX(), b.getBlockY(), b.getBlockZ()).getBlock();
            if(block.getType() != Material.STRUCTURE_BLOCK) continue; //If it's not a structure block, we don't care

            //This much code in a for loop, I must actually be insane
            Structure structure = (Structure) block.getState();
            structure.setUsageMode(UsageMode.LOAD);
            //Replaces placeholders or sets a random medium house if blank
            structure.setStructureName(VikingBuilds.replacePlaceholders(structure.getStructureName()));
            structure.update();

            //Gets the NMS structure block to load the structure
            //Why bukkit doesn't have a method for this is beyond me
            NmsStructureBlockGetter dumbThing = new NmsStructureBlockGetter(block);
            dumbThing.getSnapshot().a(((CraftWorld) block.getWorld()).getHandle(), true);

            //Power structure block to create build and then remove structure block
            Location b2 = block.getLocation().subtract(0, 1, 0);
            Material oldMaterial = b2.getBlock().getType();
            b2.getBlock().setType(Material.REDSTONE_BLOCK); //set the block under to redstone
            b2.getBlock().setType(oldMaterial); //And then set it back to what it was
            block.setType(Material.GRASS_BLOCK);
        }
        activateCommandBlocks(); //Has to be done seperately and after as structures may contain cmd blocks that need to be activated
    }

    private void activateCommandBlocks(){
        BlockVector3 min = BukkitAdapter.asBlockVector(newArea.getMinLocattion());
        BlockVector3 max = BukkitAdapter.asBlockVector(newArea.getMaxLocation());
        World world = BukkitAdapter.adapt(newArea.getWorld());
        Region region = new CuboidRegion(world, min, max);

        for (BlockVector3 b : region) {
            Block block = new Location(newArea.getWorld(), b.getBlockX(), b.getBlockY(), b.getBlockZ()).getBlock();
            if (block.getType() != Material.COMMAND_BLOCK) continue; //If it's not a structure block, we don't care

            Block below = block.getLocation().subtract(0, 1, 0).getBlock();
            Material previous = below.getType();
            below.setType(Material.REDSTONE_BLOCK);
            below.setType(previous);
        }
    }

    public void clearMobs(){
        for (Entity e: newArea.getLivingEntities()){
            if(e instanceof Player){
                e.teleport(RaidManager.EXIT_LOCATION);
                continue;
            }
            e.remove();
        }
    }

    public void placeLoot(){
        for (Location l: getChestLocations()){
            boolean spawnAtLocation = random.nextBoolean();
            //boolean spawnAtLocation = 5 < random.nextInt(10) + raid.getCurrentParty().getModifier();

            if(!spawnAtLocation){
                if(l.getBlock().getState() instanceof Chest){
                    ((Chest) l.getBlock().getState()).getBlockInventory().clear();
                }

                l.getBlock().setType(Material.AIR);
                continue;
            }
            if(!(l.getBlock().getState() instanceof Chest)) l.getBlock().setType(Material.CHEST);

            Chest chest = (Chest) l.getBlock().getState();
            chest.getBlockInventory().clear();
            chest.setLootTable(getChestLootTable());
            getChestLootTable().fillInventory(chest.getBlockInventory(), random, new LootContext.Builder(chest.getLocation()).build());
        }
    }

    /*
     * The following all 3 just check to make sure the mob types exist along with the spawn locations
     * And if they do, then it spawn them
     */
    public void placeSpecialMobs(){
        if (specialLocations == null || specialLocations.isEmpty()) specialLocations = hostileLocations;
        placeMobsInLocation(specialMobs, hostileLocations, onSpecialSpawn);
    }
    public void placeHostileMobs(){
        placeMobsInLocation(hostileMobs, hostileLocations, onHostileSpawn);
    }
    public void placePassiveMobs(){
        placeMobsInLocation(passiveMobs, passiveLocations, onPassiveSpawn);
    }

    public void placeMobsInLocation(List<EntityType> mobs, List<Location> locations, Consumer<LivingEntity> action){
        if(ListUtils.isNullOrEmpty(mobs)) return;
        if(ListUtils.isNullOrEmpty(locations)) return;

        for (Location l: locations){
            //Don't always spawn the entity, just every ~2/3 times
            if(random.nextInt(3) > 0) continue;

            LivingEntity ent = (LivingEntity) l.getWorld().spawnEntity(l,
                    mobs.get(mobs.size()-1 == 0 ? 0 : random.nextInt(mobs.size()-1)) //bOuND mUsT bE pOsItIvE
            );
            if(action != null) action.accept(ent); //Apply consumer to entity
        }
    }

    public void powerPowerblock(){
        if(getBlockToPower() == null) return;
        getBlockToPower().getBlock().setType(Material.REDSTONE_BLOCK);
        getBlockToPower().getBlock().setType(Material.AIR);
    }

    public VikingRaid getRaid() {
        return raid;
    }

    public List<Location> getChestLocations() {
        return chestLocations;
    }

    public RaidAreaGenerator setChestLocations(Location... chestLocations) {
        this.chestLocations = Arrays.asList(chestLocations);
        return this;
    }

    public List<Location> getHostileLocations() {
        return hostileLocations;
    }

    public RaidAreaGenerator setHostileLocations(Location... hostileLocations) {
        this.hostileLocations = Arrays.asList(hostileLocations);
        return this;
    }

    public List<Location> getPassiveLocations() {
        return passiveLocations;
    }

    public RaidAreaGenerator setPassiveLocations(Location... passiveLocations) {
        this.passiveLocations = Arrays.asList(passiveLocations);
        return this;
    }

    public List<EntityType> getPassiveMobs() {
        return passiveMobs;
    }

    public RaidAreaGenerator setPassiveMobs(EntityType... passiveMobs) {
        this.passiveMobs = Arrays.asList(passiveMobs);
        return this;
    }

    public List<EntityType> getHostileMobs() {
        return hostileMobs;
    }

    public RaidAreaGenerator setHostileMobs(EntityType... hostileMobs) {
        this.hostileMobs = Arrays.asList(hostileMobs);
        return this;
    }

    public RaidAreaGenerator setChestLootTable(LootTable chestLootTable) {
        this.chestLootTable = chestLootTable;
        return this;
    }

    public LootTable getChestLootTable() {
        return chestLootTable;
    }

    public Consumer<LivingEntity> getOnPassiveSpawn() {
        return onPassiveSpawn;
    }

    public RaidAreaGenerator onPassiveSpawn(Consumer<LivingEntity> passiveConsumer) {
        this.onPassiveSpawn = passiveConsumer;
        return this;
    }

    public Consumer<LivingEntity> getOnHostileSpawn() {
        return onHostileSpawn;
    }

    public RaidAreaGenerator onHostileSpawn(Consumer<LivingEntity> onHostileSpawn) {
        this.onHostileSpawn = onHostileSpawn;
        return this;
    }

    public RaidAreaGenerator onSpecialSpawn(Consumer<LivingEntity> specialSpawn){
        this.onSpecialSpawn = specialSpawn;
        return this;
    }

    public Consumer<LivingEntity> getOnSpecialSpawn() {
        return onSpecialSpawn;
    }

    public List<Location> getSpecialLocations() {
        return specialLocations;
    }

    public RaidAreaGenerator setSpecialSpawns(Location... specialLocs){
        this.specialLocations = Arrays.asList(specialLocs);
        return this;
    }

    public RaidAreaGenerator setSpecialMobs(EntityType... specialMobs) {
        this.specialMobs = Arrays.asList(specialMobs);
        return this;
    }

    public List<EntityType> getSpecialMobs() {
        return specialMobs;
    }

    public void compareBBoxes(){
        if(originalArea == null || newArea == null) return;

        boolean shouldThrowException = originalArea.getVolume() != newArea.getVolume() &&
                originalArea.getWidthZ() != newArea.getWidthZ() &&
                originalArea.getWidthX() != newArea.getWidthZ() &&
                originalArea.getHeight() != newArea.getHeight();

        if(shouldThrowException) throw new IllegalArgumentException("The bounding boxes must be the same sizes");
    }

    public CrownBoundingBox getOriginalArea() {
        return originalArea;
    }

    public RaidAreaGenerator setOriginalArea(CrownBoundingBox originalArea) {
        compareBBoxes();
        this.originalArea = originalArea;
        return this;
    }

    public CrownBoundingBox getNewArea() {
        return newArea;
    }

    public RaidAreaGenerator setNewArea(CrownBoundingBox newArea) {
        compareBBoxes();
        this.newArea = newArea;
        return this;
    }

    @Nullable
    public Location getBlockToPower() {
        return blockToPower;
    }

    public RaidAreaGenerator setBlockToPower(Location blockToPower) {
        this.blockToPower = blockToPower;
        return this;
    }
}
