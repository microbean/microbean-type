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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static org.microbean.type.ConstantDescs.CD_DefaultGenericArrayType;
import static org.microbean.type.ConstantDescs.CD_Type;

/**
 * A {@link GenericArrayType} implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see GenericArrayType
 */
public final class DefaultGenericArrayType implements Constable, GenericArrayType {


  /*
   * Instance fields.
   */


  private final Type genericComponentType;

  private final int hashCode;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link DefaultGenericArrayType}.
   *
   * @param genericComponentType the generic component type; must not
   * be {@code null}; should almost certainly be a {@link
   * GenericArrayType}, a {@link java.lang.reflect.ParameterizedType}
   * or a {@link java.lang.reflect.TypeVariable}
   *
   * @exception NullPointerException if {@code genericComponentType}
   * is {@code null}
   */
  public DefaultGenericArrayType(final Type genericComponentType) {
    super();
    this.genericComponentType = genericComponentType;
    this.hashCode = this.computeHashCode();
  }

  DefaultGenericArrayType(final Type rawType, final Type argument) {
    this(new DefaultParameterizedType(null, rawType, argument));
  }


  /*
   * Instance methods.
   */


  @Override // GenericArrayType
  public final Type getGenericComponentType() {
    return this.genericComponentType;
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final Optional<? extends ConstantDesc> genericComponentType = JavaTypes.describeConstable(this.getGenericComponentType());
    if (genericComponentType.isPresent()) {
      return
        Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                           MethodHandleDesc.ofConstructor(CD_DefaultGenericArrayType,
                                                                          CD_Type),
                                           genericComponentType.orElseThrow()));
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
    } else if (other instanceof GenericArrayType g) {
      return JavaTypes.equals(this, g);
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
   * If the supplied {@link GenericArrayType} is a {@link
   * DefaultGenericArrayType}, returns it; otherwise creates a new
   * {@link DefaultGenericArrayType} with the supplied {@link
   * GenericArrayType}'s {@linkplain
   * GenericArrayType#getGenericComponentType() generic component
   * type} and returns it.
   *
   * @param genericArrayType the {@link GenericArrayType} to
   * effectively copy (or return); must not be {@code null}
   *
   * @return a non-{@code null} {@link DefaultGenericArrayType}
   *
   * @exception NullPointerException if {@code genericArrayType} is
   * {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link DefaultGenericArrayType} with
   * each invocation).
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final DefaultGenericArrayType of(final GenericArrayType genericArrayType) {
    if (genericArrayType instanceof DefaultGenericArrayType) {
      return (DefaultGenericArrayType)genericArrayType;
    } else {
      return new DefaultGenericArrayType(genericArrayType.getGenericComponentType());
    }
  }

}
