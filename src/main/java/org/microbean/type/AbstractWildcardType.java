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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.StringJoiner;

abstract class AbstractWildcardType implements WildcardType {
  
  static final Type[] objectClassOnlyBounds = new Type[] { Object.class };

  static final Type[] emptyBounds = new Type[0];
  
  private final Type[] upperBounds;

  private final Type[] lowerBounds;

  AbstractWildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
    super();
    this.upperBounds = upperBounds == null ? objectClassOnlyBounds : upperBounds;
    this.lowerBounds = lowerBounds == null ? emptyBounds : lowerBounds;
  }

  @Override
  public final Type[] getUpperBounds() {
    return this.upperBounds;
  }

  @Override
  public final Type[] getLowerBounds() {
    return this.lowerBounds;
  }

  @Override
  public final int hashCode() {
    return Arrays.hashCode(this.getUpperBounds()) ^ Arrays.hashCode(this.getLowerBounds());
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

}
