package net.forthecrown.vikings.utils;

import net.forthecrown.vikings.valhalla.triggers.TriggerAction;

import java.util.function.Supplier;

public interface ActionProvider extends Supplier<TriggerAction<?>> {
}
