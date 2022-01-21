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
@Deprecated
public interface OldType {

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

    private final Function<T, ? extends OldType> toTypeFunction;

    private final UnaryOperator<T> typeFunction;

    private final Predicate<T> genericTypePredicate;

    private final Predicate<T> typeArgumentsPredicate;

    private final Function<T, T[]> typeArgumentsFunction;

    private final UnaryOperator<T> componentTypeFunction;

    private final Predicate<T> upperBoundsPredicate;

    private final Predicate<T> lowerBoundPredicate;

    private final Function<T, T[]> lowerBoundsFunction;

    private final Function<T, T[]> upperBoundsFunction;


    /*
     * Constructors.
     */


    protected Semantics(final Predicate<T> namedPredicate,
                        final BiPredicate<T, T> equalityBiPredicate,
                        final UnaryOperator<T> boxFunction,
                        final Function<T, ? extends OldType> toTypeFunction,
                        final UnaryOperator<T> typeFunction,
                        final Predicate<T> genericTypePredicate,
                        final Predicate<T> typeArgumentsPredicate,
                        final Function<T, T[]> typeArgumentsFunction,
                        final UnaryOperator<T> componentTypeFunction,
                        final Predicate<T> upperBoundsPredicate,
                        final Function<T, T[]> upperBoundsFunction,
                        final Predicate<T> lowerBoundPredicate,
                        final Function<T, T[]> lowerBoundsFunction) {
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
      this.lowerBoundsFunction = Objects.requireNonNull(lowerBoundsFunction, "lowerBoundsFunction");
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

    protected final T[] lowerBounds(final T type) {
      return this.lowerBoundsFunction.apply(type);
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

    protected final OldType toType(final T type) {
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


    private final boolean nonParameterizedTypeIsAssignableFromParameterizedType(final T receiverNonParameterizedType, final T payloadParameterizedType) {
      if (this.componentType(receiverNonParameterizedType) == null) {
        if (this.lowerBounded(receiverNonParameterizedType)) {
          return this.wildcardTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, true /* lower bounded */, payloadParameterizedType);
        } else if (this.upperBounded(receiverNonParameterizedType)) {
          if (this.named(receiverNonParameterizedType)) {
            return this.typeVariableIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
          } else {
            return this.wildcardTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, false /* upper bounded */, payloadParameterizedType);
          }
        } else {
          return this.classIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
        }
      } else if (this.type(receiverNonParameterizedType) == receiverNonParameterizedType) {
        return this.classIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
      } else {
        return this.genericArrayTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
      }
    }


    private final boolean classIsAssignableFromNonParameterizedType(final T receiverClass, final T payloadNonParameterizedType) {
      if (this.componentType(payloadNonParameterizedType) == null) {
        if (this.lowerBounded(payloadNonParameterizedType)) {
          return this.classIsAssignableFromWildcardType(receiverClass, payloadNonParameterizedType, true);
        } else if (this.upperBounded(payloadNonParameterizedType)) {
          if (this.named(payloadNonParameterizedType)) {
            return this.classIsAssignableFromTypeVariable(receiverClass, payloadNonParameterizedType);
          } else {
            return this.classIsAssignableFromWildcardType(receiverClass, payloadNonParameterizedType, false);
          }
        } else {
          return this.classIsAssignableFromClass(receiverClass, payloadNonParameterizedType);
        }
      } else if (this.type(payloadNonParameterizedType) == payloadNonParameterizedType) {
        return this.classIsAssignableFromClass(receiverClass, payloadNonParameterizedType);
      } else {
        return this.classIsAssignableFromGenericArrayType(receiverClass, payloadNonParameterizedType);
      }
    }

    protected boolean classIsAssignableFromClass(T receiverClass, T payloadClass) {
      return false;
    }

    protected boolean classIsAssignableFromParameterizedType(final T receiverClass, final T payloadParameterizedType) {
      return false;
    }

    protected boolean classIsAssignableFromGenericArrayType(final T receiverClass, final T payloadGenericArrayType) {
      return false;
    }

    protected boolean classIsAssignableFromTypeVariable(final T receiverClass, final T payloadTypeVariable) {
      return false;
    }

    protected boolean classIsAssignableFromWildcardType(final T receiverClass, final T payloadWildcardType, final boolean lowerBounded) {
      return false;
    }


    private final boolean parameterizedTypeIsAssignableFromType(final T receiverParameterizedType, final T payloadType) {
      if (this.hasTypeArguments(payloadType)) {
        return this.parameterizedTypeIsAssignableFromParameterizedType(receiverParameterizedType, payloadType);
      } else if (this.componentType(payloadType) == null) {
        if (this.lowerBounded(payloadType)) {
          return this.parameterizedTypeIsAssignableFromWildcardType(receiverParameterizedType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            return this.parameterizedTypeIsAssignableFromTypeVariable(receiverParameterizedType, payloadType);
          } else {
            return this.parameterizedTypeIsAssignableFromWildcardType(receiverParameterizedType, payloadType, false);
          }
        } else {
          return this.parameterizedTypeIsAssignableFromClass(receiverParameterizedType, payloadType);
        }
      } else if (this.type(payloadType) == payloadType) {
        return this.parameterizedTypeIsAssignableFromClass(receiverParameterizedType, payloadType);
      } else {
        return this.parameterizedTypeIsAssignableFromGenericArrayType(receiverParameterizedType, payloadType);
      }
    }

    protected boolean parameterizedTypeIsAssignableFromClass(final T receiverParameterizedType, final T payloadClass) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverParameterizedType, final T payloadParameterizedType) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromGenericArrayType(final T receiverParameterizedType, final T payloadGenericArrayType) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverParameterizedType, final T payloadTypeVariable) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromWildcardType(final T receiverParameterizedType,
                                                                    final T payloadWildcardType,
                                                                    final boolean lowerBounded) {
      return false;
    }


    private final boolean genericArrayTypeIsAssignableFromNonParameterizedType(final T receiverGenericArrayType, final T payloadNonParameterizedType) {
      if (this.componentType(payloadNonParameterizedType) == null) {
        if (this.lowerBounded(payloadNonParameterizedType)) {
          return this.genericArrayTypeIsAssignableFromWildcardType(receiverGenericArrayType, payloadNonParameterizedType, true);
        } else if (this.upperBounded(payloadNonParameterizedType)) {
          if (this.named(payloadNonParameterizedType)) {
            return this.genericArrayTypeIsAssignableFromTypeVariable(receiverGenericArrayType, payloadNonParameterizedType);
          } else {
            return this.genericArrayTypeIsAssignableFromWildcardType(receiverGenericArrayType, payloadNonParameterizedType, false);
          }
        } else {
          return this.genericArrayTypeIsAssignableFromClass(receiverGenericArrayType, payloadNonParameterizedType);
        }
      } else if (this.type(payloadNonParameterizedType) == payloadNonParameterizedType) {
        return this.genericArrayTypeIsAssignableFromClass(receiverGenericArrayType, payloadNonParameterizedType);
      } else {
        return this.genericArrayTypeIsAssignableFromGenericArrayType(receiverGenericArrayType, payloadNonParameterizedType);
      }
    }

    protected boolean genericArrayTypeIsAssignableFromClass(final T receiverGenericArrayType, final T payloadClass) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromParameterizedType(final T receiverGenericArrayType, final T payloadParameterizedType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromGenericArrayType(final T receiverGenericArrayType, final T payloadGenericArrayType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromTypeVariable(final T receiverGenericArrayType, final T payloadTypeVariable) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromWildcardType(final T receiverGenericArrayType,
                                                                   final T payloadWildcardType,
                                                                   final boolean lowerBounded) {
      return false;
    }


    private final boolean typeVariableIsAssignableFromNonParameterizedType(final T receiverTypeVariable, final T payloadNonParameterizedType) {
      if (this.componentType(payloadNonParameterizedType) == null) {
        if (this.lowerBounded(payloadNonParameterizedType)) {
          return this.typeVariableIsAssignableFromWildcardType(receiverTypeVariable, payloadNonParameterizedType, true);
        } else if (this.upperBounded(payloadNonParameterizedType)) {
          if (this.named(payloadNonParameterizedType)) {
            return this.typeVariableIsAssignableFromTypeVariable(receiverTypeVariable, payloadNonParameterizedType);
          } else {
            return this.typeVariableIsAssignableFromWildcardType(receiverTypeVariable, payloadNonParameterizedType, false);
          }
        } else {
          return this.typeVariableIsAssignableFromClass(receiverTypeVariable, payloadNonParameterizedType);
        }
      } else if (this.type(payloadNonParameterizedType) == payloadNonParameterizedType) {
        return this.typeVariableIsAssignableFromClass(receiverTypeVariable, payloadNonParameterizedType);
      } else {
        return this.typeVariableIsAssignableFromGenericArrayType(receiverTypeVariable, payloadNonParameterizedType);
      }
    }

    protected boolean typeVariableIsAssignableFromClass(final T receiverTypeVariable, final T payloadClass) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromParameterizedType(final T receiverTypeVariable, final T payloadParameterizedType) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromGenericArrayType(final T receiverTypeVariable, final T payloadGenericArrayType) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromTypeVariable(final T receiverTypeVariable, final T payloadTypeVariable) {
      return false;
    }

    protected boolean typeVariableIsAssignableFromWildcardType(final T receiverTypeVariable,
                                                               final T payloadWildcardType,
                                                               final boolean lowerBounded) {
      return false;
    }


    private final boolean wildcardTypeIsAssignableFromNonParameterizedType(final T receiverWildcardType,
                                                                           final boolean lowerBounded,
                                                                           final T payloadNonParameterizedType) {
      if (this.componentType(payloadNonParameterizedType) == null) {
        if (this.lowerBounded(payloadNonParameterizedType)) {
          return this.wildcardTypeIsAssignableFromWildcardType(receiverWildcardType, lowerBounded, payloadNonParameterizedType, true);
        } else if (this.upperBounded(payloadNonParameterizedType)) {
          if (this.named(payloadNonParameterizedType)) {
            return this.wildcardTypeIsAssignableFromTypeVariable(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
          } else {
            return this.wildcardTypeIsAssignableFromWildcardType(receiverWildcardType, lowerBounded, payloadNonParameterizedType, false);
          }
        } else {
          return this.wildcardTypeIsAssignableFromClass(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
        }
      } else if (this.type(payloadNonParameterizedType) == payloadNonParameterizedType) {
        return this.wildcardTypeIsAssignableFromClass(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
      } else {
        return this.wildcardTypeIsAssignableFromGenericArrayType(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
      }
    }

    protected boolean wildcardTypeIsAssignableFromClass(final T receiverWildcardType, final boolean lowerBounded, final T payloadClass) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverWildcardType,
                                                                    final boolean lowerBounded,
                                                                    final T payloadParameterizedType) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverWildcardType,
                                                                   final boolean lowerBounded,
                                                                   final T payloadGenericArrayType) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverWildcardType,
                                                               final boolean lowerBounded,
                                                               final T payloadTypeVariable) {
      return false;
    }

    protected boolean wildcardTypeIsAssignableFromWildcardType(final T receiverWildcardType,
                                                               final boolean receiverWildcardTypeLowerBounded,
                                                               final T payloadWildcardType,
                                                               final boolean payloadWildcardTypeLowerBounded) {
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


  public static abstract class VariantSemantics<T> extends Semantics<T> {

    private final Function<T, ? extends Collection<? extends T>> directSupertypesFunction;

    protected VariantSemantics(final Predicate<T> namedPredicate,
                               final BiPredicate<T, T> equalityBiPredicate,
                               final UnaryOperator<T> boxFunction,
                               final Function<T, ? extends OldType> toTypeFunction,
                               final Function<T, ? extends Collection<? extends T>> directSupertypesFunction,
                               final UnaryOperator<T> typeFunction,
                               final Predicate<T> genericTypePredicate,
                               final Predicate<T> typeArgumentsPredicate,
                               final Function<T, T[]> typeArgumentsFunction,
                               final UnaryOperator<T> componentTypeFunction,
                               final Predicate<T> upperBoundsPredicate,
                               final Function<T, T[]> upperBoundsFunction,
                               final Predicate<T> lowerBoundPredicate,
                               final Function<T, T[]> lowerBoundsFunction) {
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
            lowerBoundsFunction);
      this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
    }

    protected final Collection<? extends T> directSupertypes(final T type) {
      return this.directSupertypesFunction.apply(type);
    }

    protected final boolean supertype(final T sup, final T sub) {
      // Is sup a supertype of sub?  Remember that the supertype relation is reflexive.
      for (final T supertype : this.supertypes(sub)) {
        if (this.equals(supertype, sup)) {
          return true;
        }
      }
      return false;
    }

    protected final Collection<? extends T> supertypes(final T type) {
      return this.supertypes(type, null);
    }

    @SuppressWarnings("unchecked")
    private final Collection<? extends T> supertypes(final T type, Predicate<? super OldType> unseen) {
      if (unseen == null || unseen.test(this.toType(type))) {
        final Collection<? extends T> directSupertypes = this.directSupertypes(type);
        if (directSupertypes.isEmpty()) {
          return List.of(type); // the supertype relation is reflexive as well as transitive
        } else {
          if (unseen == null) {
            unseen = new HashSet<>(8)::add;
          }
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
    protected final boolean genericArrayTypeIsAssignableFromGenericArrayType(final T receiverGenericArrayType, final T payloadGenericArrayType) {
      return this.assignable(this.componentType(receiverGenericArrayType), this.componentType(payloadGenericArrayType));
    }

    protected final T[] condense(final T[] bounds) {
      if (bounds.length > 0 && this.typeVariable(bounds[0])) {
        return this.condense(this.upperBounds(bounds[0]));
      }
      return bounds;
    }

    protected final T[] condense(final T type) {
      final T[] lowerBounds = this.lowerBounds(type);
      if (lowerBounds.length > 0) {
        return this.condense(lowerBounds);
      } else {
        return this.condense(this.upperBounds(type));
      }
    }
  }


  /**
   * A {@link VariantSemantics} implementation that applies invariant
   * assignability semantics, except in the case of wildcard types,
   * which are by definition always covariant.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see CovariantSemantics
   */
  public static class InvariantSemantics<T> extends VariantSemantics<T> {

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
            t -> List.of(), // invariant means no direct supertypes of any kind
            wildcardSemantics::type,
            wildcardSemantics::hasTypeParameters,
            wildcardSemantics::hasTypeArguments,
            wildcardSemantics::typeArguments,
            wildcardSemantics::componentType,
            wildcardSemantics::upperBounded,
            wildcardSemantics::upperBounds,
            wildcardSemantics::lowerBounded,
            wildcardSemantics::lowerBounds);
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
    @Override // VariantSemantics
    public boolean assignable(final T receiverType, final T payloadType) {
      if (this.wildcard(receiverType) || this.wildcard(payloadType)) {
        return this.wildcardSemantics.assignable(receiverType, payloadType);
      }
      return this.equals(receiverType, payloadType);
    }

  }


  /**
   * A {@link VariantSemantics} implementation that applies covariant
   * assignability semantics.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see InvariantSemantics
   */
  public static class CovariantSemantics<T> extends VariantSemantics<T> {


    /*
     * Constructors.
     */


    public CovariantSemantics(final Predicate<T> namedPredicate,
                              final BiPredicate<T, T> equalityBiPredicate,
                              final UnaryOperator<T> boxFunction,
                              final Function<T, ? extends OldType> toTypeFunction,
                              final Function<T, ? extends Collection<T>> directSupertypesFunction,
                              final UnaryOperator<T> typeFunction,
                              final Predicate<T> genericTypePredicate,
                              final Function<T, T[]> typeArgumentsFunction,
                              final UnaryOperator<T> componentTypeFunction,
                              final Function<T, T[]> upperBoundsFunction,
                              final Function<T, T[]> lowerBoundsFunction) {
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
           t -> lowerBoundsFunction.apply(t).length > 0,
           lowerBoundsFunction);
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
                              final Function<T, ? extends OldType> toTypeFunction,
                              final Function<T, ? extends Collection<? extends T>> directSupertypesFunction,
                              final UnaryOperator<T> typeFunction,
                              final Predicate<T> genericTypePredicate,
                              final Predicate<T> typeArgumentsPredicate,
                              final Function<T, T[]> typeArgumentsFunction,
                              final UnaryOperator<T> componentTypeFunction,
                              final Predicate<T> upperBoundsPredicate,
                              final Function<T, T[]> upperBoundsFunction,
                              final Predicate<T> lowerBoundPredicate,
                              final Function<T, T[]> lowerBoundsFunction) {
      super(namedPredicate,
            equalityBiPredicate,
            boxFunction,
            toTypeFunction,
            directSupertypesFunction,
            typeFunction,
            genericTypePredicate,
            typeArgumentsPredicate,
            typeArgumentsFunction,
            componentTypeFunction,
            upperBoundsPredicate,
            upperBoundsFunction,
            lowerBoundPredicate,
            lowerBoundsFunction);
    }


    /*
     * Instance methods.
     */


    @Override
    protected boolean classIsAssignableFromParameterizedType(final T receiverClass, final T payloadParameterizedType) {
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
    protected boolean parameterizedTypeIsAssignableFromClass(final T receiverParameterizedType, final T payloadClass) {
      // Raw types are assignable and either the payload is a generic
      // class (and therefore a raw class) or the payload is a class
      // whose parameterized supertypes need to be evaluated.
      return
        this.assignable(this.type(receiverParameterizedType), payloadClass) &&
        (this.hasTypeParameters(payloadClass) ||
         this.parameterizedTypeIsAssignableFromAnyType(receiverParameterizedType, this.supertypes(payloadClass)));
    }

    @Override
    protected boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverParameterizedType,
                                                                         final T payloadParameterizedType) {
      return this.parameterizedTypeIsAssignableFromAnyType(receiverParameterizedType, this.supertypes(payloadParameterizedType));
    }

    private final boolean parameterizedTypeIsAssignableFromAnyType(final T receiverParameterizedType, final Iterable<? extends T> payloadTypes) {
      for (final T payloadType : payloadTypes) {
        if (this.hasTypeArguments(payloadType) &&
            this.parameterizedTypeIsAssignableFromParameterizedType0(receiverParameterizedType, payloadType)) {
          return true;
        }
      }
      return false;
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
    protected final boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverParameterizedType, final T payloadTypeVariable) {
      for (final T bound : this.upperBounds(payloadTypeVariable)) {
        if (this.assignable(receiverParameterizedType, bound)) {
          return true;
        }
      }
      return false;
    }


    @Override
    protected final boolean genericArrayTypeIsAssignableFromClass(final T receiverGenericArrayType, final T payloadClass) {
      return this.assignable(this.componentType(receiverGenericArrayType), this.componentType(payloadClass));
    }


    @Override
    protected final boolean typeVariableIsAssignableFromTypeVariable(final T receiverTypeVariable, final T payloadTypeVariable) {
      return this.supertype(receiverTypeVariable, payloadTypeVariable);
      /*
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
      */
    }


    @Override
    protected final boolean wildcardTypeIsAssignableFromClass(final T receiverWildcardType,
                                                              final boolean lowerBounded,
                                                              final T payloadClass) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadClass) &&
        (!lowerBounded || this.assignable(payloadClass, this.lowerBounds(receiverWildcardType)[0]));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverWildcardType,
                                                                          final boolean lowerBounded,
                                                                          final T payloadParameterizedType) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadParameterizedType) &&
        (!lowerBounded || this.assignable(payloadParameterizedType, this.lowerBounds(receiverWildcardType)[0]));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverWildcardType,
                                                                         final boolean lowerBounded,
                                                                         final T payloadGenericArrayType) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadGenericArrayType) &&
        (!lowerBounded || this.assignable(payloadGenericArrayType, this.lowerBounds(receiverWildcardType)[0]));
    }

    @Override
    protected final boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverWildcardType,
                                                                     final boolean lowerBounded,
                                                                     final T payloadTypeVariable) {
      return
        this.assignable(this.upperBounds(receiverWildcardType)[0], payloadTypeVariable) &&
        (!lowerBounded || this.assignable(payloadTypeVariable, this.lowerBounds(receiverWildcardType)[0]));
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
            this.assignable(this.lowerBounds(payloadWildcardType)[0], this.lowerBounds(receiverWildcardType)[0]);
        }
        return
          !payloadWildcardTypeLowerBounded ||
          this.assignable(receiverWildcardTypeUpperBound, this.lowerBounds(payloadWildcardType)[0]);
      }
      return false;
    }

  }

  public static class CdiSemantics<T> extends VariantSemantics<T> {

    private final Predicate<T> isObjectPredicate;

    private final CdiTypeArgumentSemantics<T> typeArgumentSemantics;

    public CdiSemantics(final Predicate<T> namedPredicate,
                        final BiPredicate<T, T> equalityBiPredicate,
                        final UnaryOperator<T> boxFunction,
                        final Function<T, ? extends OldType> toTypeFunction,
                        final Function<T, ? extends Collection<? extends T>> directSupertypesFunction, // must not be JavaTypes::directSupertypes; needs to take @Typed into account
                        final UnaryOperator<T> typeFunction,
                        final Predicate<T> genericTypePredicate,
                        final Predicate<T> typeArgumentsPredicate,
                        final Function<T, T[]> typeArgumentsFunction,
                        final UnaryOperator<T> componentTypeFunction,
                        final Predicate<T> upperBoundsPredicate,
                        final Function<T, T[]> upperBoundsFunction,
                        final Predicate<T> lowerBoundPredicate,
                        final Function<T, T[]> lowerBoundsFunction,
                        final Predicate<T> isObjectPredicate,
                        final CdiTypeArgumentSemantics<T> typeArgumentSemantics) {
      super(namedPredicate,
            equalityBiPredicate,
            boxFunction,
            toTypeFunction,
            directSupertypesFunction,
            typeFunction,
            genericTypePredicate,
            typeArgumentsPredicate,
            typeArgumentsFunction,
            componentTypeFunction,
            upperBoundsPredicate,
            upperBoundsFunction,
            lowerBoundPredicate,
            lowerBoundsFunction);
      this.isObjectPredicate = Objects.requireNonNull(isObjectPredicate, "isObjectPredicate");
      this.typeArgumentSemantics = Objects.requireNonNull(typeArgumentSemantics, "typeArgumentSemantics");
    }

    protected final boolean object(final T type) {
      return this.isObjectPredicate.test(type);
    }

    // Returns true if type is an "actual type" as implied by the CDI
    // specification. An "actual type" appears to be a class, a
    // parameterized type or a generic array type and most notably not
    // a type variable or a wildcard type.
    public final boolean actualType(final T type) {
      // If it has type arguments, it's a parameterized type, so is legal.
      // If it has a component type, it's either a class or generic array type, so is legal.
      // If it has upper bounds, then it's either a type variable or a wildcard so is not legal.
      return this.hasTypeArguments(type) || this.componentType(type) != null || !this.upperBounded(type);
    }

    @Override
    protected final boolean classIsAssignableFromParameterizedType(final T receiverClass,
                                                                   final T payloadParameterizedType) {
      if (this.equals(receiverClass, this.type(payloadParameterizedType))) {
        for (final T payloadTypeTypeArgument : this.typeArguments(payloadParameterizedType)) {
          if (this.object(payloadTypeTypeArgument)) {
            // OK
          } else if (this.typeVariable(payloadTypeTypeArgument)) {
            final T[] bounds = this.upperBounds(payloadTypeTypeArgument);
            switch (bounds.length) {
            case 0:
              // OK
              break;
            case 1:
              if (!object(bounds[0])) {
                return false;
              }
              break;
            default:
              return false;
            }
          } else {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    @Override
    protected final boolean parameterizedTypeIsAssignableFromClass(final T receiverParameterizedType,
                                                                   final T payloadClass) {
      if (this.equals(this.type(receiverParameterizedType), payloadClass)) {
        for (final T receiverTypeTypeArgument : this.typeArguments(receiverParameterizedType)) {
          if (this.object(receiverTypeTypeArgument)) {
            // OK
          } else if (this.typeVariable(receiverTypeTypeArgument)) {
            final T[] bounds = this.upperBounds(receiverTypeTypeArgument);
            switch (bounds.length) {
            case 0:
              // OK
              break;
            case 1:
              if (!object(bounds[0])) {
                return false;
              }
              break;
            default:
              return false;
            }
          } else {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    @Override
    protected final boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverParameterizedType,
                                                                               final T payloadParameterizedType) {
      if (this.equals(this.type(receiverParameterizedType), this.type(payloadParameterizedType))) {
        final T[] receiverTypeTypeArguments = this.typeArguments(receiverParameterizedType);
        final T[] payloadTypeTypeArguments = this.typeArguments(payloadParameterizedType);
        if (receiverTypeTypeArguments.length == payloadTypeTypeArguments.length) {
          for (int i = 0; i < receiverTypeTypeArguments.length; i++) {
            if (!typeArgumentSemantics.assignable(receiverTypeTypeArguments[i], payloadTypeTypeArguments[i])) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    static final class CdiTypeArgumentSemantics<T> extends VariantSemantics<T> {

      private final CovariantSemantics<T> covariantSemantics;

      CdiTypeArgumentSemantics(final Predicate<T> namedPredicate,
                               final BiPredicate<T, T> equalityBiPredicate,
                               final UnaryOperator<T> boxFunction,
                               final Function<T, ? extends OldType> toTypeFunction,
                               final Function<T, ? extends Collection<? extends T>> directSupertypesFunction, // may? be? JavaTypes::directSupertypes?
                               final UnaryOperator<T> typeFunction,
                               final Predicate<T> genericTypePredicate,
                               final Predicate<T> typeArgumentsPredicate,
                               final Function<T, T[]> typeArgumentsFunction,
                               final UnaryOperator<T> componentTypeFunction,
                               final Predicate<T> upperBoundsPredicate,
                               final Function<T, T[]> upperBoundsFunction,
                               final Predicate<T> lowerBoundPredicate,
                               final Function<T, T[]> lowerBoundsFunction,
                               final CovariantSemantics<T> covariantSemantics) {
        super(namedPredicate,
              equalityBiPredicate,
              boxFunction,
              toTypeFunction,
              directSupertypesFunction,
              typeFunction,
              genericTypePredicate,
              typeArgumentsPredicate,
              typeArgumentsFunction,
              componentTypeFunction,
              upperBoundsPredicate,
              upperBoundsFunction,
              lowerBoundPredicate,
              lowerBoundsFunction);
        this.covariantSemantics = Objects.requireNonNull(covariantSemantics, "covariantSemantics");
      }

      @Override
      protected final boolean classIsAssignableFromTypeVariable(final T receiverClass,
                                                                final T payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverClass, payloadTypeVariable);
      }

      @Override
      protected final boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverParameterizedType,
                                                                            final T payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverParameterizedType, payloadTypeVariable);
      }

      @Override
      protected final boolean genericArrayTypeIsAssignableFromTypeVariable(final T receiverGenericArrayType,
                                                                           final T payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverGenericArrayType, payloadTypeVariable);
      }

      private final boolean actualTypeIsAssignableFromTypeVariable(final T receiverActualType,
                                                                   final T payloadTypeVariable) {
        // Section 5.2.4 of the CDI specification is mealy-mouthed but
        // interpretable:
        //
        // "…the required type parameter [receiverActualType] is an
        // actual type [never defined, but implied by other sentences
        // scattered throughout the specification to be either a
        // class, parameterized type or generic array type], the bean
        // type parameter is a type variable [payloadTypeVariable] and
        // the actual type [receiverActualType] is assignable to the
        // upper bound[s], if any [there will always be one], of the
        // type variable [payloadTypeVariable], or…"
        //
        // So perhaps somewhat counterintuitively check all the bounds
        // of the *payload* and see if the *receiver* is assignable to
        // all of them.
        for (final T bound : this.condense(this.upperBounds(payloadTypeVariable))) {
          // Note that this is somewhat backwards to what you may expect.
          if (!this.covariantSemantics.assignable(bound, receiverActualType)) {
            return false;
          }
        }
        return true;
      }

      @Override
      protected final boolean typeVariableIsAssignableFromTypeVariable(final T receiverTypeVariable,
                                                                       final T payloadTypeVariable) {
        // Section 5.2.4 again:
        //
        // "…the required type parameter and the bean type parameter are
        // both type variables and the upper bound of the required [receiver]
        // type parameter is assignable to the upper bound, if any, of
        // the bean [payload] type parameter."
        //
        // The first of many problems with this:
        //
        // This is of course ambiguous.  See
        // https://issues.redhat.com/browse/CDI-440 which was
        // unceremoniously closed for no good reason.
        //
        // "For example, the bullet
        //
        // * the required type parameter and the bean type parameter
        //   are both type variables and the upper bound of the
        //   required type parameter is assignable to the upper bound,
        //   if any, of the bean type parameter.
        //
        // should have meaning similar to:
        //
        // for each upper bound T of the bean type parameter, there is
        // an (at least one) upper bound of the required type which is
        // assignable to T."
        //
        // This is not normal Java/covariant semantics.  In Java, if
        // you have two type variables, A extends Object and B extends
        // Object, you cannot assign B to A or vice versa.  Here, the
        // CDI specification is essentially performing a kind of type
        // erasure in this one case.
        //
        // In CDI, when a type variable is a type argument (by
        // definition of a parameterized type), the platform basically
        // erases it.  In CDI you cannot have a type-variable-typed
        // injection point, because of course you can't predict what
        // "value" that injection point's type will have at
        // application analysis time.  So it stands to reason that if
        // there is a type variable "anywhere in" the injection point
        // (as a type argument, say) it will effectively be erased to
        // its bounds.
        return this.match(this.upperBounds(payloadTypeVariable), this.upperBounds(receiverTypeVariable));
      }

      @Override
      protected final boolean wildcardTypeIsAssignableFromClass(final T receiverWildcardType,
                                                                final boolean lowerBounded,
                                                                final T payloadClass) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadClass);
      }

      @Override
      protected final boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverWildcardType,
                                                                            final boolean lowerBounded,
                                                                            final T payloadParameterizedType) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadParameterizedType);
      }

      @Override
      protected final boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverWildcardType,
                                                                           final boolean lowerBounded,
                                                                           final T payloadGenericArrayType) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadGenericArrayType);
      }

      private final boolean wildcardTypeIsAssignableFromActualType(final T receiverWildcardType,
                                                                   final boolean lowerBounded,
                                                                   final T payloadActualType) {
        return
          (!lowerBounded || this.match(payloadActualType, this.lowerBounds(receiverWildcardType))) &&
          this.match(this.upperBounds(receiverWildcardType), payloadActualType);
      }

      @Override
      protected final boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverWildcardType,
                                                                       final boolean lowerBounded,
                                                                       final T payloadTypeVariable) {
        final T[] condensedPayloadTypeVariableBounds = this.condense(this.upperBounds(payloadTypeVariable));
        if (!lowerBounded || this.match(condensedPayloadTypeVariableBounds, false, this.lowerBounds(receiverWildcardType), true)) {
          final T[] condensedReceiverWildcardUpperBounds = this.condense(this.upperBounds(receiverWildcardType));
          return
            this.match(condensedReceiverWildcardUpperBounds, false, condensedPayloadTypeVariableBounds, false) ||
            this.match(condensedPayloadTypeVariableBounds, false, condensedReceiverWildcardUpperBounds, false);
        }
        return false;
      }

      private final boolean match(final T bound0, final T[] bounds1) {
        return this.match(bound0, bounds1, true);
      }

      private final boolean match(final T bound0, T[] bounds1, final boolean condense1) {
        if (bounds1.length > 0) {
          if (condense1) {
            bounds1 = this.condense(bounds1);
          }
          boolean match = false;
          for (final T bound1 : bounds1) {
            if (this.covariantSemantics.assignable(bound0, bound1)) {
              match = true;
              break;
            }
          }
          if (!match) {
            return false;
          }
        }
        return true;
      }

      private final boolean match(final T[] bounds0, final T bound1) {
        return this.match(bounds0, true, bound1);
      }

      private final boolean match(T[] bounds0, final boolean condense0, final T bound1) {
        if (bounds0.length > 0) {
          if (condense0) {
            bounds0 = this.condense(bounds0);
          }
          boolean match = false;
          for (final T bound0 : bounds0) {
            if (this.covariantSemantics.assignable(bound0, bound1)) {
              match = true;
              break;
            }
          }
          if (!match) {
            return false;
          }
        }
        return true;
      }

      private final boolean match(final T[] bounds0, final T[] bounds1) {
        return this.match(bounds0, true, bounds1, true);
      }

      private final boolean match(T[] bounds0, final boolean condense0, T[] bounds1, final boolean condense1) {
        if (bounds0.length > 0 && bounds1.length > 0) {
          for (final T bound0 : condense0 ? this.condense(bounds0) : bounds0) {
            boolean match = false;
            for (final T bound1 : condense1 ? this.condense(bounds1) : bounds1) {
              if (this.covariantSemantics.assignable(bound0, bound1)) {
                match = true;
                break;
              }
            }
            if (!match) {
              return false;
            }
          }
        }
        return true;
      }

    }

  }

}
