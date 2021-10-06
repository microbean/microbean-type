/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020–2021 microBean™.
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

import org.microbean.development.annotation.Experimental;

/**
 * A {@link TypeSemantics} specialization that implements the <a
 * href="https://docs.oracle.com/javase/specs/jls/se14/html/jls-4.html">Java
 * Language Specification type assignability rules</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Experimental
public final class CovariantTypeSemantics extends TypeSemantics {


  /*
   * Instance fields.
   */


  private final InvariantTypeSemantics invariantTypeSemantics;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link CovariantTypeSemantics} instance.
   *
   * @param box whether or not {@link Type}s will be {@linkplain
   * Types#box(Type) boxed} before testing their assignability
   *
   * @see #isBoxing()
   */
  public CovariantTypeSemantics(final boolean box) {
    super(box);
    this.invariantTypeSemantics = new InvariantTypeSemantics(box) {
        @Override
        protected final TypeSemantics getSemanticsFor(final Type receiverType, final Type payloadType) {
          // Wildcards are always compared covariantly.
          if (receiverType instanceof WildcardType || payloadType instanceof WildcardType) {
            return CovariantTypeSemantics.this;
          } else {
            return super.getSemanticsFor(receiverType, payloadType);
          }
        }
      };
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a {@link TypeSemantics} used for certain cases where
   * invariant type assignability semantics are called for, such as
   * when comparing {@link ParameterizedType} type arguments.
   *
   * <p>It is worth noting that {@link WildcardType}s should always be
   * compared covariantly and the {@link TypeSemantics} returned by
   * this method is guaranteed to do so.</p>
   *
   * @return a {@link TypeSemantics} used for certain cases where
   * invariant type assignability semantics are called for; never
   * {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @see #isAssignable(ParameterizedType, ParameterizedType)
   *
   * @see InvariantTypeSemantics
   */
  public final TypeSemantics getInvariantTypeSemantics() {
    return this.invariantTypeSemantics;
  }

  @Override
  protected boolean isAssignable(final Class<?> receiverType,
                                 final Class<?> payloadType) {
    return this.box(receiverType).isAssignableFrom(this.box(payloadType));
  }

  @Override
  protected boolean isAssignable(final Class<?> receiverType,
                                 final ParameterizedType payloadType) {
    return this.isAssignable(receiverType, payloadType.getRawType());
  }

  @Override
  protected boolean isAssignable(final Class<?> receiverType,
                                 final GenericArrayType payloadType) {
    return
      receiverType == Object.class ||
      this.isAssignable(receiverType.getComponentType(), payloadType.getGenericComponentType());
  }

  @Override
  protected boolean isAssignable(final Class<?> receiverType,
                                 final TypeVariable<?> payloadType) {
    for (final Type payloadTypeBound : payloadType.getBounds()) {
      if (this.isAssignable(receiverType, payloadTypeBound)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final Class<?> payloadType) {
    return
      this.isAssignable(receiverType.getRawType(), payloadType) &&
      (!Types.normalize(payloadType).equals(payloadType) ||
       this.isAssignable0(receiverType, Types.toTypes(payloadType)));
  }

  @Override
  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final ParameterizedType payloadType) {
    return this.isAssignable0(receiverType, Types.toTypes(payloadType));
  }

  private final boolean isAssignable0(final ParameterizedType receiverType,
                                      final Iterable<? extends Type> payloadTypeSet) {
    for (final Type payloadType : payloadTypeSet) {
      if (payloadType instanceof ParameterizedType && this.isAssignable0(receiverType, (ParameterizedType)payloadType)) {
        return true;
      }
    }
    return false;
  }

  private final boolean isAssignable0(final ParameterizedType receiverType,
                                      final ParameterizedType payloadType) {
    if (receiverType.getRawType().equals(payloadType.getRawType())) {
      final Type[] receiverTypeTypeArguments = receiverType.getActualTypeArguments();
      final Type[] payloadTypeTypeArguments = payloadType.getActualTypeArguments();
      if (receiverTypeTypeArguments.length == payloadTypeTypeArguments.length) {
        // The TypeSemantics used here are invariant except for wildcards.
        final TypeSemantics invariantTypeSemantics = this.getInvariantTypeSemantics();
        for (int i = 0; i < receiverTypeTypeArguments.length; i++) {
          if (!invariantTypeSemantics.isAssignable(receiverTypeTypeArguments[i], payloadTypeTypeArguments[i])) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final TypeVariable<?> payloadType) {
    for (final Type payloadTypeBound : payloadType.getBounds()) {
      if (this.isAssignable(receiverType, payloadTypeBound)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean isAssignable(final GenericArrayType receiverType,
                                 final Class<?> payloadType) {
    return this.isAssignable(receiverType.getGenericComponentType(), payloadType.getComponentType());
  }

  @Override
  protected boolean isAssignable(final TypeVariable<?> receiverType,
                                 final TypeVariable<?> payloadType) {
    final boolean returnValue;
    if (receiverType.equals(payloadType)) {
      returnValue = true;
    } else {
      // (Note that the Java Language Specification guarantees that if
      // a type variable extends another type variable, then the
      // extended type variable will be its sole bound.)
      final Object solePayloadTypeBound = payloadType.getBounds()[0];
      returnValue =
        solePayloadTypeBound instanceof TypeVariable && this.isAssignable(receiverType, (TypeVariable<?>)solePayloadTypeBound);
    }
    return returnValue;
  }

  @Override
  protected final boolean isAssignable(final WildcardType receiverType,
                                       final Class<?> payloadType) {
    return this.isAssignable0(receiverType, payloadType);
  }

  @Override
  protected final boolean isAssignable(final WildcardType receiverType,
                                       final ParameterizedType payloadType) {
    return this.isAssignable0(receiverType, payloadType);
  }

  @Override
  protected final boolean isAssignable(final WildcardType receiverType,
                                       final GenericArrayType payloadType) {
    return this.isAssignable0(receiverType, payloadType);
  }

  private final boolean isAssignable0(final WildcardType receiverType,
                                      final Type payloadActualType) {
    if (this.isAssignable(receiverType.getUpperBounds()[0], payloadActualType)) {
      final Type[] receiverLowerBounds = receiverType.getLowerBounds();
      // If there are lower bounds, then there will be only one.  See
      // https://stackoverflow.com/a/6645454/208288 and
      // https://github.com/openjdk/jdk/blob/d3d29a4f828053f5961bfd4f4ccd200791a7eeb1/src/java.base/share/classes/java/lang/reflect/WildcardType.java#L80-L82
      return receiverLowerBounds.length <= 0 || this.isAssignable(payloadActualType, receiverLowerBounds[0]);
    } else {
      return false;
    }
  }

  @Override
  protected final boolean isAssignable(final WildcardType receiverType,
                                       final TypeVariable<?> payloadType) {
    final Type[] receiverLowerBounds = receiverType.getLowerBounds();
    return
      (receiverLowerBounds != null && receiverLowerBounds.length > 0) ?
      // Why only the first bound? See:
      // https://stackoverflow.com/a/6645454/208288
      // https://github.com/openjdk/jdk/blob/d3d29a4f828053f5961bfd4f4ccd200791a7eeb1/src/java.base/share/classes/java/lang/reflect/WildcardType.java#L80-L82
      this.isAssignable(payloadType, receiverLowerBounds[0]) :
      // The Java Language Specification guarantees that if there
      // aren't lower bounds, then there are upper bounds.  There's
      // also only one; see
      // https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/reflect/WildcardType.html#getUpperBounds()
      this.isAssignable(receiverType.getUpperBounds()[0], payloadType);
  }

  @Override
  protected final boolean isAssignable(final WildcardType receiverType,
                                       final WildcardType payloadType) {
    // There are always upper bounds, and in fact only one; see
    // https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/reflect/WildcardType.html#getUpperBounds()
    final Type receiverUpperBound = receiverType.getUpperBounds()[0];
    if (this.isAssignable(receiverUpperBound, payloadType.getUpperBounds()[0])) {
      final Type[] receiverLowerBounds = receiverType.getLowerBounds();
      if (receiverLowerBounds.length > 0) {
        final Type[] payloadLowerBounds = payloadType.getLowerBounds();
        return payloadLowerBounds.length > 0 && this.isAssignable(payloadLowerBounds[0], receiverLowerBounds[0]);
      } else {
        return payloadType.getLowerBounds().length <= 0 || receiverUpperBound.equals(Object.class);
      }
    }
    return false;
  }

}
