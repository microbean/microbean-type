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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.StringJoiner;

class AbstractWildcardType implements WildcardType {


  /*
   * Instance fields.
   */


  private final Type[] upperBounds;

  private final Type[] lowerBounds;

  private final int hashCode;


  /*
   * Constructors.
   */


  AbstractWildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
    super();
    if (upperBounds == null || upperBounds.length <= 0) {
      this.upperBounds = new Type[] { Object.class };
    } else if (upperBounds.length == 1) {
      this.upperBounds = upperBounds.clone();
    } else {
      throw new IllegalArgumentException("upperBounds.length > 1: " + Arrays.asList(upperBounds));
    }
    if (lowerBounds == null || lowerBounds.length <= 0) {
      this.lowerBounds = JavaTypes.emptyTypeArray();
    } else if (lowerBounds.length == 1) {
      this.lowerBounds = lowerBounds.clone();
    } else {
      throw new IllegalArgumentException("lowerBounds.length > 1: " + Arrays.asList(lowerBounds));
    }
    // Wildcards can't have primitives or other wildcards
    // (non-reference types) anywhere in their bounds.
    for (final Type upperBound : this.upperBounds) {
      if (!JavaTypes.isReferenceType(upperBound)) {
        throw new IllegalArgumentException("upperBounds contains non-reference type: " + Arrays.asList(this.upperBounds));
      }
    }
    for (final Type lowerBound : this.lowerBounds) {
      if (!JavaTypes.isReferenceType(lowerBound)) {
        throw new IllegalArgumentException("lowerBounds contains non-reference type: " + Arrays.asList(this.lowerBounds));
      }
    }
    this.hashCode = this.computeHashCode();
  }

  AbstractWildcardType(final WildcardType other) {
    this(other.getUpperBounds(), other.getLowerBounds());
  }


  /*
   * Instance methods.
   */


  public final Type getUpperBound() {
    return this.upperBounds[0];
  }

  public final Type getLowerBound() {
    return this.lowerBounds.length <=0 ? null : this.lowerBounds[0];
  }

  @Override // WildcardType
  public final Type[] getUpperBounds() {
    return this.upperBounds.clone();
  }

  @Override // WildcardType
  public final Type[] getLowerBounds() {
    return this.lowerBounds.clone();
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
    } else if (other instanceof WildcardType w) {
      return JavaTypes.equals(this, w);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return JavaTypes.toString(this);
  }


  /*
   * Static methods.
   */

  
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
