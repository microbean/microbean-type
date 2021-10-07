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

import java.util.Collection;

import org.microbean.development.annotation.Experimental;
import org.microbean.development.annotation.Incomplete;

/**
 * An object representing {@link Type} compatibility and assignability
 * semantics.
 *
 * <p>Unless otherwise documented, a method declared in this class
 * with the name {@code isAssignable} returns {@code false} for all
 * arguments. It follows that meaningful subclasses should override
 * the methods of interest.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #isAssignable(Type, Type)
 *
 * @see CovariantTypeSemantics
 *
 * @see InvariantTypeSemantics
 */
@Experimental
public abstract class TypeSemantics {


  /*
   * Instance fields.
   */


  /**
   * Whether or not {@link Type}s will be {@linkplain Types#box(Type)
   * boxed} before testing their assignability.
   *
   * @see #isBoxing()
   */
  private final boolean box;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link TypeSemantics}.
   *
   * @param box whether or not {@link Type}s will be {@linkplain
   * Types#box(Type) boxed} before testing their assignability
   *
   * @see #isBoxing()
   */
  protected TypeSemantics(final boolean box) {
    super();
    this.box = box;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns a {@linkplain Types#box(Type) boxed} representation of
   * the supplied {@link Class} if {@link #isBoxing()} returns {@code
   * true} or the supplied {@link Class} if it does not.
   *
   * @param c the {@link Class} to {@linkplain Types#box(Type) box},
   * maybe; may be {@code null} in which case {@code null} will be
   * returned
   *
   * @return the result of the {@linkplain Types#box(Type) boxing
   * operation}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  protected final Class<?> box(final Class<?> c) {
    return c == null ? null : (this.isBoxing() ? Types.box(c) : c);
  }

  /**
   * Returns {@code true} if the {@link #box(Class)} method will
   * actually attempt a {@linkplain Types#box(Type) boxing operation}
   * in appropriate circumstances, or {@code false} if it will simply
   * return the supplied {@link Class}.
   *
   * @return {@code true} if the {@link #box(Class)} method will
   * actually attempt a {@linkplain Types#box(Type) boxing operation}
   * in appropriate circumstances, or {@code false} if it will simply
   * return the supplied {@link Class}
   *
   * @see #TypeSemantics(boolean)
   */
  public final boolean isBoxing() {
    return this.box;
  }

  /**
   * Returns the {@link TypeSemantics} instance to use when testing if
   * objects bearing the supplied {@code payloadType} are assignable
   * to references bearing the supplied {@code receiverType}.
   *
   * <p>This implementation simply returns {@code this}, which is
   * almost always the correct thing to do.</p>
   *
   * <p>Overrides are prohibited from calling the {@link
   * #isAssignable(Type, Type)} method from within an invocation of
   * this method or undefined behavior may result.</p>
   *
   * @param receiverType a {@link Type} borne by a reference for which
   * an object bearing the supplied {@code payloadType} is destined;
   * may be {@code null}
   *
   * @param payloadType a {@link Type} borne by an object destined for
   * a reference bearing the supplied {@code receiverType}; may be
   * {@code null}
   *
   * @return an appropriate {@link TypeSemantics} for {@linkplain
   * #isAssignable(Type, Type) testing the assignment semantics}, or
   * {@code null} (or {@code this}) if this {@link TypeSemantics}
   * should be used (by far the most common state of affairs)
   *
   * @nullability This method does not but its overrides may return
   * {@code null}.
   *
   * @threadsafety This method is and its overrides must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is and its overrides must be idempotent.
   * No guarantee is made about determinism.
   */
  protected TypeSemantics getSemanticsFor(final Type receiverType,
                                          final Type payloadType) {
    return this;
  }

  /**
   * Returns {@code true} if an object bearing any of the {@link
   * Type}s contained by the supplied {@code payloadTypes} {@link
   * Collection} {@linkplain #isAssignable(Type, Type) is assignable
   * to} an object reference bearing a {@link Type} equal to the
   * supplied {@code receiverType}.
   *
   * @param receiverType the {@link Type} borne by an object reference
   * for which an object bearing a {@link Type} contained by the
   * supplied {@code payloadTypes} {@link Collection} is destined; may
   * be {@code null} in which case {@code false} will be returned
   *
   * @param payloadTypes a {@link Collection} of {@link Type}s
   * possibly borne by an object destined for a reference bearing a
   * {@link Type} equal to the supplied {@code receiverType}; may be
   * {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if an object bearing any of the {@link
   * Type}s contained by the supplied {@code payloadTypes} {@link
   * Collection} {@linkplain #isAssignable(Type, Type) is assignable
   * to} an object reference bearing a {@link Type} equal to the
   * supplied {@code receiverType}; {@code false} in all other cases
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.  Because the {@link
   * #getSemanticsFor(Type, Type)} method may be overridden, and
   * because therefore its behavior cannot be predicted, no guarantees
   * are made regarding determinism.
   *
   * @see #isAssignable(Type, Type)
   */
  public final boolean anyIsAssignable(final Type receiverType,
                                       final Collection<? extends Type> payloadTypes) {
    if (payloadTypes != null) {
      for (final Type payloadType : payloadTypes) {
        if (this.isAssignable(receiverType, payloadType)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if an object bearing a {@link Type} equal to
   * the supplied {@code payloadType} can be assigned to an object
   * reference bearing a {@link Type} equal to the supplied {@code
   * receiverType}, according to the semantics represented by the
   * return value of an invocation of the {@link
   * #getSemanticsFor(Type, Type)} method with the given parameter
   * values.
   *
   * @param receiverType a {@link Type} borne by a reference for which
   * an object bearing the supplied {@code payloadType} is destined;
   * may be {@code null}
   *
   * @param payloadType a {@link Type} borne by an object destined for
   * a reference bearing the supplied {@code receiverType}; may be
   * {@code null}
   *
   * @return {@code true} if an object bearing a {@link Type} equal to
   * the supplied {@code payloadType} can be assigned to an object
   * reference bearing a {@link Type} equal to the supplied {@code
   * receiverType}, according to the semantics represented by the
   * return value of an invocation of the {@link
   * #getSemanticsFor(Type, Type)} method with the given parameter
   * values; {@code false} in all other cases
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.  Because the {@link
   * #getSemanticsFor(Type, Type)} method may be overridden, and
   * because therefore its behavior cannot be predicted, no guarantees
   * are made regarding determinism.
   */
  public final boolean isAssignable(final Type receiverType,
                                    final Type payloadType) {
    return this.isAssignable(receiverType, payloadType, this.getSemanticsFor(receiverType, payloadType));
  }

  /**
   * Returns {@code true} if an object bearing a {@link Type} equal to
   * the supplied {@code payloadType} can be assigned to an object
   * reference bearing a {@link Type} equal to the supplied {@code
   * receiverType}, according to the semantics represented by the
   * supplied {@link TypeSemantics}, which most commonly is this very
   * {@link TypeSemantics} instance.
   *
   * @param receiverType a {@link Type} borne by a reference for which
   * an object bearing the supplied {@code payloadType} is destined;
   * may be {@code null}
   *
   * @param payloadType a {@link Type} borne by an object destined for
   * a reference bearing the supplied {@code receiverType}; may be
   * {@code null}
   *
   * @param semantics the {@link TypeSemantics} in effect; may be
   * {@code null} in which case this {@link TypeSemantics} will be
   * used instead
   *
   * @return {@code true} if an object bearing a {@link Type} equal to
   * the supplied {@code payloadType} can be assigned to an object
   * reference bearing a {@link Type} equal to the supplied {@code
   * receiverType}, according to the semantics represented by the
   * return value of an invocation of the {@link
   * #getSemanticsFor(Type, Type)} method with the given parameter
   * values; {@code false} in all other cases
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.  Because the behavior of
   * the supplied {@link TypeSemantics} cannot be predicted, no
   * guarantees are made regarding determinism.
   */
  private final boolean isAssignable(final Type receiverType,
                                     final Type payloadType,
                                     final TypeSemantics semantics) {
    final boolean returnValue;
    if (semantics == this || semantics == null) {
      if (receiverType == null || payloadType == null) {
        returnValue = false;
      } else if (receiverType == payloadType) {
        returnValue = true;
      } else if (receiverType instanceof Class) {
        if (payloadType instanceof Class) {
          returnValue = this.isAssignable((Class<?>)receiverType, (Class<?>)payloadType);
        } else if (payloadType instanceof ParameterizedType) {
          returnValue = this.isAssignable((Class<?>)receiverType, (ParameterizedType)payloadType);
        } else if (payloadType instanceof GenericArrayType) {
          returnValue = this.isAssignable((Class<?>)receiverType, (GenericArrayType)payloadType);
        } else if (payloadType instanceof TypeVariable) {
          returnValue = this.isAssignable((Class<?>)receiverType, (TypeVariable<?>)payloadType);
        } else if (payloadType instanceof WildcardType) {
          returnValue = this.isAssignable((Class<?>)receiverType, (WildcardType)payloadType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof ParameterizedType) {
        if (payloadType instanceof Class) {
          returnValue = this.isAssignable((ParameterizedType)receiverType, (Class<?>)payloadType);
        } else if (payloadType instanceof ParameterizedType) {
          returnValue = this.isAssignable((ParameterizedType)receiverType, (ParameterizedType)payloadType);
        } else if (payloadType instanceof GenericArrayType) {
          returnValue = this.isAssignable((ParameterizedType)receiverType, (GenericArrayType)payloadType);
        } else if (payloadType instanceof TypeVariable) {
          returnValue = this.isAssignable((ParameterizedType)receiverType, (TypeVariable<?>)payloadType);
        } else if (payloadType instanceof WildcardType) {
          returnValue = this.isAssignable((ParameterizedType)receiverType, (WildcardType)payloadType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof GenericArrayType) {
        if (payloadType instanceof Class) {
          returnValue = this.isAssignable((GenericArrayType)receiverType, (Class<?>)payloadType);
        } else if (payloadType instanceof ParameterizedType) {
          returnValue = this.isAssignable((GenericArrayType)receiverType, (ParameterizedType)payloadType);
        } else if (payloadType instanceof GenericArrayType) {
          returnValue = this.isAssignable((GenericArrayType)receiverType, (GenericArrayType)payloadType);
        } else if (payloadType instanceof TypeVariable) {
          returnValue = this.isAssignable((GenericArrayType)receiverType, (TypeVariable<?>)payloadType);
        } else if (payloadType instanceof WildcardType) {
          returnValue = this.isAssignable((GenericArrayType)receiverType, (WildcardType)payloadType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof TypeVariable) {
        if (payloadType instanceof Class) {
          returnValue = this.isAssignable((TypeVariable<?>)receiverType, (Class<?>)payloadType);
        } else if (payloadType instanceof ParameterizedType) {
          returnValue = this.isAssignable((TypeVariable<?>)receiverType, (ParameterizedType)payloadType);
        } else if (payloadType instanceof GenericArrayType) {
          returnValue = this.isAssignable((TypeVariable<?>)receiverType, (GenericArrayType)payloadType);
        } else if (payloadType instanceof TypeVariable) {
          returnValue = this.isAssignable((TypeVariable<?>)receiverType, (TypeVariable<?>)payloadType);
        } else if (payloadType instanceof WildcardType) {
          returnValue = this.isAssignable((TypeVariable<?>)receiverType, (WildcardType)payloadType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof WildcardType) {
        if (payloadType instanceof Class) {
          returnValue = this.isAssignable((WildcardType)receiverType, (Class<?>)payloadType);
        } else if (payloadType instanceof ParameterizedType) {
          returnValue = this.isAssignable((WildcardType)receiverType, (ParameterizedType)payloadType);
        } else if (payloadType instanceof GenericArrayType) {
          returnValue = this.isAssignable((WildcardType)receiverType, (GenericArrayType)payloadType);
        } else if (payloadType instanceof TypeVariable) {
          returnValue = this.isAssignable((WildcardType)receiverType, (TypeVariable<?>)payloadType);
        } else if (payloadType instanceof WildcardType) {
          returnValue = this.isAssignable((WildcardType)receiverType, (WildcardType)payloadType);
        } else {
          returnValue = false;
        }
      } else {
        returnValue = false;
      }
    } else {
      returnValue = semantics.isAssignable(receiverType, payloadType); // yes, two argument
    }
    return returnValue;
  }

  protected boolean isAssignable(final Class<?> receiverType,
                                 final Class<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final Class<?> receiverType,
                                 final ParameterizedType payloadType) {
    return false;
  }

  protected boolean isAssignable(final Class<?> receiverType,
                                 final GenericArrayType payloadType) {
    return false;
  }

  protected boolean isAssignable(final Class<?> receiverType,
                                 final TypeVariable<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final Class<?> receiverType,
                                 final WildcardType payloadType) {
    return false;
  }

  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final Class<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final ParameterizedType payloadType) {
    return false;
  }

  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final GenericArrayType payloadType) {
    return false;
  }

  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final TypeVariable<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final ParameterizedType receiverType,
                                 final WildcardType payloadType) {
    return false;
  }

  protected boolean isAssignable(final GenericArrayType receiverType,
                                 final Class<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final GenericArrayType receiverType,
                                 final ParameterizedType payloadType) {
    return false;
  }

  protected boolean isAssignable(final GenericArrayType receiverType,
                                 final GenericArrayType payloadType) {
    return this.isAssignable(receiverType.getGenericComponentType(), payloadType.getGenericComponentType(), this);
  }

  protected boolean isAssignable(final GenericArrayType receiverType,
                                 final TypeVariable<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final GenericArrayType receiverType,
                                 final WildcardType payloadType) {
    return false;
  }

  protected boolean isAssignable(final TypeVariable<?> receiverType,
                                 final Class<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final TypeVariable<?> receiverType,
                                 final ParameterizedType payloadType) {
    return false;
  }

  protected boolean isAssignable(final TypeVariable<?> receiverType,
                                 final GenericArrayType payloadType) {
    return false;
  }

  protected boolean isAssignable(final TypeVariable<?> receiverType,
                                 final TypeVariable<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final TypeVariable<?> receiverType,
                                 final WildcardType payloadType) {
    return false;
  }

  protected boolean isAssignable(final WildcardType receiverType,
                                 final Class<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final WildcardType receiverType,
                                 final ParameterizedType payloadType) {
    return false;
  }

  protected boolean isAssignable(final WildcardType receiverType,
                                 final GenericArrayType payloadType) {
    return false;
  }

  protected boolean isAssignable(final WildcardType receiverType,
                                 final TypeVariable<?> payloadType) {
    return false;
  }

  protected boolean isAssignable(final WildcardType receiverType,
                                 final WildcardType payloadType) {
    return false;
  }

  // Sort of like least upper bound (lub()) in the JLS; see
  // https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.10.4
  @Experimental
  @Incomplete
  public final Type getNearestAncestor(final Type t0, final Type t1) {
    if (t0 == null) {
      return t1 == null ? null : t1;
    } else if (t1 == null) {
      return t0;
    } else if (t0 == t1 || Types.equals(t0, t1)) {
      return t0;
    } else if (this.isAssignable(t0, t1)) {
      assert !this.isAssignable(t1, t0);
      return t0;
    } else if (this.isAssignable(t1, t0)) {
      return t1;
    } else {
      final Class<?> c0 = this.box(Types.erase(t0));
      final Class<?> c1 = this.box(Types.erase(t1));
      if (c0 == null || c1 == null) {
        return null;
      } else {
        assert !c0.isAssignableFrom(c1);
        assert !c1.isAssignableFrom(c0);
        if (c0.isArray()) {
          if (c1.isArray()) {
            assert Object[].class.isAssignableFrom(c0);
            assert Object[].class.isAssignableFrom(c1);
            return Object[].class;
          } else {
            return c1.isPrimitive() ? null : Object.class;
          }
        } else if (c0.isPrimitive()) {
          return null;
        } else if (c1.isArray()) {
          return Object.class;
        } else if (c1.isPrimitive()) {
          return null;
        } else {
          // Two scalar reference types not assignable to each other.
          // TODO: examine superclasses, interfaces, compare
          return Object.class;
        }
      }
    }
  }

  
  
}
