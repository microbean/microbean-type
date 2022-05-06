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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

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
 * <p>This is a <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class CdiType extends JavaType {


  /*
   * Instance fields.
   */


  private final Function<? super Type, ? extends Collection<? extends Type>> directSupertypesFunction;


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
  private CdiType(final Type type, final Function<? super Type, ? extends Collection<? extends Type>> directSupertypesFunction) {
    super(type, true);
    this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
  }


  /*
   * Instance methods.
   */



  /**
   * If this {@link CdiType} represents a primitive type, returns a
   * {@link CdiType} representing the corresponding "wrapper type", or
   * this {@link CdiType} itself in all other cases.
   *
   * @return a {@link CdiType} representing the corresponding "wrapper
   * type" where appropriate, or {@code this}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see JavaType#box()
   */
  @Override // JavaType
  public CdiType box() {
    final Type type = this.object();
    if (type == void.class) {
      return of(Void.class, this.directSupertypesFunction);
    } else if (type == int.class) {
      // This is such a ridiculously common case we avoid the map lookup
      return of(Integer.class, this.directSupertypesFunction);
    } else if (type instanceof Class<?> c && c.isPrimitive()) {
      return of(wrapperTypes.get(c), this.directSupertypesFunction);
    }
    return this;
  }

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
  public Collection<? extends JavaType> directSupertypes() {
    final Collection<? extends Type> directSupertypes = this.directSupertypesFunction.apply(this.object());
    if (directSupertypes != null && !directSupertypes.isEmpty()) {
      final Collection<JavaType> c = new ArrayList<>(directSupertypes.size());
      for (final Type type : directSupertypes) {
        c.add(of(type, this.directSupertypesFunction));
      }
      return Collections.unmodifiableCollection(c);
    }
    return List.of();
  }

  /**
   * If this {@link CdiType} represents a {@link ParameterizedType} or
   * a {@link GenericArrayType} returns a {@link CdiType} representing
   * its {@linkplain ParameterizedType#getRawType() raw type} or
   * {@linkplain GenericArrayType#getGenericComponentType() generic
   * component type}, or, if this {@link CdiType} does not represent a
   * {@link ParameterizedType} or a {@link GenericArrayType}, returns
   * {@code this}.
   *
   * @return a suitable {@link CdiType}; never {@code null}; often
   * {@code this}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   */
  @Override // JavaType
  public CdiType type() {
    final Type type = this.object();
    if (type instanceof ParameterizedType p) {
      return of(p.getRawType(), this.directSupertypesFunction);
    } else if (type instanceof GenericArrayType g) {
      return of(g.getGenericComponentType(), this.directSupertypesFunction);
    } else {
      return this;
    }
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s type
   * arguments.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link CdiType} models a
   * {@link ParameterizedType}.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s
   * {@linkplain ParameterizedType#getActualTypeArguments() type
   * arguments}; never {@code null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   */
  @Override // org.microbean.type.Type<Type>
  public List<? extends CdiType> typeArguments() {
    if (this.object() instanceof ParameterizedType p) {
      final Type[] typeArguments = p.getActualTypeArguments();
      final List<CdiType> typeArgumentsList = new ArrayList<>(typeArguments.length);
      for (final Type typeArgument : typeArguments) {
        typeArgumentsList.add(of(typeArgument, this.directSupertypesFunction));
      }
      return Collections.unmodifiableList(typeArgumentsList);
    }
    return List.of();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s type
   * parameters.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link CdiType} models a generic
   * {@link Class}.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s
   * {@linkplain Class#getTypeParameters() type parameters}; never
   * {@code null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   */
  @Override // org.microbean.type.Type<Type>
  public List<? extends CdiType> typeParameters() {
    if (this.object() instanceof Class<?> c) {
      final Type[] typeParameters = c.getTypeParameters();
      if (typeParameters.length > 0) {
        final List<CdiType> typeParametersList = new ArrayList<>(typeParameters.length);
        for (final Type typeParameter : typeParameters) {
          typeParametersList.add(of(typeParameter, this.directSupertypesFunction));
        }
        return Collections.unmodifiableList(typeParametersList);
      }
    }
    return List.of();
  }

  /**
   * Returns the {@linkplain Class#getComponentType() component type}
   * of this {@link CdiType}, if there is one, <strong>or {@code
   * null} if there is not</strong>.
   *
   * <p>This method returns a non-{@code null} result only when this
   * {@link CdiType} represents a {@link Type} that is either
   * {@linkplain Class#isArray() an array} or a {@link
   * GenericArrayType}.</p>
   *
   * @return the {@linkplain Class#getComponentType() component type}
   * of this {@link CdiType}, if there is one, or {@code null} if
   * there is not
   *
   * @nullability This method and its overrides may return null.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see Class#getComponentType()
   *
   * @see GenericArrayType#getGenericComponentType()
   */
  @Override // org.microbean.type.Type<Type>
  public CdiType componentType() {
    final Type newType;
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      newType = c.getComponentType();
    } else if (type instanceof GenericArrayType g) {
      newType = g.getGenericComponentType();
    } else {
      newType = null;
    }
    if (newType == null) {
      return null;
    }
    return of(newType, this.directSupertypesFunction);
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s lower
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a {@link
   * WildcardType} {@linkplain WildcardType#getLowerBounds() with a
   * lower bound}.</p>
   *
   * <p>The returned {@link List} will contain at most one
   * element.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see WildcardType#getLowerBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public List<? extends CdiType> lowerBounds() {
    final Type type = this.object();
    if (type instanceof WildcardType w) {
      final Type[] lowerBounds = w.getLowerBounds();
      if (lowerBounds.length > 0) {
        final List<CdiType> lowerBoundsList = new ArrayList<>(lowerBounds.length);
        for (final Type lowerBound : lowerBounds) {
          lowerBoundsList.add(of(lowerBound, this.directSupertypesFunction));
        }
        return Collections.unmodifiableList(lowerBoundsList);
      }
    }
    return List.of();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s upper
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models either a {@link
   * WildcardType} or a {@link TypeVariable}.
   *
   * <p>The returned {@link List} will contain at most one
   * element.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link CdiType}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see TypeVariable#getBounds()
   *
   * @see WildcardType#getUpperBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public List<? extends CdiType> upperBounds() {
    final Type type = this.object();
    if (type instanceof WildcardType w) {
      return List.of(of(w.getUpperBounds()[0], this.directSupertypesFunction));
    } else if (type instanceof TypeVariable<?> t) {
      final Type[] upperBounds = t.getBounds();
      switch (upperBounds.length) {
      case 0:
        throw new AssertionError();
      case 1:
        return List.of(of(upperBounds[0], this.directSupertypesFunction));
      default:
        final List<CdiType> upperBoundsList = new ArrayList<>(upperBounds.length);
        for (final Type upperBound : upperBounds) {
          upperBoundsList.add(of(upperBound, this.directSupertypesFunction));
        }
        return Collections.unmodifiableList(upperBoundsList);
      }
    } else {
      return List.of();
    }
  }

  /**
   * Returns all the supertypes of this {@link CdiType} (which
   * includes this {@link CdiType}).
   *
   * <p>This method reflexively and transitively applies the direct
   * supertype relation (represented by the {@link
   * #directSupertypes()} method) to this {@link CdiType} and returns
   * an {@linkplain Collections#unmodifiableCollection(Collection)
   * unmodifiable <code>Collection</code>} containing the result.</p>
   *
   * <p>Overrides which alter this algorithm may result in undefined
   * behavior.  Typically overriding is necessary only to refine the
   * return type of this method.</p>
   *
   * @return an {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable
   * <code>Collection</code>} containing all the supertypes of this
   * {@link Type} (which includes this {@link CdiType}); never {@code
   * null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #directSupertypes()
   */
  @SuppressWarnings("unchecked")
  public Collection<? extends CdiType> supertypes() {
    return (Collection<? extends CdiType>)super.supertypes();
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
    return of(type.type(), JavaTypes::directSupertypes);
  }

  /**
   * Creates a new {@link CdiType}.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @param directSupertypesFunction a {@link Function} that, when
   * supplied with a {@link Type} (which will be the {@code type}
   * parameter value), returns those {@link Type}s which are its
   * direct supertypes; must not be {@code null}
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
  public static final CdiType of(final Token<?> type,
                                 final Function<? super Type, ? extends Collection<? extends Type>> directSupertypesFunction) {
    return of(type.type(), directSupertypesFunction);
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
      throw new IllegalArgumentException("!box; boxing is required by CDI");
    }
    return of(type.type(), JavaTypes::directSupertypes);
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
    return of(type, JavaTypes::directSupertypes);
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
    return of(type, JavaTypes::directSupertypes);
  }

  /**
   * Returns a {@link CdiType} representing the supplied {@code type}.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @param directSupertypesFunction a {@link Function} that, when
   * supplied with a {@link Type} (which will be the {@code type}
   * parameter value), returns those {@link Type}s which are its
   * direct supertypes; must not be {@code null}
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
  public static final CdiType of(final Type type,
                                 final Function<? super Type, ? extends Collection<? extends Type>> directSupertypesFunction) {
    return new CdiType(type, directSupertypesFunction);
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
    return of(type, t -> List.of());
  }

}
