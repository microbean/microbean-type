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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A {@link ParameterizedType} implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ParameterizedType
 */
public final class DefaultParameterizedType extends AbstractType implements ParameterizedType {


  /*
   * Static fields.
   */


  private static final long serialVersionUID = 2L;


  /*
   * Instance fields.
   */


  private transient Type ownerType;

  private transient Type rawType;

  private transient Type[] actualTypeArguments;

  private transient int hashCode;


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
   * commonly a {@link Class} as in all JDKs through at least 17
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
      this.actualTypeArguments = Types.emptyTypeArray();
    } else {
      this.actualTypeArguments = actualTypeArguments.clone();
    }
    if (rawType instanceof Class) {
      final Class<?> cls = (Class<?>)rawType;
      final Type[] typeParameters = cls.getTypeParameters();
      if (typeParameters.length != this.actualTypeArguments.length) {
        throw new IllegalArgumentException("rawType: " + Types.toString(rawType) +
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


  @Override
  public final Type getOwnerType() {
    return this.ownerType;
  }

  @Override
  public final Type getRawType() {
    return this.rawType;
  }

  @Override
  public final Type[] getActualTypeArguments() {
    return this.actualTypeArguments.clone();
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return Types.hashCode(this);
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof ParameterizedType) {
      final ParameterizedType her = (ParameterizedType)other;
      return
        Objects.equals(this.ownerType, her.getOwnerType()) &&
        Objects.equals(this.rawType, her.getRawType()) &&
        Arrays.equals(this.actualTypeArguments, her.getActualTypeArguments());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Type ownerType = this.ownerType;
    if (ownerType == null) {
      sb.append(this.rawType.getTypeName());
    } else {
      sb.append(ownerType.getTypeName()).append("$");
      final Type rawType = this.rawType;
      if (ownerType instanceof ParameterizedType) {
        sb.append(rawType.getTypeName().replace(((ParameterizedType)ownerType).getRawType().getTypeName() + "$", ""));
      } else if (rawType instanceof Class) {
        sb.append(((Class<?>)rawType).getSimpleName());
      } else {
        sb.append(rawType.getTypeName());
      }
    }
    final Type[] actualTypeArguments = this.actualTypeArguments;
    if (actualTypeArguments != null && actualTypeArguments.length > 0) {
      final StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
      stringJoiner.setEmptyValue("");
      for (final Type actualTypeArgument : actualTypeArguments) {
        stringJoiner.add(actualTypeArgument.getTypeName());
      }
      sb.append(stringJoiner.toString());
    }
    return sb.toString();
  }

  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    final Object ownerType = stream.readObject();
    if (ownerType == null) {
      this.ownerType = null;
    } else {
      this.ownerType = (Type)ownerType;
    }
    final Object rawType = stream.readObject();
    if (rawType == null) {
      throw new IOException(new NullPointerException("rawType"));
    } else {
      this.rawType = (Type)rawType;
    }
    final Serializable[] serializableActualTypeArguments = (Serializable[])stream.readObject();
    if (serializableActualTypeArguments == null || serializableActualTypeArguments.length <= 0) {
      this.actualTypeArguments = Types.emptyTypeArray();
    } else {
      this.actualTypeArguments = new Type[serializableActualTypeArguments.length];
      System.arraycopy(serializableActualTypeArguments, 0, this.actualTypeArguments, 0, serializableActualTypeArguments.length);
    }
    this.hashCode = this.computeHashCode();
  }

  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(Types.toSerializableType(this.ownerType));
    stream.writeObject(Types.toSerializableType(this.rawType));
    final Type[] actualTypeArguments = this.actualTypeArguments;
    if (actualTypeArguments == null || actualTypeArguments.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newTypeArguments = new Serializable[actualTypeArguments.length];
      for (int i = 0; i < newTypeArguments.length; i++) {
        newTypeArguments[i] = Types.toSerializableType(actualTypeArguments[i]);
      }
      stream.writeObject(newTypeArguments);
    }
  }


  /*
   * Static methods.
   */


  public static final DefaultParameterizedType valueOf(final ParameterizedType type) {
    if (type == null) {
      return null;
    } else if (type instanceof DefaultParameterizedType) {
      return (DefaultParameterizedType)type;
    } else {
      return new DefaultParameterizedType(type);
    }
  }


}
