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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
public final class DefaultGenericArrayType extends AbstractType implements GenericArrayType {

  private static final long serialVersionUID = 1L;
  
  private transient Type genericComponentType;

  private transient int hashCode;

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
  
  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    final Object genericComponentType = stream.readObject();
    if (genericComponentType == null) {
      throw new IOException(new NullPointerException("genericComponentType"));
    } else if (!(genericComponentType instanceof Type)) {
      throw new IOException(new IllegalArgumentException("genericComponentType: " + genericComponentType));
    }
    this.genericComponentType = (Type)genericComponentType;
    this.hashCode = genericComponentType.hashCode();
  }
  
  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(AbstractType.toSerializableType(this.getGenericComponentType()));
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
