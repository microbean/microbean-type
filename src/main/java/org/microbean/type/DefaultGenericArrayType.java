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

import java.util.Objects;

/**
 * A {@link GenericArrayType} implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see GenericArrayType
 */
public final class DefaultGenericArrayType implements GenericArrayType {

  private final Type genericComponentType;

  private final int hashCode;

  /**
   * Creates a new {@link DefaultGenericArrayType}.
   *
   * @param genericComponentType the generic component type; must not
   * be {@code null}; should almost certainly be a {@link
   * ParameterizedType}
   */
  public DefaultGenericArrayType(final Type genericComponentType) {
    super();
    this.genericComponentType = Objects.requireNonNull(genericComponentType);
    this.hashCode = genericComponentType.hashCode();
  }

  DefaultGenericArrayType(final Class<?> rawType, final Type... actualTypeArguments) {
    this(new DefaultParameterizedType(rawType == null ? null : rawType.getDeclaringClass(),
                                      rawType,
                                      actualTypeArguments));
  }

  @Override
  public final Type getGenericComponentType() {
    return this.genericComponentType;
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof GenericArrayType) {
      final GenericArrayType her = (GenericArrayType)other;
      return this.getGenericComponentType().equals(her.getGenericComponentType());
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return this.getGenericComponentType().getTypeName() + "[]";

  }
  
}
