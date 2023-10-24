package net.forthecrown.leaderboards;

import org.bukkit.entity.TextDisplay;

public record BoardData(int id, BoardImpl board, TextDisplay display) {

}
