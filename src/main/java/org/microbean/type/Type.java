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

public interface Type {


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

    @Override
    protected boolean assignable0(final T receiverType, final T payloadType) {
      return this.equalityBiPredicate.equals(receiverType, payloadType);
    }

  }

  public static class CovariantSemantics<T> extends InvariantSemantics<T> {

    private final Semantics<T> typeArgumentSemantics;

    private final NameFunction<T> nameFunction;

    private final ToStringFunction<T> toStringFunction;

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
                              final ToStringFunction<T> toStringFunction,
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
      this.toStringFunction = Objects.requireNonNull(toStringFunction, "toStringFunction");
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
          @Override
          protected final Semantics<T> semantics(final T receiverType, final T payloadType) {
            if (lowerBounded(receiverType) || lowerBounded(payloadType)) {
              return CovariantSemantics.this;
            } else if (upperBounded(receiverType)) {
              if (name(receiverType) == null) {
                // receiverType is an upper bounded wildcard, not a type variable
                return CovariantSemantics.this;
              }
            } else if (upperBounded(payloadType) && name(payloadType) == null) {
              return CovariantSemantics.this;
            }
            return super.semantics(receiverType, payloadType);
          }
        };
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

    private final T[] directSupertypes(final T type) {
      return this.directSupertypesFunction.directSupertypes(type);
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

    protected boolean nonParameterizedTypeIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
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
    

    protected final boolean classIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
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
      if (sup == sub || equals(sup, sub)) {
        return true;
      }
      for (final T supertype : supertypes(sub)) {
        if (supertype == sup || equals(supertype, sup)) {
          return true;
        }
      }
      return false;
    }
    
    protected final T[] supertypes(final T type) {
      return supertypes(type, new HashSet<>());
    }
    
    private final T[] supertypes(final T type, final Set<String> seen) {
      final Collection<T> supertypes;
      if (seen.add(this.toStringFunction.toString(type))) {
        supertypes = new ArrayList<>();
        supertypes.add(type); // the supertype relation is reflexive as well as transitive
        for (final T directSupertype : directSupertypes(type)) {
          supertypes.addAll(Arrays.asList(supertypes(directSupertype, seen)));
        }
        return (T[])supertypes.toArray();
      } else {
        supertypes = null;
        return (T[])List.of().toArray();
      }
    }

    protected boolean classIsAssignableFromClass(final T receiverType, final T payloadType) {
      return supertype(receiverType, payloadType);
    }

    protected boolean classIsAssignableFromParameterizedType(final T receiverType, final T payloadType) {
      return this.assignable(receiverType, this.type(payloadType));
    }

    protected boolean classIsAssignableFromGenericArrayType(final T receiverType, final T payloadType) {
      return
        this.assignable(this.componentTypeFunction.componentType(receiverType),
                        this.componentTypeFunction.componentType(payloadType));
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


    protected final boolean parameterizedTypeIsAssignableFromType(final T receiverType, final T payloadType) {
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

    protected boolean parameterizedTypeIsAssignableFromWildcardType(final T receiverType, final T payloadType, final boolean lowerBounded) {
      return false;
    }


    protected final boolean genericArrayTypeIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
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
      return this.assignable(componentType(receiverType), componentType(payloadType));
    }

    protected boolean genericArrayTypeIsAssignableFromTypeVariable(final T receiverType, final T payloadType) {
      return false;
    }

    protected boolean genericArrayTypeIsAssignableFromWildcardType(final T receiverType, final T payloadType, final boolean lowerBounded) {
      return false;
    }


    protected final boolean typeVariableIsAssignableFromNonParameterizedType(final T receiverType, final T payloadType) {
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

    protected boolean typeVariableIsAssignableFromWildcardType(final T receiverType, final T payloadType, final boolean lowerBounded) {
      return false;
    }


    protected final boolean wildcardTypeIsAssignableFromNonParameterizedType(final T receiverType, final boolean lowerBounded, final T payloadType) {
      // assert: !hasTypeArguments(payloadType)
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

    protected boolean wildcardTypeIsAssignableFromParameterizedType(final T receiverType, final boolean lowerBounded, final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    protected boolean wildcardTypeIsAssignableFromGenericArrayType(final T receiverType, final boolean lowerBounded, final T payloadType) {
      return
        this.assignable(this.upperBounds(receiverType)[0], payloadType) &&
        (!lowerBounded || this.assignable(payloadType, this.lowerBounds(receiverType)[0]));
    }

    protected boolean wildcardTypeIsAssignableFromTypeVariable(final T receiverType, final boolean lowerBounded, final T payloadType) {
      if (lowerBounded) {
        return this.assignable(payloadType, this.lowerBounds(receiverType)[0]);
      }
      return this.assignable(this.upperBounds(receiverType)[0], payloadType);
    }

    protected boolean wildcardTypeIsAssignableFromWildcardType(final T receiverType, final boolean receiverTypeLowerBounded, final T payloadType, final boolean payloadTypeLowerBounded) {
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
