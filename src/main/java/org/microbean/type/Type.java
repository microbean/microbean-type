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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Type {

  /**
   * Returns a hashcode for this {@link Type}.
   *
   * @return a hashcode for this type
   *
   * @see #equals(Object)
   */
  public int hashCode();

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link Type}.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link Type}; {@code false} otherwise
   *
   * @see #hashCode()
   */
  public boolean equals(final Object other);


  /*
   * Nested classes.
   */


  public static abstract class Semantics<T> {

    protected Semantics<T> semantics(final T receiverType, final T payloadType) {
      return this;
    }

    public final boolean assignable(final T receiverType, final T payloadType) {
      return
        this.semantics(Objects.requireNonNull(receiverType, "receiverType"),
                       Objects.requireNonNull(payloadType, "payloadType"))
        .assignable0(receiverType, payloadType);
    }

    protected abstract boolean assignable0(final T receiverType, final T payloadType);

  }

  public static class InvariantSemantics<T> extends Semantics<T> {

    private final EqualityBiPredicate<T> equalityBiPredicate;

    public InvariantSemantics(final EqualityBiPredicate<T> equalityBiPredicate) {
      super();
      this.equalityBiPredicate = Objects.requireNonNull(equalityBiPredicate, "equalityBiPredicate");
    }

    protected final boolean equals(final T receiverType, final T payloadType) {
      return this.equalityBiPredicate.equals(receiverType, payloadType);
    }

    @Override // Semantics
    protected boolean assignable0(final T receiverType, final T payloadType) {
      return this.equalityBiPredicate.equals(receiverType, payloadType);
    }

  }

  public static class CovariantSemantics<T> extends InvariantSemantics<T> {

    private final Semantics<T> typeArgumentSemantics;

    private final NameFunction<T> nameFunction;

    private final UnaryOperator<T> boxFunction;

    private final ToTypeFunction<T> toTypeFunction;

    private final TypeResolver<T> typeResolver;
    
    private final DirectSupertypesFunction<T> directSupertypesFunction;

    private final TypeFunction<T> typeFunction;

    private final GenericTypePredicate<T> genericTypePredicate;

    private final TypeParametersFunction<T> typeParametersFunction;

    private final ParameterizedTypePredicate<T> typeArgumentsPredicate;

    private final TypeArgumentsFunction<T> typeArgumentsFunction;

    private final ComponentTypeFunction<T> componentTypeFunction;

    private final UpperBoundsPredicate<T> upperBoundsPredicate;

    private final LowerBoundsPredicate<T> lowerBoundsPredicate;

    private final BoundsFunction<T> lowerBoundsFunction;

    private final BoundsFunction<T> upperBoundsFunction;

    public CovariantSemantics(final NameFunction<T> nameFunction,
                              final EqualityBiPredicate<T> equalityBiPredicate,
                              final UnaryOperator<T> boxFunction,
                              final ToTypeFunction<T> toTypeFunction,
                              final TypeResolver<T> typeResolver,
                              final DirectSupertypesFunction<T> directSupertypesFunction,
                              final TypeFunction<T> typeFunction,
                              final GenericTypePredicate<T> genericTypePredicate,
                              final TypeParametersFunction<T> typeParametersFunction, // may not need this
                              final ParameterizedTypePredicate<T> typeArgumentsPredicate,
                              final TypeArgumentsFunction<T> typeArgumentsFunction,
                              final ComponentTypeFunction<T> componentTypeFunction,
                              final UpperBoundsPredicate<T> upperBoundsPredicate,
                              final BoundsFunction<T> upperBoundsFunction,
                              final LowerBoundsPredicate<T> lowerBoundsPredicate,
                              final BoundsFunction<T> lowerBoundsFunction) {
      super(equalityBiPredicate);
      this.nameFunction = Objects.requireNonNull(nameFunction, "nameFunction");
      this.boxFunction = boxFunction == null ? UnaryOperator.identity() : boxFunction;
      this.toTypeFunction = Objects.requireNonNull(toTypeFunction, "toTypeFunction");
      this.typeResolver = Objects.requireNonNull(typeResolver, "typeResolver");
      this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
      this.typeFunction = Objects.requireNonNull(typeFunction, "typeFunction");
      this.genericTypePredicate = Objects.requireNonNull(genericTypePredicate, "genericTypePredicate");
      this.typeParametersFunction = Objects.requireNonNull(typeParametersFunction, "typeParametersFunction");
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

    private final boolean hasTypeParameters(final T type) {
      return this.genericTypePredicate.hasTypeParameters(type);
    }

    private final T[] typeParameters(final T type) {
      return this.typeParametersFunction.typeParameters(type);
    }

    protected final Semantics<T> typeArgumentSemantics() {
      return this.typeArgumentSemantics;
    }

    private final boolean hasTypeArguments(final T type) {
      return this.typeArgumentsPredicate.hasTypeArguments(type);
    }

    private final T[] typeArguments(final T type) {
      return this.typeArgumentsFunction.typeArguments(type);
    }

    private final boolean lowerBounded(final T type) {
      return this.lowerBoundsPredicate.lowerBounded(type);
    }

    private final boolean upperBounded(final T type) {
      return this.upperBoundsPredicate.upperBounded(type);
    }

    private final T[] lowerBounds(final T type) {
      return this.lowerBoundsFunction.bounds(type);
    }

    private final T[] upperBounds(final T type) {
      return this.upperBoundsFunction.bounds(type);
    }

    private final T componentType(final T type) {
      return this.componentTypeFunction.componentType(type);
    }

    private final String name(final T type) {
      return this.nameFunction.name(type);
    }

    protected final boolean wildcard(final T type) {
      return name(type) == null && lowerBounded(type) || upperBounded(type);
    }

    protected final boolean typeVariable(final T type) {
      return upperBounded(type) && name(type) != null && !lowerBounded(type);
    }

    private final T type(final T type) {
      return this.typeFunction.type(type);
    }

    private final T resolve(final T type, final Function<? super Type, ? extends T> cacheReader) {
      return this.typeResolver.resolve(type, cacheReader);
    }

    private final T[] directSupertypes(final T type, final BiConsumer<? super Type, ? super T> cacheWriter) {
      return this.directSupertypesFunction.directSupertypes(type, cacheWriter);
    }

    @Override
    protected final boolean assignable0(final T receiverType, final T payloadType) {
      final boolean returnValue;
      if (hasTypeArguments(receiverType)) {
        returnValue = parameterizedTypeIsAssignableFromType(receiverType, payloadType);
      } else if (hasTypeArguments(payloadType)) {
        returnValue = nonParameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else {
        final T receiverComponentType = componentType(receiverType);
        if (receiverComponentType == null) {
          if (lowerBounded(receiverType)) {
            assert !upperBounded(receiverType);
            returnValue = wildcardTypeIsAssignableFromNonParameterizedType(receiverType, true, payloadType);
          } else if (upperBounded(receiverType)) {
            final String name = name(receiverType);
            if (name == null) {
              returnValue = wildcardTypeIsAssignableFromNonParameterizedType(receiverType, false, payloadType);
            } else {
              returnValue = typeVariableIsAssignableFromNonParameterizedType(receiverType, payloadType);
            }
          } else {
            returnValue = classIsAssignableFromNonParameterizedType(receiverType, payloadType);
          }
        } else {
          final T receiverRawType = type(receiverType);
          if (receiverRawType == receiverType) {
            returnValue = classIsAssignableFromNonParameterizedType(receiverType, payloadType);
          } else {
            returnValue = genericArrayTypeIsAssignableFromNonParameterizedType(receiverType, payloadType);
          }
        }
      }
      return returnValue;
    }

    private final boolean nonParameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      // assert: receiverType IS NOT a parameterized type
      // assert: payloadType IS a parameterized type
      final boolean returnValue;
      final T receiverComponentType = componentType(receiverType);
      if (receiverComponentType == null) {
        if (lowerBounded(receiverType)) {
          assert !upperBounded(receiverType);
          returnValue = wildcardTypeIsAssignableFromParameterizedType(receiverType, true /* lower bounded */, payloadType);
        } else if (upperBounded(receiverType)) {
          final String name = name(receiverType);
          if (name == null) {
            returnValue = wildcardTypeIsAssignableFromParameterizedType(receiverType, false /* upper bounded */, payloadType);
          } else {
            returnValue = typeVariableIsAssignableFromParameterizedType(receiverType, payloadType);
          }
        } else {
          returnValue = classIsAssignableFromParameterizedType(receiverType, payloadType);
        }
      } else {
        final T receiverRawType = type(receiverType);
        if (receiverRawType == receiverType) {
          returnValue = classIsAssignableFromParameterizedType(receiverType, payloadType);
        } else {
          returnValue = genericArrayTypeIsAssignableFromParameterizedType(receiverType, payloadType);
        }
      }
      return returnValue;
    }


    private final boolean classIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
      final boolean returnValue;
      final T payloadComponentType = componentType(payloadType);
      if (payloadComponentType == null) {
        if (lowerBounded(payloadType)) {
          assert !lowerBounded(payloadType);
          returnValue = classIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (upperBounded(payloadType)) {
          final String name = name(payloadType);
          if (name == null) {
            returnValue = classIsAssignableFromWildcardType(receiverType, payloadType, false);
          } else {
            returnValue = classIsAssignableFromTypeVariable(receiverType, payloadType);
          }
        } else {
          returnValue = classIsAssignableFromClass(receiverType, payloadType);
        }
      } else {
        final T payloadRawType = type(payloadType);
        if (payloadRawType == payloadType) {
          returnValue = classIsAssignableFromClass(receiverType, payloadType);
        } else {
          returnValue = classIsAssignableFromGenericArrayType(receiverType, payloadType);
        }
      }
      return returnValue;
    }

    protected final boolean supertype(final T sup, final T sub) {
      // Is sup a supertype of sub?
      if (equals(sup, sub)) {
        return true;
      } else if (sup instanceof Class<?> supC && sub instanceof Class<?> subC) {
        // Easy optimization
        return supC.isAssignableFrom(subC);
      } else {
        final Set<? super Type> unseen = new HashSet<>();
        for (final T supertype : supertypes(sub, unseen::add, CovariantSemantics::returnNullCacheReader, CovariantSemantics::sinkCacheWriter)) {
          if (supertype == sup || equals(supertype, sup)) {
            return true;
          }
        }
      }
      return false;
    }

    private static final <T> T returnNullCacheReader(final Type ignored) {
      return null;
    }

    private static final <T> void sinkCacheWriter(final Type u, final T r) {

    }

    protected final T[] supertypes(final T type) {
      final Set<? super Type> unseen = new HashSet<>();
      final Map<? super Type, T> cache = new HashMap<>();
      final T[] returnValue = supertypes(type, unseen::add, cache::get, cache::put);

      return returnValue;
    }

    @SuppressWarnings("unchecked")
    private final T[] supertypes(T type,
                                 final Predicate<? super Type> unseen,
                                 final Function<? super Type, ? extends T> cacheReader,
                                 final BiConsumer<? super Type, ? super T> cacheWriter) {
      type = this.resolve(type, cacheReader);
      final Collection<T> supertypes;
      if (unseen.test(this.toTypeFunction.toType(type))) {
        supertypes = new ArrayList<>();
        supertypes.add(type); // the supertype relation is reflexive as well as transitive
        for (final T directSupertype : this.directSupertypes(type, cacheWriter)) {
          supertypes.addAll(Arrays.asList(this.supertypes(directSupertype, unseen, cacheReader, cacheWriter)));
        }
        return (T[])supertypes.toArray();
      } else {
        supertypes = null;
        return (T[])List.of().toArray();
      }
    }

    protected boolean classIsAssignableFromClass(T receiverType, T payloadType) {
      receiverType = box(receiverType);
      payloadType = box(payloadType);
      if (receiverType instanceof Class<?> c0 && payloadType instanceof Class<?> c1) {
        // Use the native code shortcut
        return c0.isAssignableFrom(c1);
      }
      return supertype(receiverType, payloadType);
    }

    protected boolean classIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return this.assignable(receiverType, this.type(payloadType));
    }

    protected boolean classIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      final T receiverComponentType = this.componentType(receiverType);
      if (receiverComponentType == null) {
        return false;
      } else {
        return this.assignable(receiverComponentType, this.componentType(payloadType));
      }
    }

    protected boolean classIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      final T[] bounds = this.upperBounds(payloadType);
      for (final T bound : bounds) {
        if (this.assignable(receiverType, bound)) {
          return true;
        }
      }
      return false;
    }

    protected boolean classIsAssignableFromWildcardType(final T receiverType, final T payloadType, final boolean lowerBounded) {
      return false;
    }


    private final boolean parameterizedTypeIsAssignableFromType(final T receiverType, final T payloadType) {
      // assert: receiverType is a parameterized type
      final boolean returnValue;
      if (hasTypeArguments(payloadType)) {
        returnValue = parameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else {
        final T payloadComponentType = componentType(payloadType);
        if (payloadComponentType == null) {
          if (lowerBounded(payloadType)) {
            assert !upperBounded(payloadType);
            returnValue = parameterizedTypeIsAssignableFromWildcardType(receiverType, payloadType, true);
          } else if (upperBounded(payloadType)) {
            final String name = name(payloadType);
            if (name == null) {
              returnValue = parameterizedTypeIsAssignableFromWildcardType(receiverType, payloadType, false);
            } else {
              returnValue = parameterizedTypeIsAssignableFromTypeVariable(receiverType, payloadType);
            }
          } else {
            returnValue = parameterizedTypeIsAssignableFromClass(receiverType, payloadType);
          }
        } else {
          final T payloadRawType = type(payloadType);
          if (payloadRawType == payloadType) {
            returnValue = parameterizedTypeIsAssignableFromClass(receiverType, payloadType);
          } else {
            returnValue = parameterizedTypeIsAssignableFromGenericArrayType(receiverType, payloadType);
          }
        }
      }
      return returnValue;
    }

    protected boolean parameterizedTypeIsAssignableFromClass(final T receiverType, final T payloadType) {
      // Raw types are assignable and either the payload is a generic
      // class (and therefore a raw class) or the payload is a class
      // whose parameterized supertypes need to be evaluated.
      return
        this.assignable(this.type(receiverType), payloadType) &&
        (hasTypeParameters(payloadType)) || this.parameterizedTypeIsAssignableFromTypes(receiverType, supertypes(payloadType));
    }

    private final boolean parameterizedTypeIsAssignableFromTypes(final T receiverType, final T[] payloadTypes) {
      for (final T payloadType : payloadTypes) {
        if (hasTypeArguments(payloadType) && parameterizedTypeIsAssignableFromParameterizedType0(receiverType, payloadType)) {
          return true;
        }
      }
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return parameterizedTypeIsAssignableFromTypes(receiverType, supertypes(payloadType));
    }

    private final boolean parameterizedTypeIsAssignableFromParameterizedType0(final T receiverType, final T payloadType) {
      if (type(receiverType).equals(type(payloadType))) {
        final T[] receiverTypeTypeArguments = typeArguments(receiverType);
        final T[] payloadTypeTypeArguments = typeArguments(payloadType);
        if (receiverTypeTypeArguments.length == payloadTypeTypeArguments.length) {
          final Semantics<T> typeArgumentSemantics = this.typeArgumentSemantics();
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

    protected boolean parameterizedTypeIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      for (final T payloadTypeBound : upperBounds(payloadType)) {
        if (this.assignable(receiverType, payloadTypeBound)) {
          return true;
        }
      }
      return false;
    }

    protected boolean parameterizedTypeIsAssignableFromWildcardType(final T receiverType,
                                                                    final T payloadType,
                                                                    final boolean lowerBounded) {
      return false;
    }


    private final boolean genericArrayTypeIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
      final boolean returnValue;
      final T payloadComponentType = componentType(payloadType);
      if (payloadComponentType == null) {
        if (lowerBounded(payloadType)) {
          assert !upperBounded(payloadType);
          returnValue = genericArrayTypeIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (upperBounded(payloadType)) {
          final String name = name(payloadType);
          if (name == null) {
            returnValue = genericArrayTypeIsAssignableFromWildcardType(receiverType, payloadType, false);
          } else {
            returnValue = genericArrayTypeIsAssignableFromTypeVariable(receiverType, payloadType);
          }
        } else {
          returnValue = genericArrayTypeIsAssignableFromClass(receiverType, payloadType);
        }
      } else {
        final T payloadRawType = type(payloadType);
        if (payloadRawType == payloadType) {
          returnValue = genericArrayTypeIsAssignableFromClass(receiverType, payloadType);
        } else {
          returnValue = genericArrayTypeIsAssignableFromGenericArrayType(receiverType, payloadType);
        }
      }
      return returnValue;
    }

    protected boolean genericArrayTypeIsAssignableFromClass(final T receiverType, final T payloadType) {
      return this.assignable(componentType(receiverType), componentType(payloadType));
    }

    protected boolean genericArrayTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return this.assignable(this.componentType(receiverType), this.componentType(payloadType));
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
      // assert: !hasTypeArguments(payloadType)
      final boolean returnValue;
      final T payloadComponentType = componentType(payloadType);
      if (payloadComponentType == null) {
        if (lowerBounded(payloadType)) {
          assert !upperBounded(payloadType);
          returnValue = typeVariableIsAssignableFromWildcardType(receiverType, payloadType, true);
        } else if (upperBounded(payloadType)) {
          final String name = name(payloadType);
          if (name == null) {
            returnValue = typeVariableIsAssignableFromWildcardType(receiverType, payloadType, false);
          } else {
            returnValue = typeVariableIsAssignableFromTypeVariable(receiverType, payloadType);
          }
        } else {
          returnValue = typeVariableIsAssignableFromClass(receiverType, payloadType);
        }
      } else {
        final T payloadRawType = type(payloadType);
        if (payloadRawType == payloadType) {
          returnValue = typeVariableIsAssignableFromClass(receiverType, payloadType);
        } else {
          returnValue = typeVariableIsAssignableFromGenericArrayType(receiverType, payloadType);
        }
      }
      return returnValue;
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
      if (equals(receiverType, payloadType)) {
        return true;
      }
      final T solePayloadTypeBound = upperBounds(payloadType)[0];
      return typeVariable(solePayloadTypeBound) && this.assignable(receiverType, solePayloadTypeBound);
    }

    protected boolean typeVariableIsAssignableFromWildcardType(final T receiverType,
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
      final T payloadComponentType = componentType(payloadType);
      if (payloadComponentType == null) {
        if (lowerBounded(payloadType)) {
          assert !upperBounded(payloadType);
          returnValue = wildcardTypeIsAssignableFromWildcardType(receiverType, lowerBounded, payloadType, true);
        } else if (upperBounded(payloadType)) {
          final String name = name(payloadType);
          if (name == null) {
            returnValue = wildcardTypeIsAssignableFromWildcardType(receiverType, lowerBounded, payloadType, false);
          } else {
            returnValue = wildcardTypeIsAssignableFromTypeVariable(receiverType, lowerBounded, payloadType);
          }
        } else {
          returnValue = wildcardTypeIsAssignableFromClass(receiverType, lowerBounded, payloadType);
        }
      } else {
        final T payloadRawType = type(payloadType);
        assert payloadRawType != null;
        if (payloadRawType == payloadType) {
          returnValue = wildcardTypeIsAssignableFromClass(receiverType, lowerBounded, payloadType);
        } else {
          returnValue = wildcardTypeIsAssignableFromGenericArrayType(receiverType, lowerBounded, payloadType);
        }
      }
      return returnValue;
    }

    protected boolean wildcardTypeIsAssignableFromClass(final T receiverType, final boolean lowerBounded, final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    protected boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverType,
                                                                    final boolean lowerBounded,
                                                                    final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    protected boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverType,
                                                                   final boolean lowerBounded,
                                                                   final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    protected boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverType,
                                                               final boolean lowerBounded,
                                                               final T payloadType) {
      if (lowerBounded) {
        return this.assignable(payloadType, this.lowerBounds(receiverType)[0]);
      }
      return this.assignable(this.upperBounds(receiverType)[0], payloadType);
    }

    protected boolean wildcardTypeIsAssignableFromWildcardType(final T receiverType,
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
