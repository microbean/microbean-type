/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import java.util.function.Predicate;

import java.util.stream.Stream;

import org.microbean.development.annotation.Convenience;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SUBSIZED;

/**
 * An immutable {@link AbstractSet} of {@link Type}s that ensures that
 * {@link Type} implementations from different vendors use the same
 * equality semantics.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class NewJavaTypeSet extends AbstractSet<Type> {


  /*
   * Instance fields.
   */


  private final Set<? extends NewJavaType> set;

  private volatile Type mostSpecializedNonInterfaceType;

  private volatile Type mostSpecializedInterfaceType;


  /*
   * Constructors.
   */


  private NewJavaTypeSet(final Type type) {
    this(NewJavaType.of(type));
  }

  private NewJavaTypeSet(final NewJavaType javaType) {
    super();
    this.set = Set.of(javaType);
  }

  private NewJavaTypeSet(final Collection<?> types) {
    super();
    final int size = types == null ? 0 : types.size();
    if (size <= 0) {
      this.set = Set.of();
    } else if (size == 1) {
      final Object o = types instanceof List<?> list ? list.get(0) : types.iterator().next();
      if (o instanceof NewJavaType jt) {
        this.set = Set.of(jt);
      } else if (o instanceof Type t) {
        this.set = Set.of(NewJavaType.of(t));
      } else {
        this.set = Set.of();
      }
    } else {
      final Set<NewJavaType> set = new LinkedHashSet<>(8); // LinkedHashSet is critical for ordering
      for (final Object type : types) {
        if (type instanceof NewJavaType jt) {
          set.add(jt);
        } else if (type instanceof Type t) {
          set.add(NewJavaType.of(t));
        }
      }
      this.set = Collections.unmodifiableSet(set);
    }
  }


  /*
   * Instance methods.
   */


  public final NewJavaTypeSet nonInterfaceTypes() {
    return new NewJavaTypeSet(this.set.stream()
                              .filter(NewJavaTypeSet::nonInterfaceType)
                              .toList());
  }

  public final NewJavaTypeSet interfaceTypes() {
    return new NewJavaTypeSet(this.set.stream()
                              .filter(NewJavaTypeSet::interfaceType)
                              .toList());
  }

  @Override // Set<Type>
  public final boolean add(final Type type) {
    throw new UnsupportedOperationException();
  }

  @Override // Set<Type>
  public final boolean addAll(final Collection<? extends Type> c) {
    throw new UnsupportedOperationException();
  }

  @Override // Set<Type>
  public final void clear() {
    throw new UnsupportedOperationException();
  }

  @Override // Set<Type>
  public final boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  @Override // Set<Type>
  public final boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override // Set<Type>
  public final boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns an arbitrarily selected {@link Type} that is guaranteed
   * to represent the most specialized subclass drawn from the types
   * in this {@link NewJavaTypeSet}.
   *
   * <p>For example, if a {@link NewJavaTypeSet} contains {@link
   * Integer Integer.class}, {@link Number Number.class} and {@link
   * Object Object.class}, then this method will return {@link Integer
   * Integer.class}.  If a {@link NewJavaTypeSet} contains {@link
   * Integer Integer.class} and {@link String String.class}, then
   * either {@link Integer Integer.class} or {@link String
   * String.class} will be returned.</p>
   *
   * <p>In practice, most {@link NewJavaTypeSet}s in normal use
   * contain classes from a single inheritance hierarchy.</p>
   *
   * @return an arbitrarily selected {@link Type} that is guaranteed
   * to represent the most specialized subclass in its type hierarchy
   * drawn from the types in this {@link NewJavaTypeSet}; never {@code
   * null}; the {@link Type} returned is guaranteed to be either a
   * {@link Class} or a {@link java.lang.reflect.ParameterizedType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but deterministic only in
   * certain cases.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #mostSpecializedInterfaceType()
   */
  public final Type mostSpecializedNonInterfaceType() {
    Type mostSpecializedNonInterfaceType = this.mostSpecializedNonInterfaceType;
    if (mostSpecializedNonInterfaceType == NullType.INSTANCE) {
      return null;
    } else if (mostSpecializedNonInterfaceType == null) {
      mostSpecializedNonInterfaceType = this.mostSpecialized(NewJavaTypeSet::nonInterfaceType);
      this.mostSpecializedNonInterfaceType = mostSpecializedNonInterfaceType == null ? NullType.INSTANCE : mostSpecializedNonInterfaceType;
    }
    return mostSpecializedNonInterfaceType;
  }

  /**
   * Returns an arbitrarily selected {@link Type} that is guaranteed
   * to be a {@link Type} representing the most specialized interface
   * drawn from types representing interfaces in this {@link
   * NewJavaTypeSet}, or {@code null} if this {@link NewJavaTypeSet}
   * contains no interfaces.
   *
   * <p>The {@link Class} that is represented indirectly by the return
   * value, if any, is guaranteed to return {@code true} from its
   * {@link Class#isInterface()} method.</p>
   *
   * <p>For example, if a {@link NewJavaTypeSet} {@linkplain
   * #contains(Type) contains} {@link java.io.Closeable
   * Closeable.class} and {@link AutoCloseable AutoCloseable.class},
   * then this method will return {@link java.io.Closeable
   * Closeable.class}.  If a {@link NewJavaTypeSet} {@linkplain
   * #contains(Type) contains} {@link AutoCloseable
   * AutoCloseable.class} and {@link java.io.Serializable
   * Serializable.class}, then either {@link AutoCloseable
   * AutoCloseable.class} or {@link java.io.Serializable
   * Serializable.class} will be returned.</p>
   *
   * @return an arbitrarily selected {@link Type} that is guaranteed
   * to be a {@link Type} representing the most specialized interface
   * drawn from types representing interfaces in this {@link
   * NewJavaTypeSet}, or {@code null}; the {@link Type} returned is
   * guaranteed to be either a {@link Class} or a {@link
   * java.lang.reflect.ParameterizedType}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent but deterministic only in
   * certain cases.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #mostSpecializedNonInterfaceType()
   */
  public final Type mostSpecializedInterfaceType() {
    Type mostSpecializedInterfaceType = this.mostSpecializedInterfaceType;
    if (mostSpecializedInterfaceType == NullType.INSTANCE) {
      return null;
    } else if (mostSpecializedInterfaceType == null) {
      mostSpecializedInterfaceType = this.mostSpecialized(NewJavaTypeSet::interfaceType);
      this.mostSpecializedInterfaceType = mostSpecializedInterfaceType == null ? NullType.INSTANCE : mostSpecializedInterfaceType;
    }
    return mostSpecializedInterfaceType;
  }

  private final Type mostSpecialized(final Predicate<? super Type> p) {
    Type candidate = null;
    for (final NewJavaType javaType : this.set) {
      final Type type = javaType.object();
      if (candidate == null) {
        if (p.test(type)) {
          candidate = type;
        }
      } else {
        for (final Type supertype : JavaTypes.supertypes(type)) {
          if (p.test(supertype) && this.set.contains(NewJavaType.of(supertype)) && JavaTypes.supertype(candidate, supertype)) {
            candidate = supertype;
          }
        }
      }
    }
    return candidate;
  }

  @Override // Set<Type>
  public final boolean contains(final Object o) {
    return o instanceof Type t && this.set.contains(NewJavaType.of(t));
  }

  @Override // Set<Type>
  public final boolean isEmpty() {
    return this.set.isEmpty();
  }

  @Override // Set<Type>
  public final int size() {
    return this.set.size();
  }

  @Override // Set<Type>
  public final Iterator<Type> iterator() {
    if (this.set.isEmpty()) {
      return Collections.emptyIterator();
    } else {
      return new TypeIterator(this.set.iterator());
    }
  }

  @Override // Set<Type>
  public final Stream<Type> stream() {
    return this.set.stream().map(NewJavaType::object);
  }

  @Override // Set<Type>
  public final Spliterator<Type> spliterator() {
    if (this.set.isEmpty()) {
      return Spliterators.emptySpliterator();
    } else {
      return
        Spliterators.spliterator(this.iterator(),
                                 this.size(),
                                 DISTINCT | IMMUTABLE | NONNULL | ORDERED | SIZED | SUBSIZED);
    }
  }

  @Override // Object
  public final int hashCode() {
    return this.set.hashCode();
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && this.getClass() == other.getClass()) {
      return this.set.equals(((NewJavaTypeSet)other).set);
    } else {
      return false;
    }
  }

  @Override // Object
  public final String toString() {
    return this.set.toString();
  }


  /*
   * Static methods.
   */


  @Convenience
  public static final NewJavaTypeSet of(final Type t) {
    return of(NewJavaType.of(t));
  }

  @Convenience
  public static final NewJavaTypeSet of(final Type t0, final Type t1) {
    return of(List.of(NewJavaType.of(t0), NewJavaType.of(t1)));
  }

  public static final NewJavaTypeSet of(final NewJavaType t) {
    return new NewJavaTypeSet(t);
  }

  @Convenience
  public static final NewJavaTypeSet of(final NewJavaType t0, final NewJavaType t1) {
    return of(List.of(t0, t1));
  }

  public static final NewJavaTypeSet of(final Collection<?> types) {
    return new NewJavaTypeSet(types);
  }

  @Convenience
  public static final NewJavaTypeSet ofSupertypes(final Type t) {
    return of(JavaTypes.supertypes(t));
  }

  @Convenience
  public static final NewJavaTypeSet ofSupertypes(final NewJavaType jt) {
    return ofSupertypes(jt.type());
  }

  private static final boolean nonInterfaceType(final NewJavaType t) {
    return nonInterfaceType(t.type());
  }

  private static final boolean nonInterfaceType(final Type t) {
    final Class<?> c = JavaTypes.erase(t);
    return c != null && !c.isInterface();
  }

  private static final boolean interfaceType(final NewJavaType t) {
    return interfaceType(t.type());
  }

  private static final boolean interfaceType(final Type t) {
    final Class<?> c = JavaTypes.erase(t);
    return c != null && c.isInterface();
  }


  /*
   * Inner and nested classes.
   */


  private static final class TypeIterator implements Iterator<Type> {

    private final Iterator<? extends NewJavaType> i;

    private TypeIterator(final Iterator<? extends NewJavaType> i) {
      super();
      this.i = i;
    }

    @Override // Iterator<Type>
    public final boolean hasNext() {
      return this.i.hasNext();
    }

    @Override // Iterator<Type>
    public final Type next() {
      return this.i.next().object();
    }

    @Override // Iterator<Type>
    public final void remove() {
      throw new UnsupportedOperationException();
    }

  }


  private static final class NullType implements Type {

    private static final Type INSTANCE = new NullType();

    private NullType() {
      super();
    }

  }

}
