package net.forthecrown.vars;

import net.forthecrown.vars.types.VarType;

public record LoadedVar<T>(String name, T value, VarType<T> type) {
}