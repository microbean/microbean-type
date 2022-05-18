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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import java.lang.invoke.VarHandle;

import java.lang.reflect.Type;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
 * An immutable {@link AbstractSet} of {@link Type
 * java.lang.reflect.Type}s that ensures that {@link Type
 * java.lang.reflect.Type} implementations from different vendors use
 * the same equality semantics.
 *
 * <p><strong>This is not a set of {@link JavaType}
 * instances.</strong> See the {@link #javaTypeList()} and {@link
 * #javaTypeSet()} methods.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #javaTypeList()
 *
 * @see #javaTypeSet()
 *
 * @deprecated Now that {@link org.microbean.type.Type} instances can
 * {@linkplain org.microbean.type.Type#equals(org.microbean.type.Type,
 * org.microbean.type.Type) can be compared for equality}, there is
 * not much call for this class.
 */
@Deprecated
public final class JavaTypeSet extends AbstractSet<Type> {


  /*
   * Static fields.
   */


  private static final JavaTypeSet EMPTY_JAVA_TYPE_SET = new JavaTypeSet();

  private static final VarHandle MOST_SPECIALIZED_INTERFACE_TYPE;

  private static final VarHandle MOST_SPECIALIZED_NON_INTERFACE_TYPE;

  static {
    final Lookup lookup = MethodHandles.lookup();
    try {
      MOST_SPECIALIZED_INTERFACE_TYPE = lookup.findVarHandle(JavaTypeSet.class, "mostSpecializedInterfaceType", Type.class);
      MOST_SPECIALIZED_NON_INTERFACE_TYPE = lookup.findVarHandle(JavaTypeSet.class, "mostSpecializedNonInterfaceType", Type.class);
    } catch (final NoSuchFieldException | IllegalAccessException reflectiveOperationException) {
      throw (Error)new ExceptionInInitializerError(reflectiveOperationException.getMessage()).initCause(reflectiveOperationException);
    }
  }


  /*
   * Instance fields.
   */


  private final Set<JavaType> set;

  private volatile Type mostSpecializedNonInterfaceType;

  private volatile Type mostSpecializedInterfaceType;


  /*
   * Constructors.
   */


  private JavaTypeSet() {
    super();
    this.set = Set.of();
  }

  private JavaTypeSet(final Type type) {
    this(JavaType.of(type));
  }

  private JavaTypeSet(final org.microbean.type.Type<? extends Type> t) {
    super();
    this.set = Set.of(t instanceof JavaType jt ? jt : JavaType.of(t.object()));
  }

