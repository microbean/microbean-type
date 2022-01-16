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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
   * @see #assignable0(Object, Object)
   *
   * @see InvariantSemantics
   *
   * @see CovariantSemantics
   */
  public static abstract class Semantics<T> {

    /**
     * Calls {@link #assignable0(Object, Object)} on the {@link
     * Semantics} returned by the {@link #semantics(Object, Object)}
     * method, which, if not overridden, returns {@code this}, with
     * the supplied arguments, and returns the result.
     *
     * @param receiverType the type to which a reference bearing a
     * type represented by the supplied {@code payloadType} is
     * potentially assignable; must not be {@code null}
     *
     * @param payloadType the type borne by a reference that is being
     * assigned to a reference bearing the supplied {@code
     * receiverType}; must not be {@code null}
     *
     * @return {@code true} if {@code payloadType} is assignable to
     * {@code receiverType}; {@code false} otherwise
     *
     * @exception NullPointerException if either argument is {@code
     * null}
     *
     * @idempotency This method is idempotent and deterministic.
     *
     * @threadsafety This method is safe for concurrent use by
     * multiple threads.
     *
     * @see #semantics(Object, Object)
     *
     * @see #assignable0(Object, Object)
     */
    public final boolean assignable(final T receiverType, final T payloadType) {
      return
        this.semantics(Objects.requireNonNull(receiverType, "receiverType"),
                       Objects.requireNonNull(payloadType, "payloadType"))
        .assignable0(receiverType, payloadType);
    }

    /**
     * Returns a {@link Semantics} to use to {@linkplain
     * #assignable0(Object, Object) test the assignability} of the
     * supplied types, which most commonly is simply {@code this}.
     *
     * <p>This implementation returns {@code this} in all cases.</p>
     *
     * @param receiverType the type to which a reference bearing a
     * type represented by the supplied {@code payloadType} is
     * potentially assignable; must not be {@code null}
     *
     * @param payloadType the type borne by a reference that is being
     * assigned to a reference bearing the supplied {@code
     * receiverType}; must not be {@code null}
     *
     * @return a {@link Semantics} to use to {@linkplain
     * #assignable0(Object, Object) test the assignability} of the
     * supplied types, which most commonly is simply {@code this}
     *
     * @exception NullPointerException if either argument is {@code
     * null}
     *
     * @nullability This method does not, and its overrides must not,
     * return {@code null}.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     */
    protected Semantics<T> semantics(final T receiverType, final T payloadType) {
      return this;
    }

    /**
     * Returns {@code true} if and only if a reference bearing a type
     * represented by the supplied {@code payloadType} may be assigned
     * to a reference bearing a type represented by the supplied
     * {@code receiverType} according to the rules embodied by this
     * {@link Semantics} implementation.
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
     * represented by the supplied {@code payloadType} may be assigned
     * to a reference bearing a type represented by the supplied
     * {@code receiverType} according to the rules embodied by this
     * {@link Semantics} implementation; {@code false} otherwise
     *
     * @exception NullPointerException if either argument is {@code
     * null}
     *
     * @idempotency Implementations of this method must be idempotent
     * and deterministic.
     *
     * @threadsafety Implementations of this method must be safe for
     * concurrent use by multiple threads.
     */
    protected abstract boolean assignable0(final T receiverType, final T payloadType);

  }

  
  /**
   * A {@link Semantics} implementation that applies invariant
   * assignability semantics.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #assignable0(Object, Object)
   *
   * @see CovariantSemantics
   */
  public static class InvariantSemantics<T> extends Semantics<T> {

    private final BiPredicate<T, T> equalityBiPredicate;

    /**
     * Creates a new {@link InvariantSemantics}.
     *
     * @param equalityBiPredicate a {@link BiPredicate} to determine
     * when two instances of a type are equal to each other; must not
     * be {@code null}
     *
     * @exception NullPointerException if {@code equalityBiPredicate}
     * is {@code null}
     */
    public InvariantSemantics(final BiPredicate<T, T> equalityBiPredicate) {
      super();
      this.equalityBiPredicate = Objects.requireNonNull(equalityBiPredicate, "equalityBiPredicate");
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
    protected boolean assignable0(final T receiverType, final T payloadType) {
      return this.equalityBiPredicate.test(receiverType, payloadType);
    }

  }


  /**
   * A {@link Semantics} implementation that applies covariant
   * assignability semantics.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #assignable0(Object, Object)
   *
   * @see InvariantSemantics
   */
  public static class CovariantSemantics<T> extends Semantics<T> {

    private final Semantics<T> typeArgumentSemantics;

    private final BiPredicate<T, T> equalityBiPredicate;
    
    private final Predicate<T> namedPredicate;

    private final UnaryOperator<T> boxFunction;

    private final Function<T, ? extends Type> toTypeFunction;

    private final Function<T, T[]> directSupertypesFunction;

    private final UnaryOperator<T> typeFunction;

    private final Predicate<T> genericTypePredicate;

    private final Predicate<T> typeArgumentsPredicate;

    private final Function<T, T[]> typeArgumentsFunction;

    private final UnaryOperator<T> componentTypeFunction;

    private final Predicate<T> upperBoundsPredicate;

    private final Predicate<T> lowerBoundsPredicate;

    private final Function<T, T[]> lowerBoundsFunction;

    private final Function<T, T[]> upperBoundsFunction;


    /*
     * Constructors.
     */


    public CovariantSemantics(final Predicate<T> namedPredicate,
                              final BiPredicate<T, T> equalityBiPredicate,
                              final UnaryOperator<T> boxFunction,
                              final Function<T, ? extends Type> toTypeFunction,
                              final Function<T, T[]> directSupertypesFunction,
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
                              final Function<T, ? extends Type> toTypeFunction,
                              final Function<T, T[]> directSupertypesFunction,
                              final UnaryOperator<T> typeFunction,
                              final Predicate<T> genericTypePredicate,
                              final Predicate<T> typeArgumentsPredicate,
                              final Function<T, T[]> typeArgumentsFunction,
                              final UnaryOperator<T> componentTypeFunction,
                              final Predicate<T> upperBoundsPredicate,
                              final Function<T, T[]> upperBoundsFunction,
                              final Predicate<T> lowerBoundsPredicate,
                              final Function<T, T[]> lowerBoundsFunction) {
      super();
      this.namedPredicate = Objects.requireNonNull(namedPredicate, "namedPredicate");
      this.equalityBiPredicate = Objects.requireNonNull(equalityBiPredicate, "equalityBiPredicate");
      this.boxFunction = boxFunction == null ? UnaryOperator.identity() : boxFunction;
      this.toTypeFunction = Objects.requireNonNull(toTypeFunction, "toTypeFunction");
      this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
      this.typeFunction = Objects.requireNonNull(typeFunction, "typeFunction");
      this.genericTypePredicate = Objects.requireNonNull(genericTypePredicate, "genericTypePredicate");
      this.typeArgumentsPredicate = Objects.requireNonNull(typeArgumentsPredicate, "typeArgumentsPredicate");
      this.typeArgumentsFunction = Objects.requireNonNull(typeArgumentsFunction, "typeArgumentsFunction");
      this.componentTypeFunction = Objects.requireNonNull(componentTypeFunction, "componentTypeFunction");
      this.upperBoundsPredicate = Objects.requireNonNull(upperBoundsPredicate, "upperBoundsPredicate");
      this.upperBoundsFunction = Objects.requireNonNull(upperBoundsFunction, "upperBoundsFunction");
      this.lowerBoundsPredicate = Objects.requireNonNull(lowerBoundsPredicate, "lowerBoundsPredicate");
      this.lowerBoundsFunction = Objects.requireNonNull(lowerBoundsFunction, "lowerBoundsFunction");
      this.typeArgumentSemantics = new InvariantSemantics<>(equalityBiPredicate) {
          @Override // Semantics
          protected final Semantics<T> semantics(final T receiverType, final T payloadType) {
            if (wildcard(receiverType) || wildcard(payloadType)) {
              return CovariantSemantics.this;
            }
            return super.semantics(receiverType, payloadType);
          }
        };
    }

    private final T box(final T type) {
      return this.boxFunction.apply(type);
    }

    private final boolean equals(final T receiverType, final T payloadType) {
      return this.equalityBiPredicate.test(receiverType, payloadType);
    }

    private final boolean hasTypeParameters(final T type) {
      return this.genericTypePredicate.test(type);
    }

    /**
     * Returns a {@link Semantics} to use for {@linkplain
     * Semantics#assignable(Object, Object) testing the assignability}
     * of parameterized type type arguments.
     *
     * @return a {@link Semantics} to use for {@linkplain
     * Semantics#assignable(Object, Object) testing the assignability}
     * of parameterized type type arguments; never {@code null}
     *
     * @nullability This method never returns {@code null}.
     *
     * @idempotency This method is idempotent and deterministic.
     *
     * @threadsafety This method is safe for concurrent use by
     * multiple threads.
     */
    protected final Semantics<T> typeArgumentSemantics() {
      return this.typeArgumentSemantics;
    }

    private final boolean hasTypeArguments(final T type) {
      return this.typeArgumentsPredicate.test(type);
    }

    private final T[] typeArguments(final T type) {
      return this.typeArgumentsFunction.apply(type);
    }

    private final boolean lowerBounded(final T type) {
      return this.lowerBoundsPredicate.test(type);
    }

    private final boolean upperBounded(final T type) {
      return this.upperBoundsPredicate.test(type);
    }

    private final T[] lowerBounds(final T type) {
      return this.lowerBoundsFunction.apply(type);
    }

    private final T[] upperBounds(final T type) {
      return this.upperBoundsFunction.apply(type);
    }

    private final T componentType(final T type) {
      return this.componentTypeFunction.apply(type);
    }

    private final boolean named(final T type) {
      return this.namedPredicate.test(type);
    }

    private final boolean wildcard(final T type) {
      return !this.named(type) && (this.lowerBounded(type) || this.upperBounded(type));
    }

    private final boolean typeVariable(final T type) {
      return this.named(type) && !this.lowerBounded(type) && this.upperBounded(type);
    }

    private final T type(final T type) {
      return this.typeFunction.apply(type);
    }

    private final T[] directSupertypes(final T type) {
      return this.directSupertypesFunction.apply(type);
    }

    /**
     * Returns {@code true} if and only if a reference bearing a type
     * represented by the supplied {@code payloadType} may be assigned
     * to a reference bearing a type represented by the supplied
     * {@code receiverType} according to the rules described by the <a
     * href="https://docs.oracle.com/javase/specs/jls/se17/html"
     * target="_top">Java Language Specification</a>.
     *
     * <p>Primitive types may be assignable to suitable wrapper types
     * if a suitable {@code boxFunction} was supplied at construction
     * time.</p>
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
     * BiPredicate#test(Object, Object) is equal} to a reference
     * bearing a type represented by the supplied {@code
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
    @Override
    protected boolean assignable0(final T receiverType, final T payloadType) {
      final boolean returnValue;
      if (this.hasTypeArguments(receiverType)) {
        returnValue = parameterizedTypeIsAssignableFromType(receiverType, payloadType);
      } else if (this.hasTypeArguments(payloadType)) {
        returnValue = this.nonParameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else {
        final T receiverComponentType = this.componentType(receiverType);
        if (receiverComponentType == null) {
          if (this.lowerBounded(receiverType)) {
            assert !this.upperBounded(receiverType);
            returnValue = this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, true, payloadType);
          } else if (this.upperBounded(receiverType)) {
            if (this.named(receiverType)) {
              returnValue = this.typeVariableIsAssignableFromNonParameterizedType(receiverType, payloadType);
            } else {
              returnValue = this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, false, payloadType);
            }
          } else {
            returnValue = this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
          }
        } else {
          final T receiverRawType = this.type(receiverType);
          if (receiverRawType == receiverType) {
            returnValue = this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
          } else {
            returnValue = this.genericArrayTypeIsAssignableFromNonParameterizedType(receiverType, payloadType);
          }
        }
      }
      return returnValue;
    }

    private final boolean nonParameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      // assert: receiverType IS NOT a parameterized type
      // assert: payloadType IS a parameterized type
      final boolean returnValue;
      final T receiverComponentType = this.componentType(receiverType);
      if (receiverComponentType == null) {
        if (this.lowerBounded(receiverType)) {
          assert !this.upperBounded(receiverType);
          returnValue = this.wildcardTypeIsAssignableFromParameterizedType(receiverType, true /* lower bounded */, payloadType);
        } else if (this.upperBounded(receiverType)) {
          if (this.named(receiverType)) {
            returnValue = this.typeVariableIsAssignableFromParameterizedType(receiverType, payloadType);
          } else {
            returnValue = this.wildcardTypeIsAssignableFromParameterizedType(receiverType, false /* upper bounded */, payloadType);
          }
        } else {
          returnValue = this.classIsAssignableFromParameterizedType(receiverType, payloadType);
        }
      } else {
        final T receiverRawType = this.type(receiverType);
        if (receiverRawType == receiverType) {
          returnValue = this.classIsAssignableFromParameterizedType(receiverType, payloadType);
        } else {
          returnValue = this.genericArrayTypeIsAssignableFromParameterizedType(receiverType, payloadType);
        }
      }
      return returnValue;
    }


    private final boolean classIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
      final boolean returnValue;
      final T payloadComponentType = this.componentType(payloadType);
      if (payloadComponentType == null) {
        if (this.lowerBounded(payloadType)) {
          assert !this.upperBounded(payloadType);
          returnValue = this.classIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            returnValue = this.classIsAssignableFromTypeVariable(receiverType, payloadType);
          } else {
            returnValue = this.classIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          returnValue = this.classIsAssignableFromClass(receiverType, payloadType);
        }
      } else {
        final T payloadRawType = this.type(payloadType);
        if (payloadRawType == payloadType) {
          returnValue = this.classIsAssignableFromClass(receiverType, payloadType);
        } else {
          returnValue = this.classIsAssignableFromGenericArrayType(receiverType, payloadType);
        }
      }
      return returnValue;
    }

    private final boolean supertype(final T sup, final T sub) {
      // Is sup a supertype of sub?
      if (this.equals(Objects.requireNonNull(sup, "sup"), Objects.requireNonNull(sub, "sub"))) {
        return true;
      } else if (sup instanceof Class<?> supC && sub instanceof Class<?> subC) {
        // Easy optimization
        return supC.isAssignableFrom(subC);
      } else {
        final Set<? super Type> unseen = new HashSet<>();
        for (final T supertype : this.supertypes(sub, unseen::add)) {
          if (supertype == sup || this.equals(supertype, sup)) {
            return true;
          }
        }
      }
      return false;
    }

    private final T[] supertypes(final T type) {
      return this.supertypes(type, new HashSet<>()::add);
    }

    @SuppressWarnings("unchecked")
    private final T[] supertypes(final T type, final Predicate<? super Type> unseen) {
      if (unseen.test(this.toTypeFunction.apply(type))) {
        final Collection<T> supertypes = new ArrayList<>();
        supertypes.add(type); // the supertype relation is reflexive as well as transitive
        for (final T directSupertype : this.directSupertypes(type)) {
          supertypes.addAll(Arrays.asList(this.supertypes(directSupertype, unseen)));
        }
        return (T[])supertypes.toArray();
      } else {
        return (T[])List.of().toArray();
      }
    }

    private final boolean classIsAssignableFromClass(T receiverType, T payloadType) {
      receiverType = this.box(receiverType);
      payloadType = this.box(payloadType);
      if (receiverType instanceof Class<?> c0 && payloadType instanceof Class<?> c1) {
        // Use the native code shortcut
        return c0.isAssignableFrom(c1);
      }
      return this.supertype(receiverType, payloadType);
    }

    private final boolean classIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return this.assignable(receiverType, this.type(payloadType));
    }

    private final boolean classIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      final T receiverComponentType = this.componentType(receiverType);
      return this.assignable(receiverComponentType == null ? receiverType : receiverComponentType, this.componentType(payloadType));
    }

    private final boolean classIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      final T[] bounds = this.upperBounds(payloadType);
      for (final T bound : bounds) {
        if (this.assignable(receiverType, bound)) {
          return true;
        }
      }
      return false;
    }

    private final boolean classIsAssignableFromWildcardType(final T receiverType, final T payloadType, final boolean lowerBounded) {
      return false;
    }


    private final boolean parameterizedTypeIsAssignableFromType(final T receiverType, final T payloadType) {
      // assert: receiverType is a parameterized type
      final boolean returnValue;
      if (this.hasTypeArguments(payloadType)) {
        returnValue = this.parameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else {
        final T payloadComponentType = this.componentType(payloadType);
        if (payloadComponentType == null) {
          if (this.lowerBounded(payloadType)) {
            assert !this.upperBounded(payloadType);
            returnValue = this.parameterizedTypeIsAssignableFromWildcardType(receiverType, payloadType, true);
          } else if (this.upperBounded(payloadType)) {
            if (this.named(payloadType)) {
              returnValue = this.parameterizedTypeIsAssignableFromTypeVariable(receiverType, payloadType);
            } else {
              returnValue = this.parameterizedTypeIsAssignableFromWildcardType(receiverType, payloadType, false);
            }
          } else {
            returnValue = this.parameterizedTypeIsAssignableFromClass(receiverType, payloadType);
          }
        } else {
          final T payloadRawType = this.type(payloadType);
          if (payloadRawType == payloadType) {
            returnValue = this.parameterizedTypeIsAssignableFromClass(receiverType, payloadType);
          } else {
            returnValue = this.parameterizedTypeIsAssignableFromGenericArrayType(receiverType, payloadType);
          }
        }
      }
      return returnValue;
    }

    private final boolean parameterizedTypeIsAssignableFromClass(final T receiverType, final T payloadClass) {
      // Raw types are assignable and either the payload is a generic
      // class (and therefore a raw class) or the payload is a class
      // whose parameterized supertypes need to be evaluated.
      return
        this.assignable(this.type(receiverType), payloadClass) &&
        (this.hasTypeParameters(payloadClass) ||
         this.parameterizedTypeIsAssignableFromTypes(receiverType, this.supertypes(payloadClass)));
    }

    private final boolean parameterizedTypeIsAssignableFromTypes(final T receiverType, final T[] payloadTypes) {
      for (final T payloadType : payloadTypes) {
        if (this.hasTypeArguments(payloadType) &&
            this.parameterizedTypeIsAssignableFromParameterizedType0(receiverType, payloadType)) {
          return true;
        }
      }
      return false;
    }

    private final boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadParameterizedType) {
      return this.parameterizedTypeIsAssignableFromTypes(receiverType, this.supertypes(payloadParameterizedType));
    }

    private final boolean parameterizedTypeIsAssignableFromParameterizedType0(final T receiverParameterizedType, final T payloadParameterizedType) {
      if (this.equals(this.type(receiverParameterizedType), this.type(payloadParameterizedType))) {
        final T[] receiverTypeTypeArguments = this.typeArguments(receiverParameterizedType);
        final T[] payloadTypeTypeArguments = this.typeArguments(payloadParameterizedType);
        if (receiverTypeTypeArguments.length == payloadTypeTypeArguments.length) {
          final Semantics<T> s = this.typeArgumentSemantics();
          for (int i = 0; i < receiverTypeTypeArguments.length; i++) {
            if (!s.assignable(receiverTypeTypeArguments[i], payloadTypeTypeArguments[i])) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    private final boolean parameterizedTypeIsAssignableFromGenericArrayType(final T receiverParameterizedType, final T payloadGenericArrayType) {
      return false;
    }

    private final boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      for (final T payloadTypeBound : this.upperBounds(payloadType)) {
        if (this.assignable(receiverType, payloadTypeBound)) {
          return true;
        }
      }
      return false;
    }

    private final boolean parameterizedTypeIsAssignableFromWildcardType(final T receiverType,
                                                                        final T payloadType,
                                                                        final boolean lowerBounded) {
      return false;
    }


    private final boolean genericArrayTypeIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
      final boolean returnValue;
      final T payloadComponentType = this.componentType(payloadType);
      if (payloadComponentType == null) {
        if (this.lowerBounded(payloadType)) {
          assert !this.upperBounded(payloadType);
          returnValue = this.genericArrayTypeIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            returnValue = this.genericArrayTypeIsAssignableFromTypeVariable(receiverType, payloadType);            
          } else {
            returnValue = this.genericArrayTypeIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          returnValue = this.genericArrayTypeIsAssignableFromClass(receiverType, payloadType);
        }
      } else {
        final T payloadRawType = this.type(payloadType);
        if (payloadRawType == payloadType) {
          returnValue = this.genericArrayTypeIsAssignableFromClass(receiverType, payloadType);
        } else {
          returnValue = this.genericArrayTypeIsAssignableFromGenericArrayType(receiverType, payloadType);
        }
      }
      return returnValue;
    }

    private final boolean genericArrayTypeIsAssignableFromClass(final T receiverType, final T payloadType) {
      return this.assignable(this.componentType(receiverType), this.componentType(payloadType));
    }

    private final boolean genericArrayTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return false;
    }

    private final boolean genericArrayTypeIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return this.assignable(this.componentType(receiverType), this.componentType(payloadType));
    }

    private final boolean genericArrayTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      return false;
    }

    private final boolean genericArrayTypeIsAssignableFromWildcardType(final T receiverType,
                                                                       final T payloadType,
                                                                       final boolean lowerBounded) {
      return false;
    }


    private final boolean typeVariableIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
      final boolean returnValue;
      final T payloadComponentType = this.componentType(payloadType);
      if (payloadComponentType == null) {
        if (this.lowerBounded(payloadType)) {
          assert !this.upperBounded(payloadType);
          returnValue = this.typeVariableIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            returnValue = this.typeVariableIsAssignableFromTypeVariable(receiverType, payloadType);
          } else {
            returnValue = this.typeVariableIsAssignableFromWildcardType(receiverType, payloadType, false);
          }
        } else {
          returnValue = this.typeVariableIsAssignableFromClass(receiverType, payloadType);
        }
      } else {
        final T payloadRawType = this.type(payloadType);
        if (payloadRawType == payloadType) {
          returnValue = this.typeVariableIsAssignableFromClass(receiverType, payloadType);
        } else {
          returnValue = this.typeVariableIsAssignableFromGenericArrayType(receiverType, payloadType);
        }
      }
      return returnValue;
    }

    private final boolean typeVariableIsAssignableFromClass(final T receiverType, final T payloadType) {
      return false;
    }

    private final boolean typeVariableIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return false;
    }

    private final boolean typeVariableIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return false;
    }

    private final boolean typeVariableIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      if (this.equals(receiverType, payloadType)) {
        return true;
      }
      final T solePayloadTypeBound = this.upperBounds(payloadType)[0];
      return this.typeVariable(solePayloadTypeBound) && this.assignable(receiverType, solePayloadTypeBound);
    }

    private final boolean typeVariableIsAssignableFromWildcardType(final T receiverType,
                                                                   final T payloadType,
                                                                   final boolean lowerBounded) {
      return false;
    }


    private final boolean wildcardTypeIsAssignableFromNonParameterizedType(final T receiverType,
                                                                           final boolean lowerBounded,
                                                                           final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
      assert receiverType != null;
      final boolean returnValue;
      final T payloadComponentType = this.componentType(payloadType);
      if (payloadComponentType == null) {
        if (this.lowerBounded(payloadType)) {
          assert !this.upperBounded(payloadType);
          returnValue = this.wildcardTypeIsAssignableFromWildcardType(receiverType, lowerBounded, payloadType, true);
        } else if (this.upperBounded(payloadType)) {
          if (this.named(payloadType)) {
            returnValue = this.wildcardTypeIsAssignableFromTypeVariable(receiverType, lowerBounded, payloadType);
          } else {
            returnValue = this.wildcardTypeIsAssignableFromWildcardType(receiverType, lowerBounded, payloadType, false);
          }
        } else {
          returnValue = this.wildcardTypeIsAssignableFromClass(receiverType, lowerBounded, payloadType);
        }
      } else {
        final T payloadRawType = this.type(payloadType);
        assert payloadRawType != null;
        if (payloadRawType == payloadType) {
          returnValue = this.wildcardTypeIsAssignableFromClass(receiverType, lowerBounded, payloadType);
        } else {
          returnValue = this.wildcardTypeIsAssignableFromGenericArrayType(receiverType, lowerBounded, payloadType);
        }
      }
      return returnValue;
    }

    private final boolean wildcardTypeIsAssignableFromClass(final T receiverType, final boolean lowerBounded, final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    private final boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverType,
                                                                        final boolean lowerBounded,
                                                                        final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    private final boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverType,
                                                                       final boolean lowerBounded,
                                                                       final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    private final boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverType,
                                                                   final boolean lowerBounded,
                                                                   final T payloadType) {
      if (lowerBounded) {
        return this.assignable(payloadType, this.lowerBounds(receiverType)[0]);
      }
      return this.assignable(this.upperBounds(receiverType)[0], payloadType);
    }

    private final boolean wildcardTypeIsAssignableFromWildcardType(final T receiverType,
                                                                   final boolean receiverTypeLowerBounded,
                                                                   final T payloadType,
                                                                   final boolean payloadTypeLowerBounded) {
      final T receiverTypeUpperBound = this.upperBounds(receiverType)[0];
      if (this.assignable(receiverTypeUpperBound, this.upperBounds(payloadType)[0])) {
        if (receiverTypeLowerBounded) {
          return payloadTypeLowerBounded && this.assignable(this.lowerBounds(payloadType)[0], this.lowerBounds(receiverType)[0]);
        }
        return !payloadTypeLowerBounded || this.assignable(receiverTypeUpperBound, this.lowerBounds(payloadType)[0]);
      }
      return false;
    }

  }

}
