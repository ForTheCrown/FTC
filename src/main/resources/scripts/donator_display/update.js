import "@bukkit.entity.ArmorStand";
import "@bukkit.inventory.ItemStack";

if (args.length < 1) {
  throw "Expected args[0] to be player name/uuid";
}

if (args.length < 2) {
  throw "Expected args[1] to be color name";
}

const user = Users.get(args[0]);
const COLOR = NamedTextColor.NAMES.valueOrThrow(args[1]);

if (user == null) {
  throw "Unknown user " + args[0];
}

const LEADERBOARD_TAG = "donator_display";
const YAW = 123;
const PITCH = 15;

const standLocations = [
  { x: 210.5, y: 72.5, z: 205.5 },
  { x: 211.5, y: 73.5, z: 207.5 },
  { x: 209.5, y: 72.5, z: 208.5 },
  { x: 209.5, y: 73.5, z: 210.5 },
  { x: 207.5, y: 72.5, z: 211.5 },
];
const armorStands = createArmorStandArray();

if (Cooldown.containsOrAdd(user, "donator_display", 15 * 20)) {
  logger.warn("Player {} is on cooldown, cannot update donator display", user);
} else {
  updateDisplays();
}

function updateDisplays() {
  const displayName = user.getTabName().color(COLOR);
  const profile = user.getOfflinePlayer().getPlayerProfile();
  
  if (!profile.isComplete()) {
    profile.complete(true, true);
  }

  let headBuilder = ItemStacks.headBuilder();
  headBuilder.setProfile(profile);

  pushStandsBack();

  const newFirst = armorStands[0];
  newFirst.customName(displayName);
  newFirst.setCustomNameVisible(true);
  newFirst.getEquipment().setHelmet(headBuilder.build());
  giveArmor(newFirst);
}

function pushStandsBack() {
  for (let i = armorStands.length-2; i >= 0; i--) {
    let stand = armorStands[i];
    let next = armorStands[i + 1];

    next.customName(stand.customName());

    const sEquip = stand.getEquipment();
    const nEquip = next.getEquipment();

    nEquip.setHelmet(sEquip.getHelmet());

    if (ItemStacks.notEmpty(sEquip.getHelmet())) {
      next.setCustomNameVisible(true);
    }

    if (ItemStacks.notEmpty(sEquip.getHelmet())) {
      giveArmor(next);
    }
  }
}

function giveArmor(stand) {
  let eq = stand.getEquipment();
  eq.setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
  eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
  eq.setBoots(new ItemStack(Material.GOLDEN_BOOTS));
}

function createArmorStandArray() {
  const array = Array(standLocations.length);
  const world = Worlds.overworld();

  for (let i = 0; i < array.length; i++) {
    let pos = standLocations[i];
    let location = new Location(world, pos.x, pos.y, pos.z, YAW, PITCH);
    
    let nearby = location.getNearbyEntitiesByType(ArmorStand.class, 0.5, stand => {
      return stand.getScoreboardTags().contains(LEADERBOARD_TAG);
    });

    if (nearby.isEmpty()) {
      array[i] = spawnStand(location);
      continue;
    } else if (nearby.size() > 1) {
      logger.warn("More than 1 donator display stand at {}, total={}", 
        pos, nearby.size()
      );
    }

    array[i] = nearby.iterator().next();
    array[i].teleport(location);
  }

  return array;
}

function spawnStand(location) {
  const w = location.getWorld();

  return w.spawn(location, ArmorStand.class, stand => {
    stand.setInvulnerable(true);
    stand.setArms(true);
    stand.setAI(false);
    stand.setGravity(false);
    stand.addScoreboardTag(LEADERBOARD_TAG);
  });
}