import "@ftc.inventory.ExtendedItems";

function test(user) {
    let ent = _entity;

    if (ent == null) {
        logger.warn("Test was not called from entity!");
        return false;
    }

    let helmet = ent.getEquipment().getHelmet();
    let sword = ExtendedItems.ROYAL_SWORD.get(helmet);

    if (sword == null) {
        logger.warn("Helmet not a sword!");
        return false;
    }

    return user.getUniqueId().equals(sword.getOwner());
}

function getFailMessage(user) {
    return Component.text("Not your sword lol");
}