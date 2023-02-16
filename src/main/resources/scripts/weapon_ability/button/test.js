var validArea = new WorldBounds3i(
  Worlds.overworld(),
  262, 112, 180,
  274,  99, 187
);

validArea.contains(reader.getPlayer());