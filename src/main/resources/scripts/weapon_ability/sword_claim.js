import "@ftc.inventory.weapon.ability.AbilityAnimation";
import "@ftc.inventory.ExtendedItems";
import "@ftc.utils.inventory.ItemStacks";

function onUse(user) {
    let ent = _entity;

    if (ent == null) {
        logger.warn("Test was not called from entity!");
        return;
    }

    let helmet = ent.getEquipment().getHelmet();
    let sword = ExtendedItems.ROYAL_SWORD.get(helmet);

    if (sword == null) {
        logger.warn("Helmet not a sword!");
        return;
    }

    let inv = user.getInventory();
    let held = inv.getItemInMainHand();

    // Place item in main hand if it's empty, otherwise just
    // or drop it to the player
    if (ItemStacks.isEmpty(held)) {
        inv.setItemInMainHand(helmet);
    } else {
        Util.giveOrDropItem(
            user.getInventory(),
            user.getLocation(),
            helmet
        );
    }

    user.sendMessage(
        Component.text("Got sword!", NamedTextColor.YELLOW)
    );

    logger.info("{} picked up their sword after animation", user);

    // Remove the entity;
    // And cleanup the 'ongoing' animation
    ent.remove();
    let anim = AbilityAnimation.getInstance();
    let ongoing = anim.getOngoing();
    if (ongoing == null || ongoing.isItemTaken()) {
        return;
    }

    // Set item taken, and cancel the
    // stash task with stop()
    ongoing.setItemTaken(true);
    ongoing.stop();
    anim.setOngoing(null);
}