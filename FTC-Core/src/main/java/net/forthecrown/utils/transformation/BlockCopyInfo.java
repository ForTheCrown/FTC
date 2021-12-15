package net.forthecrown.utils.transformation;

import org.bukkit.block.Block;

// Just a class which holds two values,
// the original block: copy,
// and the destination block: paste
public record BlockCopyInfo(Block copy, Block paste) {
}
