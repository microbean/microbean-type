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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

import org.microbean.development.annotation.Convenience;
import org.microbean.development.annotation.Experimental;

@Experimental
public abstract class NewType<T> {

  private final T type;
  
  public NewType(final T type) {
    super();
    this.type = Objects.requireNonNull(type, "type");
  }

  public abstract boolean named();

  public boolean represents(final NewType<?> type) {
    if (type == this) {
      return true;
    } else if (type == null) {
      return false;
    } else if (this.equals(type) || Objects.equals(this.object(), type.object())) {
      return true;
    } else {
      return false;
    }
  }

  public final T object() {
    return this.type;
  }

  public abstract boolean top();
  
  public abstract NewType<T> box();

  public abstract Collection<? extends NewType<T>> directSupertypes();

  public abstract NewType<T> type();

  public abstract boolean hasTypeParameters();

  public abstract boolean hasTypeArguments();

  public abstract List<? extends NewType<T>> typeArguments();

  public abstract NewType<T> componentType();

  public abstract boolean upperBounded();

  public abstract boolean lowerBounded();

  public abstract List<? extends NewType<T>> lowerBounds();

  public abstract List<? extends NewType<T>> upperBounds();

  public boolean typeVariable() {
    return this.named() && !this.lowerBounded() && this.upperBounded();
  }

  public boolean wildcard() {
    return !this.named() && (this.lowerBounded() || this.upperBounded());
  }

  public final Collection<? extends NewType<T>> supertypes() {
    return this.supertypes(this, null);
  }

  public final <X> boolean supertype(final NewType<X> sub) {
    final NewType<T> me = this.box();
    // Does this represent a supertype of sub?  Remember that the supertype relation is reflexive.
    for (final NewType<?> supertype : this.supertypes(sub.box(), null)) {
      if (supertype.represents(me)) {
        return true;
      }
    }
    return false;
  }
  
  @SuppressWarnings("unchecked")
  private final <X> Collection<? extends NewType<X>> supertypes(final NewType<X> type, Predicate<? super NewType<?>> unseen) {
    if (unseen == null || unseen.test(type)) {
      final Collection<? extends NewType<X>> directSupertypes = type.directSupertypes();
      if (directSupertypes.isEmpty()) {
        return List.of(type); // the supertype relation is reflexive as well as transitive
      } else {
        if (unseen == null) {
          unseen = new HashSet<>(8)::add;
        }
        final Collection<NewType<X>> supertypes = new ArrayList<>(3 * directSupertypes.size() + 1);
        supertypes.add(type); // the supertype relation is reflexive as well as transitive
        for (final NewType<X> directSupertype : directSupertypes) {
          for (final NewType<X> supertype : this.supertypes(directSupertype, unseen)) {
            supertypes.add(supertype);
          }
        }
        return Collections.unmodifiableCollection(supertypes);
      }
    } else {
      return List.of();
    }
  }

  @Experimental
  public static abstract class Semantics {

    protected Semantics() {
      super();
    }

    @Convenience
    public final boolean assignable(final java.lang.reflect.Type receiverType,
                                    final java.lang.reflect.Type payloadType) {
      return this.assignable(NewJavaType.of(receiverType), NewJavaType.of(payloadType));
    }
    
    public <X, Y> boolean assignable(final NewType<X> receiverType, final NewType<Y> payloadType) {
      if (receiverType == payloadType || receiverType.equals(payloadType)) {
        return true;
      } else if (receiverType.hasTypeArguments()) {
        return parameterizedTypeIsAssignableFromType(receiverType, payloadType);
      } else if (payloadType.hasTypeArguments()) {
        return this.nonParameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else if (receiverType.componentType() == null) {
        if (receiverType.lowerBounded()) {
          return this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, true, payloadType);
        } else if (receiverType.upperBounded()) {
          if (receiverType.named()) {
            return this.typeVariableIsAssignableFromNonParameterizedType(receiverType, payloadType);
          } else {
            return this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, false, payloadType);
          }
        } else {
          return this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
        }
      } else if (receiverType.type() == receiverType) {
        return this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
      } else {
        return this.genericArrayTypeIsAssignableFromNonParameterizedType(receiverType, payloadType);
      }
    }

