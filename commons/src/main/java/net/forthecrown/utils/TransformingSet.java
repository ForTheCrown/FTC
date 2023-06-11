package net.forthecrown.utils;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class TransformingSet<F, T> implements Set<T> {

  private final Set<F> backingSet;
  private final Function<F, T> fromBacking;
  private final Function<T, F> toBacking;

  public TransformingSet(
      Set<F> backingSet,
      Function<F, T> fromBacking,
      Function<T, F> toBacking
  ) {
    this.backingSet = Objects.requireNonNull(backingSet);
    this.fromBacking = Objects.requireNonNull(fromBacking);
    this.toBacking = Objects.requireNonNull(toBacking);
  }

  @Override
  public int size() {
    return backingSet.size();
  }

  @Override
  public boolean isEmpty() {
    return backingSet.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    F backingObj = toBacking.apply((T) o);
    return backingSet.contains(backingObj);
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return Iterators.transform(backingSet.iterator(), fromBacking::apply);
  }

  @NotNull
  @Override
  public Object[] toArray() {
    return backingSet.toArray();
  }

  @NotNull
  @Override
  public <T1> T1[] toArray(@NotNull T1[] a) {
    return backingSet.toArray(a);
  }

  @Override
  public boolean add(T t) {
    F backing = toBacking.apply(t);
    return backingSet.add(backing);
  }

  @Override
  public boolean remove(Object o) {
    return backingSet.remove(toBacking.apply((T) o));
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c) {
    return backingSet.containsAll(
        Collections2.transform((Collection<T>) c, toBacking::apply)
    );
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends T> c) {
    return backingSet.addAll(
        Collections2.transform(c, toBacking::apply)
    );
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    return backingSet.retainAll(
        Collections2.transform((Collection<T>) c, toBacking::apply)
    );
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    return backingSet.removeAll(
        Collections2.transform((Collection<T>) c, toBacking::apply)
    );
  }

  @Override
  public void clear() {
    backingSet.clear();
  }
}