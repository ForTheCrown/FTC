const STEVE_NBT = "{Count:1b,id:\"player_head\",tag:{display:{Name:\"{\\\"text\\\":\\\"Steve Plushie\\\"}\"},SkullOwner:{Id:[I;1267022859,-791852804,-1138278407,-1993224661],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjYyMjE3ZDkwMmVkYzkyOWRlNGY1MWI5MDcxZjU2YmFhYWMwNDVkMDRlYzA0Yjg0MGNjYzhlYzk5NjJmNjJlMyJ9fX0=\"}]}}}}";
const COOL_STEVE_NBT = "{Count:1b,id:\"player_head\",tag:{display:{Name:\"{\\\"text\\\":\\\"Gangsta Steve Plushie\\\"}\"},SkullOwner:{Id:[I;462057518,2081049106,-2055455267,-1318394564],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U3YWEzMDU2ZmJhN2FlZTE3NDIwM2M0MmUyOWRkN2YwM2NlZGQ1YTI0MzVlMWU3MzJiNDU1YzZmMTBhMWFjOSJ9fX0=\"}]}}}}";
const TECHNO_NBT = "{Count:1b,id:\"player_head\",tag:{display:{Name:\"{\\\"text\\\":\\\"Technoblade Plushie\\\"}\"},SkullOwner:{Id:[I;2020670698,1631341120,-1879163262,-1771588734],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTk5YWM3OTRlMzlhZjQyOGEyN2JlZGE1NTQxY2RiMzdjNWQyZjhhZThmYmI5Y2Y1NTcwMzJkMDUwN2I2YTE0OSJ9fX0=\"}]}}}}";

const ItemStacks = Java.type("net.forthecrown.utils.inventory.ItemStacks");
const Messages = Java.type("net.forthecrown.core.Messages");

function onStreakIncrease(user, streak) {
    if (user === null || streak === 0) return;

    // Alternating high streaks
    if (streak > 30) {
        giveRhines(user, 50000);
        giveGuildExp(user, 100);

        if (streak % 2 == 0) {
            giveGems(user, 500);
        } else {
            giveCrateKey(user);
        }
        return;
    }

    // Rhines & GuildExp
    let rhines = 0;
    let guildExp = 0;
    if (streak <= 5) {
        rhines = 5000;
        guildExp = 40;
    } else if (streak <= 10) {
        rhines = 10000;
        guildExp = 50;
    } else if (streak <= 15) {
        rhines = 20000;
        guildExp = 60;
    } else if (streak <= 20) {
        rhines = 25000;
        guildExp = 75;
    } else if (streak <= 30) {
        rhines = 50000;
        guildExp = 100;
    }
    giveRhines(user, rhines);
    giveGuildExp(user, guildExp);

    // Gems
    let gems = 0;
    if (streak == 2) gems = 50;
    else if (streak == 4) gems = 100;
    else if (streak == 7) gems = 150;
    else if (streak == 9) gems = 200;
    else if (streak == 12) gems = 250;
    else if (streak == 14 || streak == 17 ||
             streak == 19 || streak == 22 ||
             streak == 24 || streak == 27 ||
             streak == 29) gems = 500;
    giveGems(user, gems);

    // Plushies
    if (streak == 3 || streak == 5 ||
        streak == 8 || streak == 13 ||
        streak == 15 || streak == 18 ||
        streak == 23 || streak == 25 || streak == 28) {
        giveCrateKey(user);
    }
    if (streak == 10) {
        giveItem(user, STEVE_NBT, "Steve Plushie");
    } else if (streak == 20) {
        giveItem(user, COOL_STEVE_NBT, "Gangsta Steve Plushie");
    } else if (streak == 30) {
        giveItem(user, TECHNO_NBT, "Technoblade Plushie");
    }
}


function giveRhines(user, amount) {
    if (amount == 0) return;

    user.addBalance(amount);
    user.sendMessage(
            Text.format("You've received &6{0, rhines}&r.",
                        NamedTextColor.YELLOW,
                        amount
            )
    );
}

function giveGems(user, amount) {
    if (amount == 0) return;

    user.addGems(amount);
    user.sendMessage(
            Text.format("You've received &6{0, gems}&r.",
                        NamedTextColor.YELLOW,
                        amount
            )
    );
}

function giveGuildExp(user, amount) {
    if (amount == 0) return;

    if (user.getGuild() != null) {
        user.getGuild().getMember(user.getUniqueId()).addExpEarned(amount);
    }
    user.sendMessage(
            Text.format("You've received &6{0, number} Guild Exp&r.",
                        NamedTextColor.YELLOW,
                        amount
            )
    );
}

function giveItem(user, nbt, name) {
    Util.giveOrDropItem(user.getInventory(), user.getLocation(), ItemStacks.fromNbtString(nbt));
    user.sendMessage(
            Text.format(
                    "You've received the &6{0}&r!",
                    NamedTextColor.YELLOW,
                    name
            )
    );
}

function giveCrateKey(user) {
    Util.consoleCommand("excellentcrates key give %s plushie 1", user.getName());
}