  private JavaTypeSet(final Collection<?> types) {
    super();
    final int size = types == null || types.isEmpty() ? 0 : types.size();
    switch (size) {
    case 0:
      this.set = Set.of();
      break;
    case 1:
      final Object o = types instanceof List<?> list ? list.get(0) : types.iterator().next();
      if (o instanceof JavaType jt) {
        this.set = Set.of(jt);
      } else if (o instanceof Type t) {
        this.set = Set.of(JavaType.of(t));
      } else if (o instanceof org.microbean.type.Type<?> t) {
        final Object modeledType = t.object();
        if (modeledType instanceof Type) {
          this.set = Set.of(JavaType.of((Type)modeledType));
        } else {
          this.set = Set.of();
        }
      } else {
        this.set = Set.of();
      }
      break;
    default:
      final Set<JavaType> set = new LinkedHashSet<>(8); // LinkedHashSet is critical for ordering
      for (final Object type : types) {
        if (type instanceof JavaType jt) {
          set.add(jt);
        } else if (type instanceof Type t) {
          set.add(JavaType.of(t));
        } else if (type instanceof org.microbean.type.Type<?> t) {
          final Object modeledType = t.object();
          if (modeledType instanceof Type) {
            set.add(JavaType.of((Type)modeledType));
          }
        }
      }
      this.set = Collections.unmodifiableSet(set);
      break;
    }
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a {@link List} of {@link JavaType}s representing the
   * elements logically stored by this {@link JavaTypeSet}.
   *
   * <p>The returned {@link List} is unmodifiable and does not contain
   * {@code null} or duplicate elements.  Its iteration order is the
   * same as that of this {@link JavaTypeSet}.</p>
   *
   * @return a {@link List} of {@link JavaType}s representing the
   * elements logically stored by this {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final List<JavaType> javaTypeList() {
    return List.copyOf(this.set);
  }

  /**
   * Returns a {@link Set} of {@link JavaType}s representing the
   * elements logically stored by this {@link JavaTypeSet}.
   *
   * <p>The returned {@link Set} is unmodifiable and does not contain
   * {@code null} elements.  Its iteration order is the same as that
   * of this {@link JavaTypeSet}.</p>
   *
   * @return a {@link Set} of {@link JavaType}s representing the
   * elements logically stored by this {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final Set<JavaType> javaTypeSet() {
    return this.set;
  }

  /**
   * Returns a {@linkplain org.microbean.type.Type#customSupertyped()
   * custom supertyped} {@link JavaType} representing this {@link
   * JavaTypeSet}.
   *
   * @return a {@linkplain org.microbean.type.Type#customSupertyped()
   * custom supertyped} {@link JavaType} representing this {@link
   * JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see org.microbean.type.Type#customSupertyped()
   *
   * @see #javaType(boolean)
   */
  public final JavaType javaType() {
    return this.javaType(false);
  }

  /**
   * Returns a {@linkplain org.microbean.type.Type#customSupertyped()
   * custom supertyped} {@link JavaType} representing this {@link
   * JavaTypeSet}.
   *
   * @param box whether autoboxing is in effect
   *
   * @return a {@linkplain org.microbean.type.Type#customSupertyped()
   * custom supertyped} {@link JavaType} representing this {@link
   * JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see org.microbean.type.Type#customSupertyped()
   *
   * @see #javaTypeList()
   *
   * @see JavaType#ofExactly(boolean, List)
   */
  public final JavaType javaType(final boolean box) {
    return JavaType.ofExactly(box, this.javaTypeList());
  }

  /**
   * Returns a new {@link JavaTypeSet} containing only the {@link
   * Type}s that are not {@linkplain Class#isInterface() interface
   * types} from this {@link JavaTypeSet}.
   *
   * @return a new {@link JavaTypeSet} containing only the {@link
   * Type}s that are not {@linkplain Class#isInterface() interface
   * types} from this {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final JavaTypeSet nonInterfaceTypes() {
    return new JavaTypeSet(this.set.stream()
                           .filter(JavaTypeSet::nonInterfaceType)
                           .toList());
  }

  /**
   * Returns a new {@link JavaTypeSet} containing only the {@linkplain
   * Class#isInterface() interface types} from this {@link
   * JavaTypeSet}.
   *
   * @return a new {@link JavaTypeSet} containing only the {@linkplain
   * Class#isInterface() interface types} from this {@link
   * JavaTypeSet}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final JavaTypeSet interfaceTypes() {
    return new JavaTypeSet(this.set.stream()
                           .filter(JavaTypeSet::interfaceType)
                           .toList());
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param type ignored
   *
   * @return false in all cases
   *
   * @exception UnsupportedOperationException when invoked
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Set<Type>
  public final boolean add(final Type type) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param c ignored
   *
   * @return false in all cases
   *
   * @exception UnsupportedOperationException when invoked
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Set<Type>
  public final boolean addAll(final Collection<? extends Type> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Set<Type>
  public final void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param o ignored
   *
   * @return false in all cases
   *
   * @exception UnsupportedOperationException when invoked
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Set<Type>
  public final boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param c ignored
   *
   * @return false in all cases
   *
   * @exception UnsupportedOperationException when invoked
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Set<Type>
  public final boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @param c ignored
   *
   * @return false in all cases
   *
   * @exception UnsupportedOperationException when invoked
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Set<Type>
  public final boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns an arbitrarily selected {@link Type} that is guaranteed
   * to represent the most specialized subclass drawn from the types
   * in this {@link JavaTypeSet}.
   *
   * <p>For example, if a {@link JavaTypeSet} contains {@link
   * Integer Integer.class}, {@link Number Number.class} and {@link
   * Object Object.class}, then this method will return {@link Integer
   * Integer.class}.  If a {@link JavaTypeSet} contains {@link
   * Integer Integer.class} and {@link String String.class}, then
   * either {@link Integer Integer.class} or {@link String
   * String.class} will be returned.</p>
   *
   * <p>In practice, most {@link JavaTypeSet}s in normal use
   * contain classes from a single inheritance hierarchy.</p>
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
   * @see #mostSpecializedInterfaceType()
   *
   * @deprecated See {@link
   * org.microbean.type.Type#mostSpecialized(Predicate)}
   */
  @Deprecated
  public final Type mostSpecializedNonInterfaceType() {
    Type mostSpecializedNonInterfaceType = this.mostSpecializedNonInterfaceType; // volatile read
    if (mostSpecializedNonInterfaceType == null) {
      mostSpecializedNonInterfaceType = this.mostSpecialized(JavaTypeSet::nonInterfaceType);
      if (!MOST_SPECIALIZED_NON_INTERFACE_TYPE.compareAndSet(this, null, mostSpecializedNonInterfaceType)) { // volatile write
        return this.mostSpecializedNonInterfaceType; // volatile read
      }
    }
    return mostSpecializedNonInterfaceType == NullType.INSTANCE ? null : mostSpecializedNonInterfaceType;
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
   * @see #mostSpecializedNonInterfaceType()
   *
   * @deprecated See {@link
   * org.microbean.type.Type#mostSpecialized(Predicate)}
   */
  @Deprecated
  public final Type mostSpecializedInterfaceType() {
    Type mostSpecializedInterfaceType = this.mostSpecializedInterfaceType; // volatile read
    if (mostSpecializedInterfaceType == null) {
      mostSpecializedInterfaceType = this.mostSpecialized(JavaTypeSet::interfaceType);
      if (!MOST_SPECIALIZED_INTERFACE_TYPE.compareAndSet(this, null, mostSpecializedInterfaceType)) { // volatile write
        return this.mostSpecializedInterfaceType; // volatile read
      }
    }
    return mostSpecializedInterfaceType == NullType.INSTANCE ? null : mostSpecializedInterfaceType;
  }

  private final Type mostSpecialized(final Predicate<? super Type> p) {
    Type candidate = null;
    for (final JavaType javaType : this.set) {
      final Type type = javaType.object();
      if (candidate == null) {
        if (p.test(type)) {
          candidate = type;
        }
      } else {
        for (final Type supertype : JavaTypes.supertypes(type)) {
          if (p.test(supertype) && this.set.contains(JavaType.of(supertype)) && JavaTypes.supertype(candidate, supertype)) {
            candidate = supertype;
          }
        }
      }
    }
    return candidate;
  }

  @Override // Set<Type>
  public final boolean contains(final Object o) {
    return o instanceof Type t ? this.set.contains(JavaType.of(t)) : this.set.contains(o);
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
    return this.set.isEmpty() ? Collections.emptyIterator() : new TypeIterator(this.set.iterator());
  }

  /**
   * Returns an {@link Iterator} of {@link JavaType} instances
   * representing the elements stored by this {@link JavaTypeSet}.
   *
   * @return an {@link Iterator} of {@link JavaType} instances
   * representing the elements stored by this {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final Iterator<JavaType> javaTypeIterator() {
    return this.set.iterator();
  }

  @Override // Set<Type>
  public final Stream<Type> stream() {
    return this.set.stream().map(JavaType::object);
  }

  /**
   * Returns a {@link Stream} of {@link JavaType} instances
   * representing the elements stored by this {@link JavaTypeSet}.
   *
   * @return a {@link Stream} of {@link JavaType} instances
   * representing the elements stored by this {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final Stream<JavaType> javaTypeStream() {
    return this.set.stream();
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

  /**
   * Returns a {@link Spliterator} of {@link JavaType} instances
   * representing the elements stored by this {@link JavaTypeSet}.
   *
   * @return a {@link Spliterator} of {@link JavaType} instances
   * representing the elements stored by this {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public final Spliterator<JavaType> javaTypeSpliterator() {
    return this.set.spliterator();
  }


  /*
   * Static methods.
   */


  /**
   * Returns an {@linkplain #isEmpty() empty} {@link JavaTypeSet}.
   *
   * @return an {@linkplain #isEmpty() empty} {@link JavaTypeSet}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Convenience
  public static final JavaTypeSet of() {
    return EMPTY_JAVA_TYPE_SET;
  }

  /**
   * Returns a {@link JavaTypeSet} whose sole element models the
   * supplied {@link Type}.
   *
   * @param t the {@link Type} in question; must not be {@code
   * null}
   *
   * @return a {@link JavaTypeSet} whose sole element models the
   * supplied {@link Type}; never {@code null}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(JavaType)
   *
   * @see JavaType#of(Type)
   */
  @Convenience
  public static final JavaTypeSet of(final Type t) {
    return of(JavaType.of(t));
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link Type}s.
   *
   * @param t0 one of the {@link Type}s in question; must not be
   * {@code null}
   *
   * @param t1 one of the {@link Type}s in question; must not be
   * {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link Type}s; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Collection)
   *
   * @see JavaType#of(Type)
   */
  @Convenience
  public static final JavaTypeSet of(final Type t0, final Type t1) {
    return of(List.of(JavaType.of(t0), JavaType.of(t1)));
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link Type}s.
   *
   * @param types the {@link Type}s in question; must not be {@code
   * null}
   *
   * @return a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link Type}s; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Collection)
   *
   * @see JavaType#of(Type)
   */
  @Convenience
  public static final JavaTypeSet of(final Type... types) {
    if (types.length == 0) {
      return of();
    }
    return of(Arrays.asList(types));
  }

  /**
   * Returns a {@link JavaTypeSet} whose sole element is the supplied
   * {@link org.microbean.type.Type}.
   *
   * @param t the {@link org.microbean.type.Type} in question; must
   * not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose sole element is the supplied
   * {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final JavaTypeSet of(final org.microbean.type.Type<? extends Type> t) {
    return new JavaTypeSet(t);
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are the supplied
   * {@link org.microbean.type.Type}s.
   *
   * @param t0 one of the {@link org.microbean.type.Type}s in
   * question; must not be {@code null}
   *
   * @param t1 one of the {@link org.microbean.type.Type}s in
   * question; must not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link org.microbean.type.Type}s; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Collection)
   */
  @Convenience
  public static final JavaTypeSet of(final org.microbean.type.Type<? extends Type> t0,
                                     final org.microbean.type.Type<? extends Type> t1) {
    return of(List.of(t0, t1));
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link org.microbean.type.Type}s.
   *
   * @param types the {@link org.microbean.type.Type}s in question;
   * must not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are modeled by the
   * supplied {@link org.microbean.type.Type}s; never {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Collection)
   */
  @Convenience
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static final JavaTypeSet of(final org.microbean.type.Type<? extends Type>... types) {
    if (types.length == 0) {
      return of();
    }
    return of(Arrays.asList(types));
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are drawn from the
   * supplied {@link Collection}, in its {@linkplain
   * Collection#iterator() iteration order}.
   *
   * @param types the {@link Collection} in question; must not be
   * {@code null}; only its elements that are instances of either
   * {@link Type} or {@link JavaType} or {@link
   * org.microbean.type.Type org.microbean.type.Type&lt;? extends
   * Type&gt;} will be considered
   *
   * @return a {@link JavaTypeSet} whose elements are drawn from the
   * supplied {@link Collection}, in its {@linkplain
   * Collection#iterator() iteration order}; never {@code null}
   *
   * @exception NullPointerException if {@code types} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final JavaTypeSet of(final Collection<?> types) {
    return new JavaTypeSet(types);
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes} of the {@link Type}
   * modeled by the supplied {@link org.microbean.type.Type} (which
   * include the modeled {@link Type} itself).
   *
   * @param t the {@link org.microbean.type.Type} in question; must
   * not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes} of the {@link Type}
   * modeled by the supplied {@link org.microbean.type.Type} (which
   * include the modeled {@link Type} itself); never {@code null}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofSupertypes(org.microbean.type.Type, Predicate)
   */
  @Convenience
  public static final JavaTypeSet ofSupertypes(final org.microbean.type.Type<? extends Type> t) {
    return ofSupertypes(t.object(), JavaTypes::acceptAll);
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes} of the {@link Type}
   * modeled by the supplied {@link org.microbean.type.Type} (which
   * include the modeled {@link Type} itself).
   *
   * @param t the {@link org.microbean.type.Type} in question; must
   * not be {@code null}
   *
   * @param acceptancePredicate a {@link Predicate} controlling
   * membership of a {@link Type} in the returned {@link JavaTypeSet};
   * must not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes} of the {@link Type}
   * modeled by the supplied {@link org.microbean.type.Type} (which
   * include the modeled {@link Type} itself); never {@code null}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofSupertypes(org.microbean.type.Type, Predicate)
   */
  @Convenience
  public static final JavaTypeSet ofSupertypes(final org.microbean.type.Type<? extends Type> t,
                                               final Predicate<? super Type> acceptancePredicate) {
    return ofSupertypes(t.object(), acceptancePredicate);
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes of the supplied
   * <code>Type</code>} (which include the supplied {@link Type}
   * itself).
   *
   * @param t the {@link Type} in question; must not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes of the supplied
   * <code>Type</code>} (which include the supplied {@link Type}
   * itself)
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofSupertypes(Type, Predicate)
   */
  @Convenience
  public static final JavaTypeSet ofSupertypes(final Type t) {
    return ofSupertypes(t, JavaTypes::acceptAll);
  }

  /**
   * Returns a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes of the supplied
   * <code>Type</code>} (which include the supplied {@link Type}
   * itself), gated by the supplied {@link Predicate}.
   *
   * @param t the {@link Type} in question; must not be {@code null}
   *
   * @param acceptancePredicate a {@link Predicate} controlling
   * membership of a {@link Type} in the returned {@link Collection};
   * must not be {@code null}
   *
   * @return a {@link JavaTypeSet} whose elements are the {@linkplain
   * JavaTypes#supertypes(Type) supertypes of the supplied
   * <code>Type</code>} (which include the supplied {@link Type}
   * itself)
   *
   * @exception NullPointerException if either {@code t} or {@code
   * acceptancePredicate} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Collection)
   *
   * @see JavaTypes#supertypes(Type)
   */
  @Convenience
  public static final JavaTypeSet ofSupertypes(final Type t, final Predicate<? super Type> acceptancePredicate) {
    return of(JavaTypes.supertypes(t, acceptancePredicate));
  }

  private static final boolean nonInterfaceType(final JavaType t) {
    return nonInterfaceType(t.type());
  }

  private static final boolean nonInterfaceType(final Type t) {
    final Class<?> c = JavaTypes.erase(t);
    return c != null && !c.isInterface();
  }

  private static final boolean interfaceType(final JavaType t) {
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

    private final Iterator<? extends JavaType> i;

    private TypeIterator(final Iterator<? extends JavaType> i) {
      super();
      this.i = Objects.requireNonNull(i, "i");
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
      this.i.remove();
    }

  }


  private static final class NullType implements Type {

    private static final Type INSTANCE = new NullType();

    private NullType() {
      super();
    }

  }

}
