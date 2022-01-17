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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import java.util.function.Predicate;

import java.util.stream.Stream;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SUBSIZED;

public final class JavaTypeSet implements Iterable<Type> {

  private final Set<JavaType> set;

  private volatile Type mostSpecializedClass;

  private volatile Type mostSpecializedInterface;

  private JavaTypeSet(final Type type) {
    super();
    this.set = Set.of(JavaType.of(type));
  }

  private JavaTypeSet(final Type t0, final Type t1) {
    super();
    this.set = Set.of(JavaType.of(t0), JavaType.of(t1));
  }

  private JavaTypeSet(final JavaType javaType) {
    super();
    this.set = Set.of(javaType);
  }

  private JavaTypeSet(final Collection<?> types) {
    super();
    if (types == null || types.isEmpty()) {
      this.set = Set.of();
    } else {
      final Set<JavaType> set = new HashSet<>(8);
      for (final Object type : types) {
        if (type instanceof JavaType jt) {
          set.add(jt);
        } else if (type instanceof Type t) {
          set.add(JavaType.of(t));
        }
      }
      this.set = Collections.unmodifiableSet(set);
    }
  }

   /**
   * Returns an arbitrarily selected {@link Type} that is guaranteed
   * to represent the most specialized subclass drawn from the types
   * in this {@link JavaTypeSet}.
   *
   * <p>For example, if a {@link JavaTypeSet} contains {@link Integer
   * Integer.class}, {@link Number Number.class} and {@link Object
   * Object.class}, then this method will return {@link Integer
   * Integer.class}.  If a {@link JavaTypeSet} contains {@link Integer
   * Integer.class} and {@link String String.class}, then either
   * {@link Integer Integer.class} or {@link String String.class} will
   * be returned.</p>
   *
   * <p>In practice, most {@link JavaTypeSet}s in normal use contain
   * classes from a single inheritance hierarchy.</p>
   *
   * @return an arbitrarily selected {@link Type} that is guaranteed
   * to represent the most specialized subclass in its type hierarchy
   * drawn from the types in this {@link JavaTypeSet}; never {@code
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
   * @see #mostSpecializedInterface()
   */
  public final Type mostSpecializedClass() {
    Type mostSpecializedClass = this.mostSpecializedClass;
    if (mostSpecializedClass == NullType.INSTANCE) {
      return null;
    } else if (mostSpecializedClass == null) {
      mostSpecializedClass = this.mostSpecialized(JavaTypeSet::isNonInterfaceClassType);
      this.mostSpecializedClass = mostSpecializedClass == null ? NullType.INSTANCE : mostSpecializedClass;
    }
    return mostSpecializedClass;
  }

  /**
   * Returns an arbitrarily selected {@link Type} that is guaranteed
   * to be a {@link Type} representing the most specialized interface
   * drawn from types representing interfaces in this {@link
   * JavaTypeSet}, or {@code null} if this {@link JavaTypeSet}
   * contains no interfaces.
   *
   * <p>The {@link Class} that is represented indirectly by the return
   * value, if any, is guaranteed to return {@code true} from its
   * {@link Class#isInterface()} method.</p>
   *
   * <p>For example, if a {@link JavaTypeSet} {@linkplain
   * #contains(Type) contains} {@link java.io.Closeable
   * Closeable.class} and {@link AutoCloseable AutoCloseable.class},
   * then this method will return {@link java.io.Closeable
   * Closeable.class}.  If a {@link JavaTypeSet} {@linkplain
   * #contains(Type) contains} {@link AutoCloseable
   * AutoCloseable.class} and {@link java.io.Serializable
   * Serializable.class}, then either {@link AutoCloseable
   * AutoCloseable.class} or {@link java.io.Serializable
   * Serializable.class} will be returned.</p>
   *
   * @return an arbitrarily selected {@link Type} that is guaranteed
   * to be a {@link Type} representing the most specialized interface
   * drawn from types representing interfaces in this {@link
   * JavaTypeSet}, or {@code null}; the {@link Type} returned is
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
   * @see #mostSpecializedClass()
   */
  public final Type mostSpecializedInterface() {
    Type mostSpecializedInterface = this.mostSpecializedInterface;
    if (mostSpecializedInterface == NullType.INSTANCE) {
      return null;
    } else if (mostSpecializedInterface == null) {
      mostSpecializedInterface = this.mostSpecialized(JavaTypeSet::isInterfaceType);
      this.mostSpecializedInterface = mostSpecializedInterface == null ? NullType.INSTANCE : mostSpecializedInterface;
    }
    return mostSpecializedInterface;
  }

  private final Type mostSpecialized(final Predicate<? super Type> p) {
    Type candidate = null;
    for (final JavaType javaType : this.set) {
      final Type type = javaType.type();
      if (candidate == null) {
        if (p.test(type)) {
          candidate = type;
        }
      } else {
        for (final Type supertype : JavaTypes.supertypes(type)) {
          if (p.test(supertype) && JavaTypes.supertype(candidate, supertype)) {
            candidate = supertype;
          }
        }
      }
    }
    return candidate;
  }

  public final boolean contains(final Type type) {
    return this.set.contains(JavaType.of(type));
  }

  public final boolean isEmpty() {
    return this.set.isEmpty();
  }

  public final int size() {
    return this.set.size();
  }

  @Override // Iterable<Type>
  public final Iterator<Type> iterator() {
    if (this.set.isEmpty()) {
      return Collections.emptyIterator();
    } else {
      return new TypeIterator(this.set.iterator());
    }
  }

  public final Stream<Type> stream() {
    return this.set.stream().map(JavaType::type);
  }

  @Override // Iterable<Type>
  public final Spliterator<Type> spliterator() {
    if (this.set.isEmpty()) {
      return Spliterators.emptySpliterator();
    } else {
      return
        Spliterators.spliterator(this.iterator(),
                                 this.size(),
                                 DISTINCT | IMMUTABLE | NONNULL | SIZED | SUBSIZED);
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
      final JavaTypeSet her = (JavaTypeSet)other;
      return this.set.equals(her.set);
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


  public static final JavaTypeSet of(final Type t) {
    return new JavaTypeSet(t);
  }

  public static final JavaTypeSet of(final Type t0, final Type t1) {
    return new JavaTypeSet(t0, t1);
  }

  public static final JavaTypeSet of(final JavaType t) {
    return new JavaTypeSet(t);
  }

  public static final JavaTypeSet of(final Collection<?> types) {
    return new JavaTypeSet(types);
  }

  private static final boolean isNonInterfaceClassType(final Type t) {
    final Class<?> c = JavaTypes.erase(t);
    return c != null && !c.isInterface();
  }

  private static final boolean isInterfaceType(final Type t) {
    final Class<?> c = JavaTypes.erase(t);
    return c != null && c.isInterface();
  }


  /*
   * Inner and nested classes.
   */


  private static final class TypeIterator implements Iterator<Type> {

    private final Iterator<? extends JavaType> i;

    private TypeIterator(final Iterator<? extends JavaType> i) {
      super();
      this.i = i;
    }

    @Override // Iterator<Type>
    public final boolean hasNext() {
      return this.i.hasNext();
    }

    @Override // Iterator<Type>
    public final Type next() {
      return this.i.next().type();
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
