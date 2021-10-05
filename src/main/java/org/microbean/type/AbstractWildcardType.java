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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.StringJoiner;

class AbstractWildcardType extends AbstractType implements WildcardType {

  private static final long serialVersionUID = 1L;

  private transient Type[] upperBounds;

  private transient Type[] lowerBounds;

  private transient int hashCode;

  AbstractWildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
    super();
    if (upperBounds == null || upperBounds.length <= 0) {
      this.upperBounds = new Type[] { Object.class };
    } else {
      this.upperBounds = upperBounds.clone();
    }
    if (lowerBounds == null || lowerBounds.length <= 0) {
      this.lowerBounds = EMPTY_TYPE_ARRAY;
    } else {
      this.lowerBounds = lowerBounds.clone();
    }
    this.hashCode = this.computeHashCode();
  }

  AbstractWildcardType(final WildcardType other) {
    this(other.getUpperBounds(), other.getLowerBounds());
  }

  @Override
  public final Type[] getUpperBounds() {
    return this.upperBounds.clone();
  }

  @Override
  public final Type[] getLowerBounds() {
    return this.lowerBounds.clone();
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return Arrays.hashCode(this.upperBounds) ^ Arrays.hashCode(this.lowerBounds);
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof WildcardType) {
      final WildcardType her = (WildcardType)other;
      return
        Arrays.equals(this.upperBounds, her.getUpperBounds()) &&
        Arrays.equals(this.lowerBounds, her.getLowerBounds());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("?");
    Type[] bounds = this.lowerBounds;
    if (bounds == null || bounds.length <= 0) {
      // Upper bounds only.
      bounds = this.upperBounds;
      if (bounds == null || bounds.length <= 0 || Object.class.equals(bounds[0])) {
        bounds = null;
      } else {
        sb.append(" extends ");
      }
    } else {
      // Lower bounds only.
      sb.append(" super ");
    }
    if (bounds != null) {
      assert bounds.length > 0;
      final StringJoiner sj = new StringJoiner(" & ");
      for (final Type bound: bounds) {
        sj.add(String.valueOf(bound.getTypeName()));
      }
      sb.append(sj.toString());
    }
    return sb.toString();
  }

  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    final Object lowerBounds = stream.readObject();
    assert lowerBounds instanceof Serializable[];
    Serializable[] serializableBounds = (Serializable[])lowerBounds;
    if (serializableBounds == null || serializableBounds.length <= 0) {
      this.lowerBounds = EMPTY_TYPE_ARRAY;
    } else {
      this.lowerBounds = new Type[serializableBounds.length];
      System.arraycopy(serializableBounds, 0, this.lowerBounds, 0, serializableBounds.length);
    }
    final Object upperBounds = stream.readObject();
    assert upperBounds instanceof Serializable[];
    serializableBounds = (Serializable[])upperBounds;
    if (serializableBounds == null || serializableBounds.length <= 0) {
      this.upperBounds = new Type[] { Object.class };
    } else if (serializableBounds.length == 1) {
      final Object bound = serializableBounds[0];
      if (bound == null || Object.class.equals(bound)) {
        this.upperBounds = new Type[] { Object.class };
      } else {
        assert bound instanceof Type;
        this.upperBounds = new Type[] { (Type)bound };
      }
    } else {
      this.upperBounds = new Type[serializableBounds.length];
      System.arraycopy(serializableBounds, 0, this.upperBounds, 0, serializableBounds.length);
    }
    // Note: we use the instance variables directly, not the
    // accessors, so we don't compute hashcodes off of clones.
    this.hashCode = this.computeHashCode();
  }

  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Type[] originalBounds = this.lowerBounds;
    if (originalBounds == null || originalBounds.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newBounds = new Serializable[originalBounds.length];
      for (int i = 0; i < newBounds.length; i++) {
        newBounds[i] = Types.toSerializableType(originalBounds[i]);
      }
      stream.writeObject(newBounds);
    }
    originalBounds = this.upperBounds;
    if (originalBounds == null || originalBounds.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newBounds = new Serializable[originalBounds.length];
      for (int i = 0; i < newBounds.length; i++) {
        newBounds[i] = Types.toSerializableType(originalBounds[i]);
      }
      stream.writeObject(newBounds);
    }
  }

  public static final AbstractWildcardType valueOf(final WildcardType type) {
    if (type == null) {
      return null;
    } else if (type instanceof AbstractWildcardType) {
      return (AbstractWildcardType)type;
    } else {
      return new AbstractWildcardType(type);
    }
  }

}
