/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.type;

import java.lang.reflect.Type;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;

/**
 * An {@linkplain Collections#unmodifiableSet(Set) unmodifiable}
 * {@link AbstractSet} of {@link Type}s with additional convenient
 * functionality.
 *
 * <p>{@linkplain #iterator() Iteration} over any {@link TypeSet}
 * instance is guaranteed to be in some deterministic but otherwise
 * unspecified order.  The order is subject to change between versions
 * of this class.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class TypeSet extends AbstractSet<Type> {


  /*
   * Instance fields.
   */


  private final SortedSet<Type> typeSet;

  private volatile SortedSet<Class<?>> classes;

  private volatile SortedSet<Class<?>> interfaces;

  private volatile SortedSet<Class<?>> rawTypes;

  private volatile Class<?> mostSpecializedInterface;

  private volatile Class<?> mostSpecializedClass;


  /*
   * Constructors.
   */


  /**
   * Creates a forever-{@linkplain #isEmpty() empty} {@link TypeSet}.
   */
  public TypeSet() {
    this(Collections.emptySet());
  }

  /**
   * Creates a {@link TypeSet} that {@linkplain #contains(Object)
   * contains} only the supplied {@link Type} unless the supplied
   * {@link Type} is {@code null}, in which case the resulting {@link
   * TypeSet} will be forever {@linkplain #isEmpty() empty}.
   *
   * @param type the {@link Type} the new {@link TypeSet} will
   * contain; may be {@code null} in which case the new {@link
   * TypeSet} will be {@linkplain #isEmpty() empty}
   */
  public TypeSet(final Type type) {
    this(type == null ? Collections.emptySet() : Collections.singleton(type));
  }

  /**
   * Creates a {@link TypeSet} that {@linkplain #contains(Object)
   * contains} only the {@link Type}s supplied unless the supplied
   * {@link Collection} is {@code null} or {@linkplain
   * Collection#isEmpty() empty}, in which case the resulting {@link
   * TypeSet} will be forever {@linkplain #isEmpty() empty}.
   *
   * @param types the {@link Type}s the new {@link TypeSet} will
   * contain; may be {@code null} or {@linkplain Collection#isEmpty()
   * empty} in which case the new {@link TypeSet} will be forever
   * {@linkplain #isEmpty() empty}
   */
  public TypeSet(final Collection<? extends Type> types) {
    super();
    if (types == null || types.isEmpty()) {
      this.typeSet = Collections.emptySortedSet();
    } else {
      final SortedSet<Type> sortedTypeSet = new TreeSet<>(Types.typeNameComparator);
      sortedTypeSet.addAll(types);
      this.typeSet = Collections.unmodifiableSortedSet(sortedTypeSet);
    }
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable} {@link SortedSet}
   * of all raw types contained directly or indirectly by this {@link
   * TypeSet}.
   *
   * <p>This method calls the {@link #getClasses()} and {@link
   * #getInterfaces()} methods as part of its operation.</p>
   *
   * @return a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable} {@link SortedSet}
   * of all raw types contained directly or indirectly by this {@link
   * TypeSet}
   *
   * @see #getClasses()
   *
   * @see #getInterfaces()
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.  The
   * {@link Set} that is returned consists of elements in a
   * particular, but deliberately unspecified, order that is not
   * dependent upon the ordering of the {@link Set} of {@link Type}s
   * initially supplied to this {@link TypeSet}.
   */
  public final SortedSet<Class<?>> getRawTypes() {
    SortedSet<Class<?>> rawTypes = this.rawTypes;
    if (rawTypes == null) {
      final SortedSet<Class<?>> classes = this.getClasses();
      final SortedSet<Class<?>> interfaces = this.getInterfaces();
      if (classes.isEmpty()) {
        if (interfaces.isEmpty()) {
          rawTypes = Collections.emptySortedSet();
        } else {
          rawTypes = interfaces;
        }
      } else if (interfaces.isEmpty()) {
        rawTypes = classes;
      } else {
        final SortedSet<Class<?>> target = new TreeSet<>(Types.typeNameComparator);
        target.addAll(classes);
        target.addAll(interfaces);
        rawTypes = Collections.unmodifiableSortedSet(target);
      }
      this.rawTypes = rawTypes;
    }
    return rawTypes;
  }

  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable} {@link SortedSet}
   * of all {@link Class}es (not interfaces) contained directly or
   * indirectly by this {@link TypeSet}.
   *
   * @return a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable} {@link Set} of all
   * {@link Class}es (not interfaces) contained directly or indirectly
   * by this {@link TypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.  The
   * {@link Set} that is returned consists of elements in a
   * particular, but deliberately unspecified, order that is not
   * dependent upon the ordering of the {@link Set} of {@link Type}s
   * initially supplied to this {@link TypeSet}.
   */
  public final SortedSet<Class<?>> getClasses() {
    SortedSet<Class<?>> classes = this.classes;
    if (classes == null) {
      if (this.isEmpty()) {
        classes = Collections.emptySortedSet();
      } else {
        classes = new TreeSet<>(Types.typeNameComparator);
        for (final Type type : this) {
          Class<?> rawType = Types.toClass(type);
          while (rawType != null) {
            if (!rawType.isInterface()) {
              classes.add(rawType);
            }
            rawType = rawType.getSuperclass();
          }
        }
        if (classes.isEmpty()) {
          classes = Collections.emptySortedSet();
        } else {
          classes = Collections.unmodifiableSortedSet(classes);
        }
      }
      assert classes != null;
      this.classes = classes;
    }
    return classes;
  }

  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable} {@link Set} of all
   * interfaces contained directly or indirectly by this {@link
   * TypeSet}.
   *
   * @return a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable} {@link Set} of all
   * interfaces contained directly or indirectly by this {@link
   * TypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.  The
   * {@link Set} that is returned consists of elements in a
   * particular, but deliberately unspecified, order that is not
   * dependent upon the ordering of the {@link Set} of {@link Type}s
   * initially supplied to this {@link TypeSet}.
   */
  public final SortedSet<Class<?>> getInterfaces() {
    SortedSet<Class<?>> interfaces = this.interfaces;
    if (interfaces == null) {
      if (this.isEmpty()) {
        interfaces = Collections.emptySortedSet();
      } else {
        interfaces = new TreeSet<>(Types.typeNameComparator);
        for (final Type type : this) {
          TypeSet.getInterfaces(type, interfaces); // adds to interfaces
        }
        if (interfaces.isEmpty()) {
          interfaces = Collections.emptySortedSet();
        } else {
          interfaces = Collections.unmodifiableSortedSet(interfaces);
        }
      }
      this.interfaces = interfaces;
    }
    return interfaces;
  }

  /**
   * Returns an arbitrarily selected {@link Class} that is guaranteed
   * to be the most specialized interface in its type hierarchy drawn
   * from the {@linkplain #getInterfaces() interfaces} in this {@link
   * TypeSet}, or {@code null} if this {@link TypeSet} contains no
   * interfaces.
   *
   * <p>The {@link Class} that is returned is guaranteed to return
   * {@code true} from its {@link Class#isInterface()} method.</p>
   *
   * <p>For example, if a {@link TypeSet} contains {@link
   * java.io.Closeable Closeable.class} and {@link AutoCloseable
   * AutoCloseable.class}, then this method will return {@link
   * java.io.Closeable Closeable.class}.  If a {@link TypeSet}
   * contains {@link AutoCloseable AutoCloseable.class} and {@link
   * java.io.Serializable Serializable.class}, then either {@link
   * AutoCloseable AutoCloseable.class} or {@link java.io.Serializable
   * Serializable.class} will be returned.</p>
   *
   * @return an arbitrarily selected {@link Class} that is guaranteed
   * to be the most specialized interface in its type hierarchy drawn
   * from the {@linkplain #getInterfaces() interfaces} in this {@link
   * TypeSet}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #getMostSpecializedClass()
   */
  public final Class<?> getMostSpecializedInterface() {
    final Class<?> mostSpecializedInterface = this.mostSpecializedInterface;
    final Class<?> returnValue;
    if (mostSpecializedInterface == null) {
      final Set<Class<?>> interfaces = this.getInterfaces();
      if (interfaces == null || interfaces.isEmpty()) {
        this.mostSpecializedInterface = Object.class; // marker
        returnValue = null;
      } else {
        final Iterator<? extends Class<?>> interfacesIterator = interfaces.iterator();
        Class<?> candidate = interfacesIterator.next();
        while (interfacesIterator.hasNext()) {
          Class<?> intrface = interfacesIterator.next();
          if (candidate.isAssignableFrom(intrface)) {
            candidate = intrface;
          }
        }
        this.mostSpecializedInterface = returnValue = candidate;
      }
    } else if (mostSpecializedInterface.equals(Object.class)) {
      // marker
      returnValue = null;
    } else {
      returnValue = mostSpecializedInterface;
    }
    return returnValue;
  }

  /**
   * Returns an arbitrarily selected {@link Class} that is guaranteed
   * to be the most specialized subclass in its type hierarchy drawn
   * from the {@linkplain #getClasses() classes} in this {@link
   * TypeSet}.
   *
   * <p>For example, if a {@link TypeSet} contains {@link Integer
   * Integer.class}, {@link Number Number.class} and {@link Object
   * Object.class}, then this method will return {@link Integer
   * Integer.class}.  If a {@link TypeSet} contains {@link Integer
   * Integer.class} and {@link String String.class}, then either
   * {@link Integer Integer.class} or {@link String String.class} will
   * be returned.</p>
   *
   * <p>In practice, most {@link TypeSet}s in normal use contain
   * classes from a single inheritance hierarchy.</p>
   *
   * @return an arbitrarily selected {@link Class} that is guaranteed
   * to be the most specialized subclass in its type hierarchy drawn
   * from the {@linkplain #getClasses() classes} in this {@link
   * TypeSet}; never {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #getMostSpecializedInterface()
   */
  public final Class<?> getMostSpecializedClass() {
    final Class<?> mostSpecializedClass = this.mostSpecializedClass;
    final Class<?> returnValue;
    if (mostSpecializedClass == null) {
      final Set<Class<?>> classes = this.getClasses();
      if (classes == null || classes.isEmpty()) {
        this.mostSpecializedClass = returnValue = Object.class;
      } else {
        final Iterator<? extends Class<?>> classesIterator = classes.iterator();
        assert classesIterator.hasNext();
        Class<?> candidateClass = classesIterator.next();
        assert candidateClass != null;
        assert !candidateClass.isInterface();
        while (classesIterator.hasNext()) {
          final Class<?> cls = classesIterator.next();
          assert cls != null;
          assert !cls.isInterface();
          if (candidateClass.isAssignableFrom(cls)) {
            candidateClass = cls;
          }
        }
        this.mostSpecializedClass = returnValue = candidateClass;
      }
    } else {
      returnValue = mostSpecializedClass;
    }
    assert returnValue != null;
    return returnValue;
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param type ignored
   *
   * @return Not applicable
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final boolean add(final Type type) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param types ignored
   *
   * @return Not applicable
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final boolean addAll(final Collection<? extends Type> types) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean contains(final Object object) {
    return !this.isEmpty() && this.typeSet.contains(object);
  }

  @Override
  public final boolean containsAll(final Collection<?> objects) {
    return !this.isEmpty() && this.typeSet.containsAll(objects);
  }

  @Override
  public final boolean isEmpty() {
    return this.typeSet.isEmpty();
  }

  @Override
  public final Iterator<Type> iterator() {
    return this.typeSet.iterator();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param object ignored
   *
   * @return Not applicable
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public boolean remove(final Object object) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param objects ignored
   *
   * @return Not applicable
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public boolean removeAll(final Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param objects ignored
   *
   * @return Not applicable
   *
   * @exception UnsupportedOperationException when invoked
   */  
  @Override
  public boolean retainAll(final Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final int size() {
    return this.typeSet.size();
  }

  @Override
  public final Spliterator<Type> spliterator() {
    return this.typeSet.spliterator();
  }

  @Override
  public final Object[] toArray() {
    return this.typeSet.toArray();
  }

  @Override
  public final <T> T[] toArray(final T[] array) {
    return this.typeSet.toArray(array);
  }

  @Override
  public String toString() {
    return this.typeSet.toString();
  }


  /*
   * Static methods.
   */


  private static final void getInterfaces(final Type type, final Set<Class<?>> interfaces) {
    Class<?> cls = Types.toClass(type);
    if (cls.isInterface()) {
      interfaces.add(cls);
    }
    while (cls != null) {
      for (final Class<?> intrface : cls.getInterfaces()) {
        if (interfaces.add(intrface)) {
          TypeSet.getInterfaces(intrface, interfaces); // XXX recursive
        }
      }
      cls = cls.getSuperclass();
    }
  }

}
