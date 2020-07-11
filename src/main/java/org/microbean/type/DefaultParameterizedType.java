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
public final class DefaultParameterizedType implements ParameterizedType {

  private final Type ownerType;

  private final Type rawType;

  private final Type[] actualTypeArguments;

  private final int hashCode;

  /**
   * Creates a new {@link DefaultParameterizedType}.
   *
   * @param ownerType the {@link Type} that owns this {@link
   * DefaultParameterizedType}; may be (and usually is) {@code null};
   * if non-{@code null} is most commonly a {@link Class} as in all
   * JDKs through at least 13
   *
   * @param rawType the raw type; must not be {@code null}; is most commonly a {@link Class} as in all
   * JDKs through at least 13
   *
   * @param actualTypeArguments the actual {@linkplain
   * #getActualTypeArguments() actual type arguments} of this {@link
   * DefaultParameterizedType}; may be {@code null}
   *
   * @exception NullPointerException if {@code rawType} is {@code
   * null}
   */
  public DefaultParameterizedType(final Type ownerType, final Type rawType, final Type... actualTypeArguments) {
    super();
    this.ownerType = ownerType;
    this.rawType = Objects.requireNonNull(rawType);
    this.actualTypeArguments = actualTypeArguments;
    this.hashCode = this.computeHashCode();
  }

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
    return this.actualTypeArguments;
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    int hashCode = 17;

    final Object ownerType = this.getOwnerType();
    int c = ownerType == null ? 0 : ownerType.hashCode();
    hashCode = 37 * hashCode + c;

    final Object rawType = this.getRawType();
    c = rawType == null ? 0 : rawType.hashCode();
    hashCode = 37 * hashCode + c;

    final Type[] actualTypeArguments = this.getActualTypeArguments();
    c = Arrays.hashCode(actualTypeArguments);
    hashCode = 37 * hashCode + c;

    return hashCode;
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof ParameterizedType) {
      final ParameterizedType her = (ParameterizedType)other;

      final Object ownerType = this.getOwnerType();
      if (ownerType == null) {
        if (her.getOwnerType() != null) {
          return false;
        }
      } else if (!ownerType.equals(her.getOwnerType())) {
        return false;
      }

      final Object rawType = this.getRawType();
      if (rawType == null) {
        if (her.getRawType() != null) {
          return false;
        }
      } else if (!rawType.equals(her.getRawType())) {
        return false;
      }

      final Type[] actualTypeArguments = this.getActualTypeArguments();
      if (!Arrays.equals(actualTypeArguments, her.getActualTypeArguments())) {
        return false;
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Type rawType = this.getRawType();
    final Type ownerType = this.getOwnerType();
    if (ownerType == null) {
      sb.append(rawType.getTypeName());
    } else {
      sb.append(ownerType.getTypeName()).append("$");
      if (ownerType instanceof ParameterizedType) {
        final ParameterizedType ownerPType = (ParameterizedType)ownerType;
        final Type ownerRawType = ownerPType.getRawType();
        sb.append(rawType.getTypeName().replace(ownerRawType.getTypeName() + "$", ""));
      } else if (rawType instanceof Class) {
        sb.append(((Class<?>)rawType).getSimpleName());
      } else {
        sb.append(rawType.getTypeName());
      }
    }

    final Type[] actualTypeArguments = this.getActualTypeArguments();
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

}
