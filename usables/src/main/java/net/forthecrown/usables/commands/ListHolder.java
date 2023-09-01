package net.forthecrown.usables.commands;

import net.forthecrown.usables.ComponentList;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.objects.UsableObject;

public interface ListHolder<T extends UsableComponent> {

  ComponentList<T> getList();

  void postEdit();

  UsableObject object();
}
