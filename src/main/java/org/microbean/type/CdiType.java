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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.function.Function;

/**
 * A {@link JavaType} with a flexible means of describing its direct
 * supertypes, suitable only, probaly, for CDI use cases.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class CdiType extends JavaType {


  /*
   * Instance fields.
   */


  private final Function<Type, Collection<Type>> directSupertypesFunction;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link CdiType}.
   *
   * @param type the {@link Type} being modeled; must not be {@code
   * null}; must be a <a
   * href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#legal_bean_types"
   * target="_parent">legal CDI bean type</a>
   *
   * @param directSupertypesFunction a {@link Function} that, when
   * supplied with a {@link Type} (which will be the {@code type}
   * parameter value), returns those {@link Type}s which are its
   * direct supertypes; must not be {@code null}
   *
   * @exception NullPointerException if any argument is {@code null}
   */
  public CdiType(final Type type, final Function<Type, Collection<Type>> directSupertypesFunction) {
    super(type);
    this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
  }


  /*
   * Instance methods.
   */


  /**
   * Returns an {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable and
   * immutable <code>Collection</code>} of the <em>direct
   * supertypes</em> of this {@link CdiType} as provided by the
   * {@linkplain #CdiType(Type, Function)
   * <code>directSupertypesFunction</code> supplied at construction
   * time}.
   *
   * @return an {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable and
   * immutable <code>Collection</code>} of the <em>direct
   * supertypes</em> of this {@link JavaType}; never {@code null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic, though the ordering of elements
   * within returned {@link Collection}s is undefined
   */
  @Override
  public Collection<JavaType> directSupertypes() {
    final Collection<Type> directSupertypes = this.directSupertypesFunction.apply(this.object());
    if (directSupertypes != null && !directSupertypes.isEmpty()) {
      final Collection<JavaType> c = new ArrayList<>(directSupertypes.size());
      for (final Type type : directSupertypes) {
        c.add(of(type));
      }
      return Collections.unmodifiableCollection(c);
    }
    return List.of();
  }


  /*
   * Static methods.
   */


  /**
   * Creates a new {@link CdiType}.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @return a new {@link CdiType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it returns a new {@link CdiType} with each invocation).
   * However, any {@link CdiType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * CdiType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type)
   */
  public static final CdiType of(final Token<?> type) {
    return CdiType.of(type.type());
  }

  /**
   * Creates a new {@link CdiType}.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @param box whether autoboxing is enabled; <strong>must be {@code
   * true} because CDI requires autoboxing</strong>; see {@link
   * JavaType#of(Token, boolean)} which this method shadows
   *
   * @return a new {@link CdiType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @exception IllegalArgumentException if {@code box} is {@code
   * false}; CDI requires autoboxing
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it returns a new {@link CdiType} with each invocation).
   * However, any {@link CdiType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * CdiType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type)
   */
  public static final CdiType of(final Token<?> type, final boolean box) {
    if (!box) {
      throw new IllegalArgumentException("boxing is required by CDI");
    }
    return CdiType.of(type.type());
  }

  /**
   * Returns a {@link CdiType} representing the supplied {@code type}.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @return a {@link CdiType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link CdiType} with each invocation).
   * However, any {@link CdiType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * CdiType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final CdiType of(final Type type) {
    return new CdiType(type, JavaTypes::directSupertypes);
  }

  /**
   * Returns a {@link CdiType} <strong>with no direct
   * supertypes</strong> representing the supplied {@code type}.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @return a {@link CdiType} <strong>with no direct
   * supertypes</strong>; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link CdiType} with each invocation).
   * However, any {@link CdiType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * CdiType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final CdiType ofExact(final Type type) {
    return new CdiType(type, t -> List.of());
  }

  /**
   * Returns a {@link CdiType} representing the supplied {@code type}.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @param box whether autoboxing is enabled; <strong>must be {@code
   * true} because CDI requires autoboxing</strong>; see {@link
   * JavaType#of(Type, boolean)} which this method effectively shadows
   *
   * @return a {@link CdiType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @exception IllegalArgumentException if {@code box} is {@code
   * false}; CDI requires autoboxing
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link CdiType} with each invocation).
   * However, any {@link CdiType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * CdiType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type)
   */
  public static final CdiType of(final Type type, final boolean box) {
    if (!box) {
      throw new IllegalArgumentException("boxing is required by CDI");
    }
    return CdiType.of(type);
  }

}