    private final <X, Y> boolean nonParameterizedTypeIsAssignableFromParameterizedType(final NewType<X> receiverNonParameterizedType,
                                                                                       final NewType<Y> payloadParameterizedType) {
      if (receiverNonParameterizedType.componentType() == null) {
        if (receiverNonParameterizedType.lowerBounded()) {
          return this.wildcardTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, true /* lower bounded */, payloadParameterizedType);
        } else if (receiverNonParameterizedType.upperBounded()) {
          if (receiverNonParameterizedType.named()) {
            return this.typeVariableIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
          } else {
            return this.wildcardTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, false /* upper bounded */, payloadParameterizedType);
          }
        } else {
          return this.classIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
        }
      } else if (receiverNonParameterizedType.type() == receiverNonParameterizedType) {
        return this.classIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
      } else {
        return this.genericArrayTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
      }
    }

    private final <X, Y> boolean classIsAssignableFromNonParameterizedType(final NewType<X> receiverClass,
                                                                           final NewType<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.classIsAssignableFromWildcardType(receiverClass, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.classIsAssignableFromTypeVariable(receiverClass, payloadNonParameterizedType);
          } else {
            return this.classIsAssignableFromWildcardType(receiverClass, payloadNonParameterizedType, false);
          }
        } else {
          return this.classIsAssignableFromClass(receiverClass, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.classIsAssignableFromClass(receiverClass, payloadNonParameterizedType);
      } else {
        return this.classIsAssignableFromGenericArrayType(receiverClass, payloadNonParameterizedType);
      }
    }

    protected <X, Y> boolean classIsAssignableFromClass(final NewType<X> receiverClass, final NewType<Y> payloadClass) {
      return false;
    }

    protected <X, Y> boolean classIsAssignableFromParameterizedType(final NewType<X> receiverClass, final NewType<Y> payloadParameterizedType) {
      return false;
    }

    protected <X, Y> boolean classIsAssignableFromGenericArrayType(final NewType<X> receiverClass, final NewType<Y> payloadGenericArrayType) {
      return false;
    }

    protected <X, Y> boolean classIsAssignableFromTypeVariable(final NewType<X> receiverClass, final NewType<Y> payloadTypeVariable) {
      return false;
    }

    protected <X, Y> boolean classIsAssignableFromWildcardType(final NewType<X> receiverClass, final NewType<Y> payloadWildcardType, final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean parameterizedTypeIsAssignableFromType(final NewType<X> receiverParameterizedType, final NewType<Y> payloadType) {
      if (payloadType.hasTypeArguments()) {
        return this.parameterizedTypeIsAssignableFromParameterizedType(receiverParameterizedType, payloadType);
      } else if (payloadType.componentType() == null) {
        if (payloadType.lowerBounded()) {
          return this.parameterizedTypeIsAssignableFromWildcardType(receiverParameterizedType, payloadType, true);
        } else if (payloadType.upperBounded()) {
          if (payloadType.named()) {
            return this.parameterizedTypeIsAssignableFromTypeVariable(receiverParameterizedType, payloadType);
          } else {
            return this.parameterizedTypeIsAssignableFromWildcardType(receiverParameterizedType, payloadType, false);
          }
        } else {
          return this.parameterizedTypeIsAssignableFromClass(receiverParameterizedType, payloadType);
        }
      } else if (payloadType.type() == payloadType) {
        return this.parameterizedTypeIsAssignableFromClass(receiverParameterizedType, payloadType);
      } else {
        return this.parameterizedTypeIsAssignableFromGenericArrayType(receiverParameterizedType, payloadType);
      }
    }

    protected <X, Y> boolean parameterizedTypeIsAssignableFromClass(final NewType<X> receiverParameterizedType, final NewType<Y> payloadClass) {
      return false;
    }

    protected <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType(final NewType<X> receiverParameterizedType, final NewType<Y> payloadParameterizedType) {
      return false;
    }

    protected <X, Y> boolean parameterizedTypeIsAssignableFromGenericArrayType(final NewType<X> receiverParameterizedType, final NewType<Y> payloadGenericArrayType) {
      return false;
    }

    protected <X, Y> boolean parameterizedTypeIsAssignableFromTypeVariable(final NewType<X> receiverParameterizedType, final NewType<Y> payloadTypeVariable) {
      return false;
    }

    protected <X, Y> boolean parameterizedTypeIsAssignableFromWildcardType(final NewType<X> receiverParameterizedType,
                                                                           final NewType<Y> payloadWildcardType,
                                                                           final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean genericArrayTypeIsAssignableFromNonParameterizedType(final NewType<X> receiverGenericArrayType,
                                                                                      final NewType<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.genericArrayTypeIsAssignableFromWildcardType(receiverGenericArrayType, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.genericArrayTypeIsAssignableFromTypeVariable(receiverGenericArrayType, payloadNonParameterizedType);
          } else {
            return this.genericArrayTypeIsAssignableFromWildcardType(receiverGenericArrayType, payloadNonParameterizedType, false);
          }
        } else {
          return this.genericArrayTypeIsAssignableFromClass(receiverGenericArrayType, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.genericArrayTypeIsAssignableFromClass(receiverGenericArrayType, payloadNonParameterizedType);
      } else {
        return this.genericArrayTypeIsAssignableFromGenericArrayType(receiverGenericArrayType, payloadNonParameterizedType);
      }
    }

    protected <X, Y> boolean genericArrayTypeIsAssignableFromClass(final NewType<X> receiverGenericArrayType, final NewType<Y> payloadClass) {
      return false;
    }

    protected <X, Y> boolean genericArrayTypeIsAssignableFromParameterizedType(final NewType<X> receiverGenericArrayType, final NewType<Y> payloadParameterizedType) {
      return false;
    }

    protected <X, Y> boolean genericArrayTypeIsAssignableFromGenericArrayType(final NewType<X> receiverGenericArrayType, final NewType<Y> payloadGenericArrayType) {
      return false;
    }

    protected <X, Y> boolean genericArrayTypeIsAssignableFromTypeVariable(final NewType<X> receiverGenericArrayType, final NewType<Y> payloadTypeVariable) {
      return false;
    }

    protected <X, Y> boolean genericArrayTypeIsAssignableFromWildcardType(final NewType<X> receiverGenericArrayType,
                                                                          final NewType<Y> payloadWildcardType,
                                                                          final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean typeVariableIsAssignableFromNonParameterizedType(final NewType<X> receiverTypeVariable,
                                                                                  final NewType<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.typeVariableIsAssignableFromWildcardType(receiverTypeVariable, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.typeVariableIsAssignableFromTypeVariable(receiverTypeVariable, payloadNonParameterizedType);
          } else {
            return this.typeVariableIsAssignableFromWildcardType(receiverTypeVariable, payloadNonParameterizedType, false);
          }
        } else {
          return this.typeVariableIsAssignableFromClass(receiverTypeVariable, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.typeVariableIsAssignableFromClass(receiverTypeVariable, payloadNonParameterizedType);
      } else {
        return this.typeVariableIsAssignableFromGenericArrayType(receiverTypeVariable, payloadNonParameterizedType);
      }
    }

    protected <X, Y> boolean typeVariableIsAssignableFromClass(final NewType<X> receiverTypeVariable, final NewType<Y> payloadClass) {
      return false;
    }

    protected <X, Y> boolean typeVariableIsAssignableFromParameterizedType(final NewType<X> receiverTypeVariable, final NewType<Y> payloadParameterizedType) {
      return false;
    }

    protected <X, Y> boolean typeVariableIsAssignableFromGenericArrayType(final NewType<X> receiverTypeVariable, final NewType<Y> payloadGenericArrayType) {
      return false;
    }

    protected <X, Y> boolean typeVariableIsAssignableFromTypeVariable(final NewType<X> receiverTypeVariable, final NewType<Y> payloadTypeVariable) {
      return false;
    }

    protected <X, Y> boolean typeVariableIsAssignableFromWildcardType(final NewType<X> receiverTypeVariable,
                                                                      final NewType<Y> payloadWildcardType,
                                                                      final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean wildcardTypeIsAssignableFromNonParameterizedType(final NewType<X> receiverWildcardType,
                                                                           final boolean lowerBounded,
                                                                           final NewType<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.wildcardTypeIsAssignableFromWildcardType(receiverWildcardType, lowerBounded, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.wildcardTypeIsAssignableFromTypeVariable(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
          } else {
            return this.wildcardTypeIsAssignableFromWildcardType(receiverWildcardType, lowerBounded, payloadNonParameterizedType, false);
          }
        } else {
          return this.wildcardTypeIsAssignableFromClass(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.wildcardTypeIsAssignableFromClass(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
      } else {
        return this.wildcardTypeIsAssignableFromGenericArrayType(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
      }
    }

    protected <X, Y> boolean wildcardTypeIsAssignableFromClass(final NewType<X> receiverWildcardType,
                                                               final boolean lowerBounded,
                                                               final NewType<Y> payloadClass) {
      return false;
    }

    protected <X, Y> boolean wildcardTypeIsAssignableFromParameterizedType(final NewType<X> receiverWildcardType,
                                                                           final boolean lowerBounded,
                                                                           final NewType<Y> payloadParameterizedType) {
      return false;
    }

    protected <X, Y> boolean wildcardTypeIsAssignableFromGenericArrayType(final NewType<X> receiverWildcardType,
                                                                          final boolean lowerBounded,
                                                                          final NewType<Y> payloadGenericArrayType) {
      return false;
    }

    protected <X, Y> boolean wildcardTypeIsAssignableFromTypeVariable(final NewType<X> receiverWildcardType,
                                                                      final boolean lowerBounded,
                                                                      final NewType<Y> payloadTypeVariable) {
      return false;
    }

    protected <X, Y> boolean wildcardTypeIsAssignableFromWildcardType(final NewType<X> receiverWildcardType,
                                                                      final boolean receiverWildcardTypeLowerBounded,
                                                                      final NewType<Y> payloadWildcardType,
                                                                      final boolean payloadWildcardTypeLowerBounded) {
      return false;
    }
    
  }

  @Experimental
  public static abstract class VariantSemantics extends Semantics {

    protected VariantSemantics() {
      super();
    }

    @Override
    protected <X, Y> boolean classIsAssignableFromClass(final NewType<X> receiverClass, final NewType<Y> payloadClass) {
      return receiverClass.supertype(payloadClass);
    }

    @Override
    protected final <X, Y> boolean genericArrayTypeIsAssignableFromGenericArrayType(final NewType<X> receiverGenericArrayType,
                                                                                    final NewType<Y> payloadGenericArrayType) {
      return this.assignable(receiverGenericArrayType.componentType(), payloadGenericArrayType.componentType());
    }

    protected final <X> List<? extends NewType<X>> condense(final List<? extends NewType<X>> bounds) {
      if (!bounds.isEmpty()) {
        final NewType<X> firstBound = bounds.get(0);
        if (firstBound.named() && firstBound.upperBounded()) {
          // it's a type variable
          return this.condense(firstBound.upperBounds());
        }
      }
      return bounds;
    }
    
  }

  @Experimental
  public static class CovariantSemantics extends VariantSemantics {

    public CovariantSemantics() {
      super();
    }
    
    @Override
    protected <X, Y> boolean classIsAssignableFromParameterizedType(final NewType<X> receiverClass, final NewType<Y> payloadParameterizedType) {
      return this.assignable(receiverClass, payloadParameterizedType.type());
    }

    @Override
    protected final <X, Y> boolean classIsAssignableFromGenericArrayType(final NewType<X> receiverClass, final NewType<Y> payloadGenericArrayType) {
      final NewType<X> receiverComponentType = receiverClass.componentType();
      return
        this.assignable(receiverComponentType == null ? receiverClass : receiverComponentType,
                        payloadGenericArrayType.componentType());
    }

    @Override
    protected final <X, Y> boolean classIsAssignableFromTypeVariable(final NewType<X> receiverType, final NewType<Y> payloadType) {
      for (final NewType<Y> bound : payloadType.upperBounds()) {
        if (this.assignable(receiverType, bound)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected <X, Y> boolean parameterizedTypeIsAssignableFromClass(final NewType<X> receiverParameterizedType, final NewType<Y> payloadClass) {
      // Raw types are assignable and either the payload is a generic
      // class (and therefore a raw class) or the payload is a class
      // whose parameterized supertypes need to be evaluated.
      return
        this.assignable(receiverParameterizedType.type(), payloadClass) &&
        (payloadClass.hasTypeParameters() ||
         this.parameterizedTypeIsAssignableFromAnyType(receiverParameterizedType, payloadClass.supertypes()));
    }

    @Override
    protected <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType(final NewType<X> receiverParameterizedType,
                                                                                final NewType<Y> payloadParameterizedType) {
      return this.parameterizedTypeIsAssignableFromAnyType(receiverParameterizedType, payloadParameterizedType.supertypes());
    }

    private final <X, Y> boolean parameterizedTypeIsAssignableFromAnyType(final NewType<X> receiverParameterizedType,
                                                                          final Iterable<? extends NewType<Y>> payloadTypes) {
      for (final NewType<Y> payloadType : payloadTypes) {
        if (payloadType.hasTypeArguments() &&
            this.parameterizedTypeIsAssignableFromParameterizedType0(receiverParameterizedType, payloadType)) {
          return true;
        }
      }
      return false;
    }

    private final <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType0(final NewType<X> receiverParameterizedType,
                                                                                     final NewType<Y> payloadParameterizedType) {
      if (receiverParameterizedType.type().represents(payloadParameterizedType.type())) {
        final List<? extends NewType<X>> receiverTypeTypeArguments = receiverParameterizedType.typeArguments();
        final List<? extends NewType<Y>> payloadTypeTypeArguments = payloadParameterizedType.typeArguments();
        if (receiverTypeTypeArguments.size() == payloadTypeTypeArguments.size()) {
          for (int i = 0; i < receiverTypeTypeArguments.size(); i++) {
            final NewType<X> receiverTypeTypeArgument = receiverTypeTypeArguments.get(i);
            final NewType<Y> payloadTypeTypeArgument = payloadTypeTypeArguments.get(i);
            if (receiverTypeTypeArgument.wildcard() || payloadTypeTypeArgument.wildcard()) {
              if (!this.assignable(receiverTypeTypeArgument, payloadTypeTypeArgument)) {
                return false;
              }
            } else if (!receiverTypeTypeArgument.represents(payloadTypeTypeArgument)) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    @Override
    protected final <X, Y> boolean parameterizedTypeIsAssignableFromTypeVariable(final NewType<X> receiverParameterizedType,
                                                                                 final NewType<Y> payloadTypeVariable) {
      for (final NewType<Y> bound : payloadTypeVariable.upperBounds()) {
        if (this.assignable(receiverParameterizedType, bound)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected final <X, Y> boolean genericArrayTypeIsAssignableFromClass(final NewType<X> receiverGenericArrayType,
                                                                         final NewType<Y> payloadClass) {
      return this.assignable(receiverGenericArrayType.componentType(), payloadClass.componentType());
    }


    @Override
    protected final <X, Y> boolean typeVariableIsAssignableFromTypeVariable(final NewType<X> receiverTypeVariable,
                                                                            final NewType<Y> payloadTypeVariable) {
      return receiverTypeVariable.supertype(payloadTypeVariable);
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromClass(final NewType<X> receiverWildcardType,
                                                                     final boolean lowerBounded,
                                                                     final NewType<Y> payloadClass) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadClass) &&
        (!lowerBounded || this.assignable(payloadClass, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromParameterizedType(final NewType<X> receiverWildcardType,
                                                                                 final boolean lowerBounded,
                                                                                 final NewType<Y> payloadParameterizedType) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadParameterizedType) &&
        (!lowerBounded || this.assignable(payloadParameterizedType, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromGenericArrayType(final NewType<X> receiverWildcardType,
                                                                                final boolean lowerBounded,
                                                                                final NewType<Y> payloadGenericArrayType) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadGenericArrayType) &&
        (!lowerBounded || this.assignable(payloadGenericArrayType, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromTypeVariable(final NewType<X> receiverWildcardType,
                                                                            final boolean lowerBounded,
                                                                            final NewType<Y> payloadTypeVariable) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadTypeVariable) &&
        (!lowerBounded || this.assignable(payloadTypeVariable, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromWildcardType(final NewType<X> receiverWildcardType,
                                                                            final boolean receiverWildcardTypeLowerBounded,
                                                                            final NewType<Y> payloadWildcardType,
                                                                            final boolean payloadWildcardTypeLowerBounded) {
      final NewType<X> receiverWildcardTypeUpperBound = receiverWildcardType.upperBounds().get(0);
      if (this.assignable(receiverWildcardTypeUpperBound, payloadWildcardType.upperBounds().get(0))) {
        if (receiverWildcardTypeLowerBounded) {
          return
            payloadWildcardTypeLowerBounded &&
            this.assignable(payloadWildcardType.lowerBounds().get(0), receiverWildcardType.lowerBounds().get(0));
        }
        return
          !payloadWildcardTypeLowerBounded ||
          this.assignable(receiverWildcardTypeUpperBound, payloadWildcardType.lowerBounds().get(0));
      }
      return false;
    }
    
    
  }

  @Experimental
  public static class InvariantSemantics extends VariantSemantics {

    private final CovariantSemantics wildcardSemantics;
    
    public InvariantSemantics(final CovariantSemantics wildcardSemantics) {
      super();
      this.wildcardSemantics = Objects.requireNonNull(wildcardSemantics, "wildcardSemantics");
    }

    @Override // VariantSemantics
    public <X, Y> boolean assignable(final NewType<X> receiverType, final NewType<Y> payloadType) {
      if (receiverType.wildcard() || payloadType.wildcard()) {
        return this.wildcardSemantics.assignable(receiverType, payloadType);
      }
      return receiverType.represents(payloadType);
    }
    
  }

  @Experimental
  public static class CdiSemantics extends VariantSemantics {

    private final CdiTypeArgumentSemantics typeArgumentSemantics;
    
    public CdiSemantics() {
      super();
      this.typeArgumentSemantics = new CdiTypeArgumentSemantics();
    }

    public final boolean actualType(final NewType<?> type) {
      // If it has type arguments, it's a parameterized type, so is legal.
      // If it has a component type, it's either a class or generic array type, so is legal.
      // If it has upper bounds, then it's either a type variable or a wildcard so is not legal.
      return type.hasTypeArguments() || type.componentType() != null || !type.upperBounded();
    }

    @Override
    protected final <X, Y> boolean classIsAssignableFromParameterizedType(final NewType<X> receiverClass,
                                                                          final NewType<Y> payloadParameterizedType) {
      if (receiverClass.represents(payloadParameterizedType.type())) {
        for (final NewType<Y> payloadTypeTypeArgument : payloadParameterizedType.typeArguments()) {
          if (payloadTypeTypeArgument.top()) {
            // OK
          } else if (payloadTypeTypeArgument.typeVariable()) {
            final List<? extends NewType<Y>> bounds = payloadTypeTypeArgument.upperBounds();
            switch (bounds.size()) {
            case 0:
              // OK
              break;
            case 1:
              if (!bounds.get(0).top()) {
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
    protected final <X, Y> boolean parameterizedTypeIsAssignableFromClass(final NewType<X> receiverParameterizedType,
                                                                          final NewType<Y> payloadClass) {
      if (receiverParameterizedType.type().represents(payloadClass)) {
        for (final NewType<X> receiverTypeTypeArgument : receiverParameterizedType.typeArguments()) {
          if (receiverTypeTypeArgument.top()) {
            // OK
          } else if (receiverTypeTypeArgument.typeVariable()) {
            final List<? extends NewType<X>> bounds = receiverTypeTypeArgument.upperBounds();
            switch (bounds.size()) {
            case 0:
              // OK
              break;
            case 1:
              if (!bounds.get(0).top()) {
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
    protected final <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType(final NewType<X> receiverParameterizedType,
                                                                                      final NewType<Y> payloadParameterizedType) {
      if (receiverParameterizedType.type().represents(payloadParameterizedType.type())) {
        final List<? extends NewType<X>> receiverTypeTypeArguments = receiverParameterizedType.typeArguments();
        final List<? extends NewType<Y>> payloadTypeTypeArguments = payloadParameterizedType.typeArguments();
        if (receiverTypeTypeArguments.size() == payloadTypeTypeArguments.size()) {
          for (int i = 0; i < receiverTypeTypeArguments.size(); i++) {
            if (!typeArgumentSemantics.assignable(receiverTypeTypeArguments.get(i), payloadTypeTypeArguments.get(i))) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    @Experimental
    private static class CdiTypeArgumentSemantics extends VariantSemantics {

      private final CovariantSemantics covariantSemantics;

      private CdiTypeArgumentSemantics() {
        super();
        this.covariantSemantics = new CovariantSemantics();
      }
    
      @Override
      protected final <X, Y> boolean classIsAssignableFromTypeVariable(final NewType<X> receiverClass,
                                                                       final NewType<Y> payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverClass, payloadTypeVariable);
      }
    
      @Override
      protected final <X, Y> boolean parameterizedTypeIsAssignableFromTypeVariable(final NewType<X> receiverParameterizedType,
                                                                                   final NewType<Y> payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverParameterizedType, payloadTypeVariable);
      }
    
      @Override
      protected final <X, Y> boolean genericArrayTypeIsAssignableFromTypeVariable(final NewType<X> receiverGenericArrayType,
                                                                                  final NewType<Y> payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverGenericArrayType, payloadTypeVariable);
      }
    
      private final <X, Y> boolean actualTypeIsAssignableFromTypeVariable(final NewType<X> receiverActualType,
                                                                          final NewType<Y> payloadTypeVariable) {
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
        for (final NewType<Y> bound : this.condense(payloadTypeVariable.upperBounds())) {
          // Note that this is somewhat backwards to what you may expect.
          if (!this.covariantSemantics.assignable(bound, receiverActualType)) {
            return false;
          }
        }
        return true;
      }

      @Override
      protected final <X, Y> boolean typeVariableIsAssignableFromTypeVariable(final NewType<X> receiverTypeVariable,
                                                                              final NewType<Y> payloadTypeVariable) {
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
        return this.match(payloadTypeVariable.upperBounds(), receiverTypeVariable.upperBounds());
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromClass(final NewType<X> receiverWildcardType,
                                                                       final boolean lowerBounded,
                                                                       final NewType<Y> payloadClass) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadClass);
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromParameterizedType(final NewType<X> receiverWildcardType,
                                                                                   final boolean lowerBounded,
                                                                                   final NewType<Y> payloadParameterizedType) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadParameterizedType);
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromGenericArrayType(final NewType<X> receiverWildcardType,
                                                                                  final boolean lowerBounded,
                                                                                  final NewType<Y> payloadGenericArrayType) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadGenericArrayType);
      }

      private final <X, Y> boolean wildcardTypeIsAssignableFromActualType(final NewType<X> receiverWildcardType,
                                                                          final boolean lowerBounded,
                                                                          final NewType<Y> payloadActualType) {
        return
          (!lowerBounded || this.match(payloadActualType, receiverWildcardType.lowerBounds())) &&
          this.match(receiverWildcardType.upperBounds(), payloadActualType);
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromTypeVariable(final NewType<X> receiverWildcardType,
                                                                              final boolean lowerBounded,
                                                                              final NewType<Y> payloadTypeVariable) {
        final List<? extends NewType<Y>> condensedPayloadTypeVariableBounds = this.condense(payloadTypeVariable.upperBounds());
        if (!lowerBounded || this.match(condensedPayloadTypeVariableBounds, false, receiverWildcardType.lowerBounds(), true)) {
          final List<? extends NewType<X>> condensedReceiverWildcardUpperBounds = this.condense(receiverWildcardType.upperBounds());
          return
            this.match(condensedReceiverWildcardUpperBounds, false, condensedPayloadTypeVariableBounds, false) ||
            this.match(condensedPayloadTypeVariableBounds, false, condensedReceiverWildcardUpperBounds, false);
        }
        return false;
      }

      private final <Y> boolean match(final NewType<?> bound0, final List<? extends NewType<Y>> bounds1) {
        return this.match(bound0, bounds1, true);
      }

      private final <Y> boolean match(final NewType<?> bound0, List<? extends NewType<Y>> bounds1, final boolean condense1) {
        if (!bounds1.isEmpty()) {
          if (condense1) {
            bounds1 = this.condense(bounds1);
          }
          boolean match = false;
          for (final NewType<?> bound1 : bounds1) {
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

      private final <X> boolean match(final List<? extends NewType<X>> bounds0, final NewType<?> bound1) {
        return this.match(bounds0, true, bound1);
      }

      private final <X> boolean match(List<? extends NewType<X>> bounds0, final boolean condense0, final NewType<?> bound1) {
        if (!bounds0.isEmpty()) {
          if (condense0) {
            bounds0 = this.condense(bounds0);
          }
          boolean match = false;
          for (final NewType<X> bound0 : bounds0) {
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

      private final <X, Y> boolean match(final List<? extends NewType<X>> bounds0, final List<? extends NewType<Y>> bounds1) {
        return this.match(bounds0, true, bounds1, true);
      }

      private final <X, Y> boolean match(List<? extends NewType<X>> bounds0,
                                         final boolean condense0,
                                         List<? extends NewType<Y>> bounds1,
                                         final boolean condense1) {
        if (!bounds0.isEmpty() && !bounds1.isEmpty()) {
          for (final NewType<X> bound0 : condense0 ? this.condense(bounds0) : bounds0) {
            boolean match = false;
            for (final NewType<Y> bound1 : condense1 ? this.condense(bounds1) : bounds1) {
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
