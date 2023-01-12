import "@ftc.inventory.weapon.ability.menu.AbilityAnimation";

function test(user) {
    var instance = AbilityAnimation.getInstance();
    return !instance.hasOngoing();
}

function getFailMessage(user) {
    return Component.text(
        "Cannot open menu during ongoing animation", 
        NamedTextColor.GRAY
    );
}