
let map = new Map;

function canComplete(user) {
  return Packages.net.forthecrown.guilds.Guilds.getGuild(user) != null;
}

function onActivate(handle) {
  map.clear();
}

function onReset(handle) {
  map.clear();
}

function onEvent(event, handle) {
  let user = event.user;
  let marketShop = event.marketShop;

  if (marketShop == null) {
    return;
  }

  if (!isValidShop(marketShop)) {
    return;
  }

  handle.givePoint(player);
  markShop(user, marketShop);
}

function isValidShop(user, marketShop) {
  let entry = map[user.uniqueId];
  if (entry == null) {
    return true;
  }

  let index = entry.indexOf(marketShop.name);
  return index == -1;
}

function markShop(user, marketShop) {
  let entry = map[user.uniqueId];

  if (entry == null) {
    entry = [];
    map[user.uniqueId] = entry;
  }

  entry.push(marketShop.name);
}