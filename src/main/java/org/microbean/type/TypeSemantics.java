/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020 microBean™.
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

/**
 * An object representing {@link Type} compatibility and assignability
 * semantics.
 *
 * <p>Subclasses that do not override anything essentially return
 * {@code false} for all assignability tests and are rarely
 * appropriate for much.</p>
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
    boolean returnValue = false;
    if (payloadTypes != null && !payloadTypes.isEmpty()) {
      for (final Type payloadType : payloadTypes) {
        if (this.isAssignable(receiverType, payloadType)) {
          returnValue = true;
          break;
        }
      }
    }
    return returnValue;
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
    if (semantics == null || semantics == this) {
      if (receiverType == null || payloadType == null) {
        returnValue = false;
      } else if (receiverType instanceof Class receiverClass) {
        if (payloadType instanceof Class<?> payloadClass) {
          returnValue = this.isAssignable(receiverClass, payloadClass);
        } else if (payloadType instanceof ParameterizedType payloadParameterizedType) {
          returnValue = this.isAssignable(receiverClass, payloadParameterizedType);
        } else if (payloadType instanceof GenericArrayType payloadGenericArrayType) {
          returnValue = this.isAssignable(receiverClass, payloadGenericArrayType);
        } else if (payloadType instanceof TypeVariable<?> payloadTypeVariable) {
          returnValue = this.isAssignable(receiverClass, payloadTypeVariable);
        } else if (payloadType instanceof WildcardType payloadWildcardType) {
          returnValue = this.isAssignable(receiverClass, payloadWildcardType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof ParameterizedType receiverParameterizedType) {
        if (payloadType instanceof Class<?> payloadClass) {
          returnValue = this.isAssignable(receiverParameterizedType, payloadClass);
        } else if (payloadType instanceof ParameterizedType payloadParameterizedType) {
          returnValue = this.isAssignable(receiverParameterizedType, payloadParameterizedType);
        } else if (payloadType instanceof GenericArrayType payloadGenericArrayType) {
          returnValue = this.isAssignable(receiverParameterizedType, payloadGenericArrayType);
        } else if (payloadType instanceof TypeVariable<?> payloadTypeVariable) {
          returnValue = this.isAssignable(receiverParameterizedType, payloadTypeVariable);
        } else if (payloadType instanceof WildcardType payloadWildcardType) {
          returnValue = this.isAssignable(receiverParameterizedType, payloadWildcardType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof GenericArrayType receiverGenericArrayType) {
        if (payloadType instanceof Class<?> payloadClass) {
          returnValue = this.isAssignable(receiverGenericArrayType, payloadClass);
        } else if (payloadType instanceof ParameterizedType payloadParameterizedType) {
          returnValue = this.isAssignable(receiverGenericArrayType, payloadParameterizedType);
        } else if (payloadType instanceof GenericArrayType payloadGenericArrayType) {
          returnValue = this.isAssignable(receiverGenericArrayType, payloadGenericArrayType);
        } else if (payloadType instanceof TypeVariable<?> payloadTypeVariable) {
          returnValue = this.isAssignable(receiverGenericArrayType, payloadTypeVariable);
        } else if (payloadType instanceof WildcardType payloadWildcardType) {
          returnValue = this.isAssignable(receiverGenericArrayType, payloadWildcardType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof TypeVariable<?> receiverTypeVariable) {
        if (payloadType instanceof Class<?> payloadClass) {
          returnValue = this.isAssignable(receiverTypeVariable, payloadClass);
        } else if (payloadType instanceof ParameterizedType payloadParameterizedType) {
          returnValue = this.isAssignable(receiverTypeVariable, payloadParameterizedType);
        } else if (payloadType instanceof GenericArrayType payloadGenericArrayType) {
          returnValue = this.isAssignable(receiverTypeVariable, payloadGenericArrayType);
        } else if (payloadType instanceof TypeVariable<?> payloadTypeVariable) {
          returnValue = this.isAssignable(receiverTypeVariable, payloadTypeVariable);
        } else if (payloadType instanceof WildcardType payloadWildcardType) {
          returnValue = this.isAssignable(receiverTypeVariable, payloadWildcardType);
        } else {
          returnValue = false;
        }
      } else if (receiverType instanceof WildcardType receiverWildcardType) {
        if (payloadType instanceof Class<?> payloadClass) {
          returnValue = this.isAssignable(receiverWildcardType, payloadClass);
        } else if (payloadType instanceof ParameterizedType payloadParameterizedType) {
          returnValue = this.isAssignable(receiverWildcardType, payloadParameterizedType);
        } else if (payloadType instanceof GenericArrayType payloadGenericArrayType) {
          returnValue = this.isAssignable(receiverWildcardType, payloadGenericArrayType);
        } else if (payloadType instanceof TypeVariable<?> payloadTypeVariable) {
          returnValue = this.isAssignable(receiverWildcardType, payloadTypeVariable);
        } else if (payloadType instanceof WildcardType payloadWildcardType) {
          returnValue = this.isAssignable(receiverWildcardType, payloadWildcardType);
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

}
