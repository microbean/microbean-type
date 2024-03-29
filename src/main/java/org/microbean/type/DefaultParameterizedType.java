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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.DEFAULT_NAME;

import static org.microbean.type.ConstantDescs.CD_DefaultParameterizedType;
import static org.microbean.type.ConstantDescs.CD_Type;

/**
 * A {@link ParameterizedType} implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ParameterizedType
 */
public final class DefaultParameterizedType implements Constable, ParameterizedType {


  /*
   * Instance fields.
   */


  private final Type ownerType;

  private final Type rawType;

  private final Type[] actualTypeArguments;

  private final int hashCode;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param ownerType the {@link Type} that owns this {@link
   * DefaultParameterizedType}; may be (and usually is) {@code null}
   *
   * @param rawType the raw type; must not be {@code null}; is most
   * commonly a {@link Class} as in all JDKs through at least 17 and
   * as mandated currently by the Java Language Specification
   *
   * @param actualTypeArgument the actual {@linkplain
   * #getActualTypeArguments() actual type argument} of this {@link
   * DefaultParameterizedType}; must not be {@code null}
   *
   * @exception NullPointerException if {@code rawType} or {@code
   * actualTypeArgument} is {@code null}
   *
   * @exception IllegalArgumentException if {@code rawType} is an
   * {@linkplain Class#isInstance(Object) instance of} {@link Class}
   * and the length of the array returned by its {@link
   * Class#getTypeParameters()} method is not {@code 1}
   */
  public DefaultParameterizedType(final Type ownerType, final Type rawType, final Type actualTypeArgument) {
    super();
    this.ownerType = ownerType;
    this.rawType = Objects.requireNonNull(rawType, "rawType");
    if (rawType instanceof Class<?> cls && cls.getTypeParameters().length != 1) {
      throw new IllegalArgumentException("rawType: " + JavaTypes.toString(rawType) +
                                         "; actualTypeArguments: " + JavaTypes.toString(actualTypeArgument));
    }
    this.actualTypeArguments = new Type[] { Objects.requireNonNull(actualTypeArgument, "actualTypeArgument") };
    this.hashCode = this.computeHashCode();
  }

  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param ownerType the {@link Type} that owns this {@link
   * DefaultParameterizedType}; may be (and usually is) {@code null}
   *
   * @param rawType the raw type; must not be {@code null}; is most
   * commonly a {@link Class} as in all JDKs through at least 17 and
   * as mandated currently by the Java Language Specification
   *
   * @param actualTypeArgument0 the first actual {@linkplain
   * #getActualTypeArguments() actual type argument} of this {@link
   * DefaultParameterizedType}; must not be {@code null}
   *
   * @param actualTypeArgument1 the second actual {@linkplain
   * #getActualTypeArguments() actual type argument} of this {@link
   * DefaultParameterizedType}; must not be {@code null}
   *
   * @exception NullPointerException if {@code rawType} or any type
   * argument is {@code null}
   *
   * @exception IllegalArgumentException if {@code rawType} is an
   * {@linkplain Class#isInstance(Object) instance of} {@link Class}
   * and the length of the array returned by its {@link
   * Class#getTypeParameters()} method is not {@code 2}
   */
  public DefaultParameterizedType(final Type ownerType,
                                  final Type rawType,
                                  final Type actualTypeArgument0,
                                  final Type actualTypeArgument1) {
    super();
    this.ownerType = ownerType;
    this.rawType = Objects.requireNonNull(rawType, "rawType");
    if (rawType instanceof Class<?> cls && cls.getTypeParameters().length != 2) {
      throw new IllegalArgumentException("rawType: " + JavaTypes.toString(rawType) +
                                         "; actualTypeArguments: " + List.of(actualTypeArgument0, actualTypeArgument1));
    }
    this.actualTypeArguments = new Type[] {
      Objects.requireNonNull(actualTypeArgument0, "actualTypeArgument0"),
      Objects.requireNonNull(actualTypeArgument1, "actualTypeArgument1")
    };
    this.hashCode = this.computeHashCode();
  }

  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param ownerType the {@link Type} that owns this {@link
   * DefaultParameterizedType}; may be (and usually is) {@code null}
   *
   * @param rawType the raw type; must not be {@code null}; is most
   * commonly a {@link Class} as in all JDKs through at least 17 and
   * as mandated currently by the Java Language Specification
   *
   * @param actualTypeArguments the actual {@linkplain
   * #getActualTypeArguments() actual type arguments} of this {@link
   * DefaultParameterizedType}; may be {@code null} in which case a
   * zero-length array will be used instead; will be cloned if
   * non-{@code null} and if its length is greater than {@code 0}
   *
   * @exception NullPointerException if {@code rawType} is {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code rawType} is an
   * {@linkplain Class#isInstance(Object) instance of} {@link Class}
   * and the length of the array returned by its {@link
   * Class#getTypeParameters()} method is not the same as the length
   * of the {@link actualTypeArguments} array (or {@code 0} if the
   * {@code actualTypeArguments} array is {@code null})
   */

  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param ownerType the {@link Type} that owns this {@link
   * DefaultParameterizedType}; may be (and usually is) {@code null}
   *
   * @param rawType the raw type; must not be {@code null}; is most
   * commonly a {@link Class} as in all JDKs through at least 17 and
   * as mandated currently by the Java Language Specification
   *
   * @param actualTypeArgument0 the first actual {@linkplain
   * #getActualTypeArguments() actual type argument} of this {@link
   * DefaultParameterizedType}; must not be {@code null}
   *
   * @param actualTypeArgument1 the second actual {@linkplain
   * #getActualTypeArguments() actual type argument} of this {@link
   * DefaultParameterizedType}; must not be {@code null}
   *
   * @param actualTypeArgument2 the third actual {@linkplain
   * #getActualTypeArguments() actual type argument} of this {@link
   * DefaultParameterizedType}; must not be {@code null}
   *
   * @exception NullPointerException if {@code rawType} or any type
   * argument is {@code null}
   *
   * @exception IllegalArgumentException if {@code rawType} is an
   * {@linkplain Class#isInstance(Object) instance of} {@link Class}
   * and the length of the array returned by its {@link
   * Class#getTypeParameters()} method is not {@code 3}
   */
  public DefaultParameterizedType(final Type ownerType,
                                  final Type rawType,
                                  final Type actualTypeArgument0,
                                  final Type actualTypeArgument1,
                                  final Type actualTypeArgument2) {
    
    super();
    this.ownerType = ownerType;
    this.rawType = Objects.requireNonNull(rawType, "rawType");
    if (rawType instanceof Class<?> cls && cls.getTypeParameters().length != 3) {
      throw new IllegalArgumentException("rawType: " + JavaTypes.toString(rawType) +
                                         "; actualTypeArguments: " +
                                         List.of(actualTypeArgument0, actualTypeArgument1, actualTypeArgument2));
    }
    this.actualTypeArguments = new Type[] {
      Objects.requireNonNull(actualTypeArgument0, "actualTypeArgument0"),
      Objects.requireNonNull(actualTypeArgument1, "actualTypeArgument1"),
      Objects.requireNonNull(actualTypeArgument2, "actualTypeArgument2")
    };
    this.hashCode = this.computeHashCode();
  }
  
  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param ownerType the {@link Type} that owns this {@link
   * DefaultParameterizedType}; may be (and usually is) {@code null}
   *
   * @param rawType the raw type; must not be {@code null}; is most
   * commonly a {@link Class} as in all JDKs through at least 17 and
   * as mandated currently by the Java Language Specification
   *
   * @param actualTypeArguments the actual {@linkplain
   * #getActualTypeArguments() actual type arguments} of this {@link
   * DefaultParameterizedType}; may be {@code null} in which case a
   * zero-length array will be used instead; will be cloned if
   * non-{@code null} and if its length is greater than {@code 0}
   *
   * @exception NullPointerException if {@code rawType} is {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code rawType} is an
   * {@linkplain Class#isInstance(Object) instance of} {@link Class}
   * and the length of the array returned by its {@link
   * Class#getTypeParameters()} method is not the same as the length
   * of the {@link actualTypeArguments} array (or {@code 0} if the
   * {@code actualTypeArguments} array is {@code null})
   */
  public DefaultParameterizedType(final Type ownerType, final Type rawType, final Type... actualTypeArguments) {
    super();
    this.ownerType = ownerType;
    this.rawType = Objects.requireNonNull(rawType, "rawType");
    if (actualTypeArguments == null || actualTypeArguments.length <= 0) {
      this.actualTypeArguments = JavaTypes.emptyTypeArray();
    } else {
      this.actualTypeArguments = actualTypeArguments.clone();
    }
    if (rawType instanceof Class<?> cls) {
      final Type[] typeParameters = cls.getTypeParameters();
      if (typeParameters.length != this.actualTypeArguments.length) {
        throw new IllegalArgumentException("rawType: " + JavaTypes.toString(rawType) +
                                           "; actualTypeArguments: " + Arrays.asList(actualTypeArguments));
      }
    }
    this.hashCode = this.computeHashCode();
  }

  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param other the {@link ParameterizedType} to copy information
   * from; must not be {@code null}
   *
   * @exception NullPointerException if {@code other} is {@code null}
   *
   * @see #DefaultParameterizedType(Type, Type, Type...)
   */
  public DefaultParameterizedType(final ParameterizedType other) {
    this(other.getOwnerType(), other.getRawType(), other.getActualTypeArguments());
  }


  /*
   * Instance methods.
   */


  @Override // ParameterizedType
  public final Type getOwnerType() {
    return this.ownerType;
  }

  @Override // ParameterizedType
  public final Type getRawType() {
    return this.rawType;
  }

  @Override // ParameterizedType
  public final Type[] getActualTypeArguments() {
    return this.actualTypeArguments.clone();
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final Optional<? extends ConstantDesc> ownerType = JavaTypes.describeConstable(this.getOwnerType());
    if (ownerType.isPresent()) {
      final Optional<? extends ConstantDesc> rawType = JavaTypes.describeConstable(this.getRawType());
      if (rawType.isPresent()) {
        final Type[] actualTypeArguments = this.getActualTypeArguments();
        final int bsmInvokeArgumentsLength = actualTypeArguments.length + 3;
        final ConstantDesc[] bsmInvokeArguments = new ConstantDesc[bsmInvokeArgumentsLength];
        bsmInvokeArguments[0] =
          MethodHandleDesc.ofConstructor(CD_DefaultParameterizedType,
                                         CD_Type,
                                         CD_Type,
                                         CD_Type.arrayType());
        bsmInvokeArguments[1] = ownerType.orElseThrow();
        bsmInvokeArguments[2] = rawType.orElseThrow();
        for (int i = 3; i < bsmInvokeArgumentsLength; i++) {
          final Optional<? extends ConstantDesc> arg = JavaTypes.describeConstable(actualTypeArguments[i - 3]);
          if (arg.isEmpty()) {
            return Optional.empty();
          }
          bsmInvokeArguments[i] = arg.orElseThrow();
        }
        return Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                       DEFAULT_NAME,
                                                       CD_DefaultParameterizedType,
                                                       bsmInvokeArguments));
      }
    }
    return Optional.empty();
  }
  
  @Override // Object
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return JavaTypes.hashCode(this);
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof ParameterizedType p) {
      return JavaTypes.equals(this, p);
    } else {
      return false;
    }
  }

  @Override // Object
  public final String toString() {
    return JavaTypes.toString(this);
  }


  /*
   * Static methods.
   */


  /**
   * If the supplied {@link ParameterizedType} is a {@link
   * DefaultParameterizedType}, returns it; otherwise creates a new
   * {@link DefaultParameterizedType} with the supplied {@link
   * ParameterizedType}'s {@linkplain ParameterizedType#getOwnerType()
   * owner type}, {@linkplain ParameterizedType#getRawType() raw type}
   * and {@linkplain ParameterizedType#getActualTypeArguments() type
   * arguments} and returns it.
   *
   * @param parameterizedType the {@link ParameterizedType} to
   * effectively copy (or return); must not be {@code null}
   *
   * @return a non-{@code null} {@link DefaultParameterizedType}
   *
   * @exception NullPointerException if {@code parameterizedType} is
   * {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link DefaultParameterizedType} with
   * each invocation).
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final DefaultParameterizedType of(final ParameterizedType parameterizedType) {
    if (parameterizedType instanceof DefaultParameterizedType p) {
      return p;
    } else {
      return new DefaultParameterizedType(parameterizedType);
    }
  }


}
