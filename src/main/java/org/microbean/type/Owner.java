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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.microbean.development.annotation.OverridingEncouraged;

/**
 * An interface whose implementations represent a Java {@linkplain
 * java.lang.reflect.Type type} or a Java {@linkplain
 * java.lang.reflect.Executable executable} for equality comparison
 * purposes and no other.
 *
 * @param <T> the type describing the type representation type
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public interface Owner<T> {

  /**
   * Returns the {@link Owner} of this {@link Owner}, or {@code null}
   * if there is no such {@link Owner}.
   *
   * @return the {@link Owner} of this {@link Owner}, or {@code null}
   *
   * @nullability Implementations of this method may, and often will,
   * return {@code null}.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   */
  public Owner<T> owner();

  /**
   * Returns the object this {@link Owner} is modeling, or {@code
   * null} if that information is not supplied by the implementation.
   *
   * <p><strong>The default implementation of this method returns
   * {@code null}.</strong> {@linkplain OverridingEncouraged
   * Overriding is encouraged}.</p>
   *
   * @return the object this {@link Owner} is modeling, or {@code
   * null} if that information is not supplied by the implementation
   *
   * @nullability Implementations of this method may return {@code
   * null}.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   */
  @OverridingEncouraged
  public default Object object() {
    return null;
  }

  /**
   * Returns {@code true} if and only if this {@link Owner} represents
   * either a Java type that has a name, or an {@linkplain
   * java.lang.reflect.Executable executable}.
   *
   * <p>In the Java reflective type system, only {@link Class}, {@link
   * java.lang.reflect.TypeVariable}, and {@link
   * java.lang.reflect.Executable} instances have names.</p>
   *
   * @return {@code true} if and only if this {@link Owner} represents
   * either a Java type that has a name, or an {@linkplain
   * java.lang.reflect.Executable executable}
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @see #name()
   *
   * @see Class#getName()
   *
   * @see java.lang.reflect.TypeVariable#getName()
   */
  @OverridingEncouraged
  public default boolean named() {
    return this.name() != null;
  }

  /**
   * Returns the name of this {@link Owner} if it has one <strong>or
   * {@code null} if it does not</strong>.
   *
   * <p>Only classes (including interfaces) and type variables in the
   * Java reflective type system have names.  {@linkplain
   * java.lang.reflect.Executable Owners representing executables} do
   * too.</p>
   *
   * @return the name of this {@link Owner}, or {@code null}
   *
   * @nullability Implementations of this method may, and often will,
   * return {@code null}.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   */
  public String name();

  /**
   * If this {@link Owner} represents a parameterized type or a
   * generic array type, returns a {@link Type} representing its raw
   * type or generic component type, or, if this {@link Owner}
   * represents an {@linkplain java.lang.reflect.Constructor
   * constructor}, returns a {@link Type} representing {@code void} or
   * {@link Void}, or, if this {@link Owner} represents a {@linkplain
   * java.lang.reflect.Method method}, returns a {@link Type}
   * representing its {@linkplain
   * java.lang.reflect.Method#getGenericReturnType() generic return
   * type}, or returns {@code this} (the {@link Owner} in this case
   * will be known to be a {@link Type}).
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return a suitable {@link Type}; never {@code null}; often {@code
   * this}
   *
   * @nullability Implementations of this method must not return
   * {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   */
  public Type<T> type();

  /**
   * Returns {@code true} if and only if this {@link Owner} represents
   * an {@linkplain java.lang.reflect.Executable executable} by virtue
   * of having parameters.
   *
   * <p>This implementation calls the {@link #parameters()} method and
   * returns {@code true} if the resulting {@link List} is not {@code
   * null} and {@linkplain List#isEmpty() is not empty}.  Subclasses
   * are encouraged to provide a faster implementation.</p>
   *
   * @return {@code true} if and only if this {@link Owner} represents
   * an {@linkplain java.lang.reflect.Executable executable} by virtue
   * of having parameters
   *
   * @see #parameters()
   */
  @OverridingEncouraged
  public default boolean hasParameters() {
    final Collection<?> p = this.parameters();
    return p != null;
  }

  /**
   * Returns an {@linkplain
   * java.util.Collections#unmodifiableList(List) unmodifiable
   * <code>List</code>} of this {@link Owner}'s parameters, if this
   * {@link Owner} represents an {@linkplain
   * java.lang.reflect.Executable executable}, or {@code null} if it
   * does not.
   *
   * @return an {@linkplain
   * java.util.Collections#unmodifiableList(List) unmodifiable
   * <code>List</code>} of this {@link Owner}'s parameters, if this
   * {@link Owner} represents an {@linkplain
   * java.lang.reflect.Executable executable}, or {@code null} if it
   * does not
   *
   * @nullability Implementations of this method may, and often will,
   * return {@code null}.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   */
  public List<? extends Type<T>> parameters();

  /**
   * Returns {@code true} if and only if this {@link Owner} represents
   * a generic class or a generic {@linkplain
   * java.lang.reflect.Executable executable} by virtue of having type
   * parameters.
   *
   * <p>This implementation calls the {@link #typeParameters()} method
   * and returns {@code true} if the resulting {@link List} is not
   * {@code null} and {@linkplain List#isEmpty() not empty}.
   * Subclasses are encouraged to provide a faster implementation.</p>
   *
   * @return {@code true} if and only if this {@link Owner} represents
   * a generic class or a generic {@linkplain
   * java.lang.reflect.Executable executable} by virtue of having type
   * parameters
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @see #typeParameters()
   */
  @OverridingEncouraged
  public default boolean hasTypeParameters() {
    return !this.typeParameters().isEmpty();
  }

  /**
   * Returns an {@linkplain
   * java.util.Collections#unmodifiableList(List) unmodifiable
   * <code>List</code>} of this {@link Owner}'s type parameters.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a generic
   * class or a generic {@linkplain java.lang.reflect.Executable
   * executable}.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return an {@linkplain
   * java.util.Collections#unmodifiableList(List) unmodifiable
   * <code>List</code>} of this {@link Type}'s type parameters; never
   * {@code null}
   *
   * @nullability Implementations of this method must not return
   * {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   */
  public List<? extends Type<T>> typeParameters();

  /**
   * Returns {@code true} if this {@link Owner} implementation is
   * equal to the supplied {@link Object}.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if this {@link Owner} implementation is
   * equal to the supplied {@link Object}
   */
  @Override // Object
  public boolean equals(final Object other);

  /**
   * Returns {@code true} if and only if the supplied {@link Object}
   * is equal in some way to this {@link Owner}'s {@linkplain
   * #object() object}.
   *
   * @param other the object to test; may be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Object}
   * is equal in some way to this {@link Owner}'s {@linkplain
   * #object() object}
   *
   * @see #object()
   */
  public default boolean objectEquals(final Object other) {
    return Objects.equals(this.object(), other);
  }

}
