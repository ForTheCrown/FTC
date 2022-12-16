package net.forthecrown.log;

import com.mojang.serialization.Codec;

public record SchemaField<T>(String name, int id, Codec<T> type) {
}