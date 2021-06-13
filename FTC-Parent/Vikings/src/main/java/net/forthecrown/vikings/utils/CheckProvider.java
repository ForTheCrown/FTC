package net.forthecrown.vikings.utils;

import net.forthecrown.vikings.valhalla.triggers.TriggerCheck;

import java.util.function.Supplier;

public interface CheckProvider extends Supplier<TriggerCheck<?>> {
}
