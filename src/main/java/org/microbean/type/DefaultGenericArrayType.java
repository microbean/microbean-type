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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * A {@link GenericArrayType} implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see GenericArrayType
 */
public final class DefaultGenericArrayType implements GenericArrayType {

  
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
   * java.lang.reflect.ParameterizedType} or a {@link
   * java.lang.reflect.TypeVariable}
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


  @Override
  public final Type getGenericComponentType() {
    return this.genericComponentType;
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return JavaTypes.hashCode(this);
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof GenericArrayType g) {
      return JavaTypes.equals(this, g);
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return JavaTypes.toString(this);
  }

  public static final DefaultGenericArrayType valueOf(final GenericArrayType genericArrayType) {
    if (genericArrayType == null) {
      return null;
    } else if (genericArrayType instanceof DefaultGenericArrayType) {
      return (DefaultGenericArrayType)genericArrayType;
    } else {
      return new DefaultGenericArrayType(genericArrayType.getGenericComponentType());
    }
  }

}
