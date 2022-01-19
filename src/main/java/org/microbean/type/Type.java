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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.microbean.development.annotation.Convenience;

/**
 * An {@link Object} that represents a Java type of some kind.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #hashCode()
 *
 * @see #equals(Object)
 */
public interface Type {

  /**
   * Returns a hashcode for this {@link Type}.
   *
   * @return a hashcode for this type
   *
   * @idempotency Implementations of this method must be idempotent and
   * deterministic.
   *
   * @threadsafety Implementations of this method must be safe for concurrent
   * use by multiple threads.
   *
   * @see #equals(Object)
   */
  @Override
  public int hashCode();

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link Type}.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} must be returned
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link Type}; {@code false} otherwise
   *
   * @idempotency Implementations of this method must be idempotent and
   * deterministic.
   *
   * @threadsafety Implementations of this method must be safe for concurrent
   * use by multiple threads.
   *
   * @see #hashCode()
   */
  @Override
  public boolean equals(final Object other);


  /*
   * Nested classes.
   */


  /**
   * Assignability semantics for a type.
   *
   * @param <T> the type
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #assignable(Object, Object)
   *
   * @see InvariantSemantics
   *
   * @see CovariantSemantics
   */
  public static abstract class Semantics<T> {


    /*
     * Instance fields.
     */


    private final BiPredicate<T, T> equalityBiPredicate;

    private final Predicate<T> namedPredicate;

    private final UnaryOperator<T> boxFunction;

    private final Function<T, ? extends Type> toTypeFunction;

    private final UnaryOperator<T> typeFunction;

    private final Predicate<T> genericTypePredicate;

    private final Predicate<T> typeArgumentsPredicate;

    private final Function<T, T[]> typeArgumentsFunction;

    private final UnaryOperator<T> componentTypeFunction;

    private final Predicate<T> upperBoundsPredicate;

    private final Predicate<T> lowerBoundPredicate;

    private final UnaryOperator<T> lowerBoundFunction;

    private final Function<T, T[]> upperBoundsFunction;


    /*
     * Constructors.
     */


    protected Semantics(final Predicate<T> namedPredicate,
                        final BiPredicate<T, T> equalityBiPredicate,
                        final UnaryOperator<T> boxFunction,
                        final Function<T, ? extends Type> toTypeFunction,
                        final UnaryOperator<T> typeFunction,
                        final Predicate<T> genericTypePredicate,
                        final Predicate<T> typeArgumentsPredicate,
                        final Function<T, T[]> typeArgumentsFunction,
                        final UnaryOperator<T> componentTypeFunction,
                        final Predicate<T> upperBoundsPredicate,
                        final Function<T, T[]> upperBoundsFunction,
                        final Predicate<T> lowerBoundPredicate,
                        final UnaryOperator<T> lowerBoundFunction) {
      super();
      this.namedPredicate = Objects.requireNonNull(namedPredicate, "namedPredicate");
      this.equalityBiPredicate = Objects.requireNonNull(equalityBiPredicate, "equalityBiPredicate");
      this.boxFunction = boxFunction == null ? UnaryOperator.identity() : boxFunction;
      this.toTypeFunction = Objects.requireNonNull(toTypeFunction, "toTypeFunction");
      this.typeFunction = Objects.requireNonNull(typeFunction, "typeFunction");
      this.genericTypePredicate = Objects.requireNonNull(genericTypePredicate, "genericTypePredicate");
      this.typeArgumentsPredicate = Objects.requireNonNull(typeArgumentsPredicate, "typeArgumentsPredicate");
      this.typeArgumentsFunction = Objects.requireNonNull(typeArgumentsFunction, "typeArgumentsFunction");
      this.componentTypeFunction = Objects.requireNonNull(componentTypeFunction, "componentTypeFunction");
      this.upperBoundsPredicate = Objects.requireNonNull(upperBoundsPredicate, "upperBoundsPredicate");
      this.upperBoundsFunction = Objects.requireNonNull(upperBoundsFunction, "upperBoundsFunction");
      this.lowerBoundPredicate = Objects.requireNonNull(lowerBoundPredicate, "lowerBoundPredicate");
      this.lowerBoundFunction = Objects.requireNonNull(lowerBoundFunction, "lowerBoundFunction");
    }


    /*
     * Instance methods.
     */


    protected final T box(final T type) {
      return this.boxFunction.apply(type);
    }

    protected final boolean equals(final T receiverType, final T payloadType) {
      return this.equalityBiPredicate.test(receiverType, payloadType);
    }

    protected final boolean hasTypeParameters(final T type) {
      return this.genericTypePredicate.test(type);
    }

    protected final boolean hasTypeArguments(final T type) {
      return this.typeArgumentsPredicate.test(type);
    }

    protected final T[] typeArguments(final T type) {
      return this.typeArgumentsFunction.apply(type);
    }

    protected final boolean lowerBounded(final T type) {
      return this.lowerBoundPredicate.test(type);
    }

    protected final boolean upperBounded(final T type) {
      return this.upperBoundsPredicate.test(type);
    }

    protected final T lowerBound(final T type) {
      return this.lowerBoundFunction.apply(type);
    }

    protected final T[] upperBounds(final T type) {
      return this.upperBoundsFunction.apply(type);
    }

    protected final T componentType(final T type) {
      return this.componentTypeFunction.apply(type);
    }

    protected final boolean named(final T type) {
      return this.namedPredicate.test(type);
    }

    protected final T type(final T type) {
      return this.typeFunction.apply(type);
    }

    protected final Type toType(final T type) {
      return this.toTypeFunction.apply(type);
    }

    protected final boolean wildcard(final T type) {
      return !this.named(type) && (this.lowerBounded(type) || this.upperBounded(type));
    }

    protected final boolean typeVariable(final T type) {
      return this.named(type) && !this.lowerBounded(type) && this.upperBounded(type);
    }

    /**
     * Returns {@code true} if and only if a reference bearing a type
     * represented by the supplied {@code payloadType} can be assigned
     * to a reference bearing a type represented by the supplied
     * {@code receiverType}.
     *
     * @param receiverType the type to which a reference bearing a
     * type represented by the supplied {@code payloadType} is
     * potentially assignable; must not be {@code null}
     *
     * @param payloadType the type borne by a reference that is being
     * assigned to a reference bearing the supplied {@code
     * receiverType}; must not be {@code null}
     *
     * @return {@code true} if and only if a reference bearing a type
     * represented by the supplied {@code payloadType} can be assigned
     * to a reference bearing a type represented by the supplied
     * {@code receiverType}; {@code false} otherwise
     *
     * @exception NullPointerException if either argument is {@code
     * null}
     *
     * @idempotency This method is idempotent and deterministic.
     *
     * @threadsafety This method is safe for concurrent use by
     * multiple threads.
     */
    public boolean assignable(final T receiverType, final T payloadType) {
      if (this.equals(Objects.requireNonNull(receiverType, "receiverType"),
                      Objects.requireNonNull(payloadType, "payloadType"))) {
        return true;
      } else if (this.hasTypeArguments(receiverType)) {
        return parameterizedTypeIsAssignableFromType(receiverType, payloadType);
      } else if (this.hasTypeArguments(payloadType)) {
        return this.nonParameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else if (this.componentType(receiverType) == null) {
        if (this.lowerBounded(receiverType)) {
          return this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, true, payloadType);
        } else if (this.upperBounded(receiverType)) {
          if (this.named(receiverType)) {
            return this.typeVariableIsAssignableFromNonParameterizedType(receiverType, payloadType);
          } else {
            return this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, false, payloadType);
          }
        } else {
          return this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
        }
      } else if (this.type(receiverType) == receiverType) {
        return this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
      } else {
        return this.genericArrayTypeIsAssignableFromNonParameterizedType(receiverType, payloadType);
      }
    }


    private final boolean nonParameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      if (this.componentType(receiverType) == null) {
        if (this.lowerBounded(receiverType)) {
          return this.wildcardTypeIsAssignableFromParameterizedType(receiverType, true /* lower bounded */, payloadType);
        } else if (this.upperBounded(receiverType)) {
          if (this.named(receiverType)) {
            return this.typeVariableIsAssignableFromParameterizedType(receiverType, payloadType);
          } else {
            return this.wildcardTypeIsAssignableFromParameterizedType(receiverType, false /* upper bounded */, payloadType);
          }
        } else {
          return this.classIsAssignableFromParameterizedType(receiverType, payloadType);
        }
      } else if (this.type(receiverType) == receiverType) {
        return this.classIsAssignableFromParameterizedType(receiverType, payloadType);
      } else {
        return this.genericArrayTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      }
    }


    private final boolean classIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      if (this.componentType(payloadType) == null) {
        if (this.lowerBounded(payloadType)) {
          return this.classIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            return this.classIsAssignableFromTypeVariable(receiverType, payloadType);
          } else {
            return this.classIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          return this.classIsAssignableFromClass(receiverType, payloadType);
        }
      } else if (this.type(payloadType) == payloadType) {
        return this.classIsAssignableFromClass(receiverType, payloadType);
      } else {
        return this.classIsAssignableFromGenericArrayType(receiverType, payloadType);
      }
    }

    protected boolean classIsAssignableFromClass(T receiverType, T payloadType) {
      return false;
    }

    protected boolean classIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean classIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean classIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean classIsAssignableFromWildcardType(final T receiverType, final T payloadType, final boolean lowerBounded) {
      return false;
    }


    private final boolean parameterizedTypeIsAssignableFromType(final T receiverType, final T payloadType) {
      if (this.hasTypeArguments(payloadType)) {
        return this.parameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else if (this.componentType(payloadType) == null) {
        if (this.lowerBounded(payloadType)) {
          return this.parameterizedTypeIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            return this.parameterizedTypeIsAssignableFromTypeVariable(receiverType, payloadType);
          } else {
            return this.parameterizedTypeIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          return this.parameterizedTypeIsAssignableFromClass(receiverType, payloadType);
        }
      } else if (this.type(payloadType) == payloadType) {
        return this.parameterizedTypeIsAssignableFromClass(receiverType, payloadType);
      } else {
        return this.parameterizedTypeIsAssignableFromGenericArrayType(receiverType, payloadType);
      }
    }

    protected boolean parameterizedTypeIsAssignableFromClass(final T receiverType, final T payloadClass) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadParameterizedType) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromGenericArrayType(final T receiverParameterizedType, final T payloadGenericArrayType) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromWildcardType(final T receiverType,
                                                                    final T payloadType,
                                                                    final boolean lowerBounded) {
      return false;
    }


    private final boolean genericArrayTypeIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      if (this.componentType(payloadType) == null) {
        if (this.lowerBounded(payloadType)) {
          return this.genericArrayTypeIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            return this.genericArrayTypeIsAssignableFromTypeVariable(receiverType, payloadType);
          } else {
            return this.genericArrayTypeIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          return this.genericArrayTypeIsAssignableFromClass(receiverType, payloadType);
        }
      } else if (this.type(payloadType) == payloadType) {
        return this.genericArrayTypeIsAssignableFromClass(receiverType, payloadType);
      } else {
        return this.genericArrayTypeIsAssignableFromGenericArrayType(receiverType, payloadType);
      }
    }

    protected boolean genericArrayTypeIsAssignableFromClass(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromWildcardType(final T receiverType,
                                                                   final T payloadType,
                                                                   final boolean lowerBounded) {
      return false;
    }


    private final boolean typeVariableIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      if (this.componentType(payloadType) == null) {
        if (this.lowerBounded(payloadType)) {
          return this.typeVariableIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            return this.typeVariableIsAssignableFromTypeVariable(receiverType, payloadType);
          } else {
            return this.typeVariableIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          return this.typeVariableIsAssignableFromClass(receiverType, payloadType);
        }
      } else if (this.type(payloadType) == payloadType) {
        return this.typeVariableIsAssignableFromClass(receiverType, payloadType);
      } else {
        return this.typeVariableIsAssignableFromGenericArrayType(receiverType, payloadType);
      }
    }

    protected boolean typeVariableIsAssignableFromClass(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromWildcardType(final T receiverType,
                                                               final T payloadType,
                                                               final boolean lowerBounded) {
      return false;
    }


    private final boolean wildcardTypeIsAssignableFromNonParameterizedType(final T receiverType,
                                                                           final boolean lowerBounded,
                                                                           final T payloadType) {
      if (this.componentType(payloadType) == null) {
        if (this.lowerBounded(payloadType)) {
          return this.wildcardTypeIsAssignableFromWildcardType(receiverType, lowerBounded, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            return this.wildcardTypeIsAssignableFromTypeVariable(receiverType, lowerBounded, payloadType);
          } else {
            return this.wildcardTypeIsAssignableFromWildcardType(receiverType, lowerBounded, payloadType, false);
          }
        } else {
          return this.wildcardTypeIsAssignableFromClass(receiverType, lowerBounded, payloadType);
        }
      } else if (this.type(payloadType) == payloadType) {
        return this.wildcardTypeIsAssignableFromClass(receiverType, lowerBounded, payloadType);
      } else {
        return this.wildcardTypeIsAssignableFromGenericArrayType(receiverType, lowerBounded, payloadType);
      }
    }

    protected boolean wildcardTypeIsAssignableFromClass(final T receiverType, final boolean lowerBounded, final T payloadType) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverType,
                                                                    final boolean lowerBounded,
                                                                    final T payloadType) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverType,
                                                                   final boolean lowerBounded,
                                                                   final T payloadType) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverType,
                                                               final boolean lowerBounded,
                                                               final T payloadType) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromWildcardType(final T receiverType,
                                                               final boolean receiverTypeLowerBounded,
                                                               final T payloadType,
                                                               final boolean payloadTypeLowerBounded) {
      return false;
    }


    @Convenience
    protected static final <T> boolean unsupportedPredicate(final T ignored) {
      throw new UnsupportedOperationException();
    }

    @Convenience
    protected static final <T, U> U unsupportedFunction(final T ignored) {
      throw new UnsupportedOperationException();
    }

  }


  /**
   * A {@link Semantics} implementation that applies invariant
   * assignability semantics, except in the case of wildcard types,
   * which are by definition always covariant.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see CovariantSemantics
   */
  public static class InvariantSemantics<T> extends Semantics<T> {

    private final CovariantSemantics<T> wildcardSemantics;

    /**
     * Creates a new {@link InvariantSemantics}.
     *
     * @param wildcardSemantics a {@link CovariantSemantics} instance
     * for comparing wildcard type arguments; must not be {@code null}
     *
     * @exception NullPointerException if {@code wildcardSemantics} is
     * {@code null}
     */
    public InvariantSemantics(final CovariantSemantics<T> wildcardSemantics) {
      super(wildcardSemantics::named,
            wildcardSemantics::equals,
            wildcardSemantics::box,
            wildcardSemantics::toType,
            wildcardSemantics::type,
            wildcardSemantics::hasTypeParameters,
            wildcardSemantics::hasTypeArguments,
            wildcardSemantics::typeArguments,
            wildcardSemantics::componentType,
            wildcardSemantics::upperBounded,
            wildcardSemantics::upperBounds,
            wildcardSemantics::lowerBounded,
            wildcardSemantics::lowerBound);
      this.wildcardSemantics = Objects.requireNonNull(wildcardSemantics, "wildcardSemantics");
    }

    /**
     * Returns {@code true} if and only if a reference bearing a type
     * represented by the supplied {@code payloadType} {@linkplain
     * BiPredicate#test(Object, Object) is equal} to a reference
     * bearing a type represented by the supplied {@code
     * receiverType}.
     *
     * @param receiverType the type to which a reference bearing a
     * type represented by the supplied {@code payloadType} is
     * potentially assignable; must not be {@code null}
     *
     * @param payloadType the type borne by a reference that is being
     * assigned to a reference bearing the supplied {@code
     * receiverType}; must not be {@code null}
     *
     * @return {@code true} if and only if a reference bearing a type
     * represented by the supplied {@code payloadType} {@linkplain
     * BiPredicate#test(Object, Object) is equal} to a
     * reference bearing a type represented by the supplied {@code
     * receiverType}; {@code false} otherwise
     *
     * @exception NullPointerException if either argument is {@code
     * null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     */
    @Override // Semantics
    public boolean assignable(final T receiverType, final T payloadType) {
      if (this.wildcard(receiverType) || this.wildcard(payloadType)) {
        return this.wildcardSemantics.assignable(receiverType, payloadType);
      }
      return this.equals(receiverType, payloadType);
    }

  }


  /**
   * A {@link Semantics} implementation that applies covariant
   * assignability semantics.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see InvariantSemantics
   */
  public static class CovariantSemantics<T> extends Semantics<T> {


    private final Function<T, ? extends Collection<? extends T>> directSupertypesFunction;


    /*
     * Constructors.
     */


    public CovariantSemantics(final Predicate<T> namedPredicate,
                              final BiPredicate<T, T> equalityBiPredicate,
                              final UnaryOperator<T> boxFunction,
                              final Function<T, ? extends Type> toTypeFunction,
                              final Function<T, ? extends Collection<T>> directSupertypesFunction,
                              final UnaryOperator<T> typeFunction,
                              final Predicate<T> genericTypePredicate,
                              final Function<T, T[]> typeArgumentsFunction,
                              final UnaryOperator<T> componentTypeFunction,
                              final Function<T, T[]> upperBoundsFunction,
                              final UnaryOperator<T> lowerBoundFunction) {
      this(namedPredicate,
           equalityBiPredicate,
           boxFunction,
           toTypeFunction,
           directSupertypesFunction,
           typeFunction,
           genericTypePredicate,
           t -> typeArgumentsFunction.apply(t).length > 0,
           typeArgumentsFunction,
           componentTypeFunction,
           t -> upperBoundsFunction.apply(t).length > 0,
           upperBoundsFunction,
           t -> lowerBoundFunction.apply(t) != null,
           lowerBoundFunction);
    }

    /**
     * Creates a new {@link CovariantSemantics}.
     *
     * @param namedPredicate a {@link Predicate} to return whether a
     * type has a name; must not be {@code null}
     *
     * @param equalityBiPredicate a {@link BiPredicate} to determine
     * when two instances of a type are equal to each other; must not
     * be {@code null}
     *
     * @param boxFunction a {@link UnaryOperator} that returns either
     * its supplied operand (if boxing should not be applied) or a
     * boxed representation of its supplied operand (if boxing should
     * be applied); may be {@code null} in which case the return value
     * of {@link UnaryOperator#identity()} will be used instead
     *
     * @param toTypeFunction a {@link Function} that returns a
     * {@link Type} representing a type; must not be {@code null}
     *
     * @param directSupertypesFunction a {@link Function} that is
     * responsible for returning an array representing the <em>direct
     * supertypes</em> of a type; must not be {@code null}
     *
     * @param typeFunction a {@link UnaryOperator} responsible for
     * returning the raw type of a parameterized type; must not be
     * {@code null}
     */
    public CovariantSemantics(final Predicate<T> namedPredicate,
                              final BiPredicate<T, T> equalityBiPredicate,
                              final UnaryOperator<T> boxFunction,
                              final Function<T, ? extends Type> toTypeFunction,
                              final Function<T, ? extends Collection<? extends T>> directSupertypesFunction,
                              final UnaryOperator<T> typeFunction,
                              final Predicate<T> genericTypePredicate,
                              final Predicate<T> typeArgumentsPredicate,
                              final Function<T, T[]> typeArgumentsFunction,
                              final UnaryOperator<T> componentTypeFunction,
                              final Predicate<T> upperBoundsPredicate,
                              final Function<T, T[]> upperBoundsFunction,
                              final Predicate<T> lowerBoundPredicate,
                              final UnaryOperator<T> lowerBoundFunction) {
      super(namedPredicate,
            equalityBiPredicate,
            boxFunction,
            toTypeFunction,
            typeFunction,
            genericTypePredicate,
            typeArgumentsPredicate,
            typeArgumentsFunction,
            componentTypeFunction,
            upperBoundsPredicate,
            upperBoundsFunction,
            lowerBoundPredicate,
            lowerBoundFunction);
      this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
    }


    /*
     * Instance methods.
     */


    protected final Collection<? extends T> directSupertypes(final T type) {
      return this.directSupertypesFunction.apply(type);
    }

    protected final boolean supertype(final T sup, final T sub) {
      // Is sup a supertype of sub?
      if (this.equals(Objects.requireNonNull(sup, "sup"), Objects.requireNonNull(sub, "sub"))) {
        return true;
      } else {
        for (final T supertype : this.supertypes(sub)) {
          if (this.equals(supertype, sup)) {
            return true;
          }
        }
      }
      return false;
    }

    protected final Collection<? extends T> supertypes(final T type) {
      return this.supertypes(type, new HashSet<>(8)::add);
    }

    @SuppressWarnings("unchecked")
    private final Collection<? extends T> supertypes(final T type, final Predicate<? super Type> unseen) {
      if (unseen.test(this.toType(type))) {
        final Collection<? extends T> directSupertypes = this.directSupertypes(type);
        if (directSupertypes.isEmpty()) {
          return List.of(type); // the supertype relation is reflexive as well as transitive
        } else {
          final Collection<T> supertypes = new ArrayList<>(3 * directSupertypes.size() + 1);
          supertypes.add(type); // the supertype relation is reflexive as well as transitive
          for (final T directSupertype : directSupertypes) {
            for (final T supertype : this.supertypes(directSupertype, unseen)) {
              supertypes.add(supertype);
            }
          }
          return Collections.unmodifiableCollection(supertypes);
        }
      } else {
        return List.of();
      }
    }

    @Override
    protected boolean classIsAssignableFromClass(final T receiverClass, final T payloadClass) {
      return this.supertype(this.box(receiverClass), this.box(payloadClass));
    }

    @Override
    protected final boolean classIsAssignableFromParameterizedType(final T receiverClass, final T payloadParameterizedType) {
      return this.assignable(receiverClass, this.type(payloadParameterizedType));
    }

    @Override
    protected final boolean classIsAssignableFromGenericArrayType(final T receiverClass, final T payloadGenericArrayType) {
      final T receiverComponentType = this.componentType(receiverClass);
      return
        this.assignable(receiverComponentType == null ? receiverClass : receiverComponentType,
                        this.componentType(payloadGenericArrayType));
    }

    @Override
    protected final boolean classIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      for (final T bound : this.upperBounds(payloadType)) {
        if (this.assignable(receiverType, bound)) {
          return true;
        }
      }
      return false;
    }


    @Override
    protected final boolean parameterizedTypeIsAssignableFromClass(final T receiverParameterizedType, final T payloadClass) {
      // Raw types are assignable and either the payload is a generic
      // class (and therefore a raw class) or the payload is a class
      // whose parameterized supertypes need to be evaluated.
      return
        this.assignable(this.type(receiverParameterizedType), payloadClass) &&
        (this.hasTypeParameters(payloadClass) ||
         this.parameterizedTypeIsAssignableFromTypes(receiverParameterizedType, this.supertypes(payloadClass)));
    }

    private final boolean parameterizedTypeIsAssignableFromTypes(final T receiverParameterizedType, final Iterable<? extends T> payloadTypes) {
      for (final T payloadType : payloadTypes) {
        if (this.hasTypeArguments(payloadType) &&
            this.parameterizedTypeIsAssignableFromParameterizedType0(receiverParameterizedType, payloadType)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected final boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverParameterizedType,
                                                                               final T payloadParameterizedType) {
      return this.parameterizedTypeIsAssignableFromTypes(receiverParameterizedType, this.supertypes(payloadParameterizedType));
    }

    private final boolean parameterizedTypeIsAssignableFromParameterizedType0(final T receiverParameterizedType,
                                                                              final T payloadParameterizedType) {
      if (this.equals(this.type(receiverParameterizedType), this.type(payloadParameterizedType))) {
        final T[] receiverTypeTypeArguments = this.typeArguments(receiverParameterizedType);
        final T[] payloadTypeTypeArguments = this.typeArguments(payloadParameterizedType);
        if (receiverTypeTypeArguments.length == payloadTypeTypeArguments.length) {
          for (int i = 0; i < receiverTypeTypeArguments.length; i++) {
            final T receiverTypeTypeArgument = receiverTypeTypeArguments[i];
            final T payloadTypeTypeArgument = payloadTypeTypeArguments[i];
            if (this.wildcard(receiverTypeTypeArgument) || this.wildcard(payloadTypeTypeArgument)) {
              if (!this.assignable(receiverTypeTypeArgument, payloadTypeTypeArgument)) {
                return false;
              }
            } else if (!this.equals(receiverTypeTypeArgument, payloadTypeTypeArgument)) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    @Override
    protected final boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      for (final T payloadTypeBound : this.upperBounds(payloadType)) {
        if (this.assignable(receiverType, payloadTypeBound)) {
          return true;
        }
      }
      return false;
    }


    @Override
    protected final boolean genericArrayTypeIsAssignableFromClass(final T receiverType, final T payloadClass) {
      return this.assignable(this.componentType(receiverType), this.componentType(payloadClass));
    }

    @Override
    protected final boolean genericArrayTypeIsAssignableFromGenericArrayType(final T receiverGenericArrayType,
                                                                             final T payloadGenericArrayType) {
      return this.assignable(this.componentType(receiverGenericArrayType), this.componentType(payloadGenericArrayType));
    }


    @Override
    protected final boolean typeVariableIsAssignableFromTypeVariable(final T receiverTypeVariable, final T payloadTypeVariable) {
      if (this.equals(receiverTypeVariable, payloadTypeVariable)) {
        return true;
      }
      // If the two type variables are not equal, then see if the
      // payload's first bound is a type variable itself (in which
      // case there will be no other bounds), and, if so, if *it* is
      // assignable to receiverTypeVariable, which may be a recursive
      // operation.
      final T firstPayloadTypeBound = this.upperBounds(payloadTypeVariable)[0];
      return this.typeVariable(firstPayloadTypeBound) && this.assignable(receiverTypeVariable, firstPayloadTypeBound);
    }


    @Override
    protected final boolean wildcardTypeIsAssignableFromClass(final T receiverWildcardType,
                                                              final boolean lowerBounded,
                                                              final T payloadClass) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadClass) &&
        (!lowerBounded || this.assignable(payloadClass, this.lowerBound(receiverWildcardType)));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverWildcardType,
                                                                          final boolean lowerBounded,
                                                                          final T payloadParameterizedType) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadParameterizedType) &&
        (!lowerBounded || this.assignable(payloadParameterizedType, this.lowerBound(receiverWildcardType)));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverWildcardType,
                                                                         final boolean lowerBounded,
                                                                         final T payloadGenericArrayType) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadGenericArrayType) &&
        (!lowerBounded || this.assignable(payloadGenericArrayType, this.lowerBound(receiverWildcardType)));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverWildcardType,
                                                                     final boolean lowerBounded,
                                                                     final T payloadTypeVariable) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadTypeVariable) &&
        (!lowerBounded || this.assignable(payloadTypeVariable, this.lowerBound(receiverWildcardType)));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromWildcardType(final T receiverWildcardType,
                                                                     final boolean receiverWildcardTypeLowerBounded,
                                                                     final T payloadWildcardType,
                                                                     final boolean payloadWildcardTypeLowerBounded) {
      final T receiverWildcardTypeUpperBound = this.upperBounds(receiverWildcardType)[0];
      if (this.assignable(receiverWildcardTypeUpperBound, this.upperBounds(payloadWildcardType)[0])) {
        if (receiverWildcardTypeLowerBounded) {
          return
            payloadWildcardTypeLowerBounded &&
            this.assignable(this.lowerBound(payloadWildcardType), this.lowerBound(receiverWildcardType));
        }
        return
          !payloadWildcardTypeLowerBounded ||
          this.assignable(receiverWildcardTypeUpperBound, this.lowerBound(payloadWildcardType));
      }
      return false;
    }

  }

}
