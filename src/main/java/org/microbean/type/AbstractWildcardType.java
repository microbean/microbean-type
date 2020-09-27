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
    this.upperBounds = upperBounds == null || upperBounds.length <= 0 ? OBJECT : upperBounds;
    this.lowerBounds = lowerBounds == null ? EMPTY_TYPE_ARRAY : lowerBounds;
    this.hashCode = Arrays.hashCode(this.getUpperBounds()) ^ Arrays.hashCode(this.getLowerBounds());
  }

  AbstractWildcardType(final WildcardType other) {
    super();
    final Type[] upperBounds = other.getUpperBounds();
    this.upperBounds = upperBounds == null ? OBJECT : upperBounds;
    final Type[] lowerBounds = other.getLowerBounds();
    this.lowerBounds = lowerBounds == null ? EMPTY_TYPE_ARRAY : lowerBounds;
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

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof WildcardType) {
      final WildcardType her = (WildcardType)other;
      return
        Arrays.equals(this.getUpperBounds(), her.getUpperBounds()) &&
        Arrays.equals(this.getLowerBounds(), her.getLowerBounds());
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("?");
    Type[] bounds = this.getLowerBounds();
    if (bounds == null || bounds.length <= 0) {
      // Upper bounds only.
      bounds = this.getUpperBounds();
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
      this.upperBounds = OBJECT;
    } else if (serializableBounds.length == 1) {
      final Object bound = serializableBounds[0];
      if (bound == null || Object.class.equals(bound)) {
        this.upperBounds = OBJECT;
      } else {
        assert bound instanceof Type;
        this.upperBounds = new Type[] { (Type)bound };
      }
    } else {
      this.upperBounds = new Type[serializableBounds.length];
      System.arraycopy(serializableBounds, 0, this.upperBounds, 0, serializableBounds.length);
    }
    this.hashCode = Arrays.hashCode(this.getUpperBounds()) ^ Arrays.hashCode(this.getLowerBounds());
  }
  
  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Type[] originalBounds = this.getLowerBounds();
    if (originalBounds == null || originalBounds.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newBounds = new Serializable[originalBounds.length];
      for (int i = 0; i < newBounds.length; i++) {
        newBounds[i] = AbstractType.toSerializableType(originalBounds[i]);
      }
      stream.writeObject(newBounds);
    }
    originalBounds = this.getUpperBounds();
    if (originalBounds == null || originalBounds.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newBounds = new Serializable[originalBounds.length];
      for (int i = 0; i < newBounds.length; i++) {
        newBounds[i] = AbstractType.toSerializableType(originalBounds[i]);
      }
      stream.writeObject(newBounds);
    }
  }

}
